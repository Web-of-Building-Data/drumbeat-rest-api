package fi.aalto.cs.drumbeat.rest.managers;

import java.io.InputStream;

import javax.servlet.ServletContext;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.aalto.cs.drumbeat.rest.api.ApplicationConfig;
import fi.aalto.cs.drumbeat.rest.ontology.BuildingDataOntology;
import fi.hut.cs.drumbeat.common.config.ComplexProcessorConfiguration;
import fi.hut.cs.drumbeat.common.config.document.ConfigurationDocument;
import fi.hut.cs.drumbeat.ifc.convert.ifc2ld.cli.Ifc2RdfExporter;
import fi.hut.cs.drumbeat.ifc.convert.ifc2ld.util.Ifc2RdfExportUtil;
import fi.hut.cs.drumbeat.ifc.convert.stff2ifc.IfcModelParser;
import fi.hut.cs.drumbeat.ifc.data.model.IfcModel;
import fi.hut.cs.drumbeat.ifc.processing.IfcModelAnalyser;

/*
The MIT License (MIT)

Copyright (c) 2015 Jyrki Oraskari

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/


public class DataSetManager {
//	private static final Logger logger = Logger.getLogger(DataSetManager.class);
	private static boolean ifcSchemaLoaded; 

	private final Model model;	
	
	public Model getModel() {
		return model;
	}

	public DataSetManager(Model model) {
		this.model = model;
	}
	
	public boolean listAll(Model m,String collectionname,String datasourcename) {
		boolean ret=false;
		final QueryExecution queryExecution = 
				QueryExecutionFactory.create(
						QueryFactory.create("PREFIX lbdh: <http://drumbeat.cs.hut.fi/owl/LDBHO#>"
								+ "SELECT ?dataset "
								+ "WHERE {"								
								+  "<"+ApplicationConfig.getBaseUrl()+"datasets/"+collectionname+"/"+datasourcename+"> lbdh:hasDataSets ?dataset."		
								+ "}"
								),
						model);

         ResultSet rs = queryExecution.execSelect();
         Resource type = model.createResource(BuildingDataOntology.DataSets.DataSet); 		
         while (rs.hasNext()) {
        	         ret=true;
                     QuerySolution row = rs.nextSolution();                     
                     Resource c = model.createResource(row.getResource("dataset").getURI());
                     m.add(m.createStatement(c,RDF.type,type));
         }
         return ret;
	}
	
	public boolean get(Model m,String collectionname,String datasourcename,String datasetname) {
		boolean ret=false;
		final QueryExecution queryExecution = 
				QueryExecutionFactory.create(
						QueryFactory.create(
								String.format("SELECT ?p ?o  WHERE {<%s> ?p ?o} ",ApplicationConfig.getBaseUrl()+"datasources/"+collectionname+"/"+datasourcename)),
						model);

         ResultSet rs = queryExecution.execSelect();
         Resource ds = model.createResource(ApplicationConfig.getBaseUrl()+"datasources/"+collectionname+"/"+datasourcename); 
         while (rs.hasNext()) {
        	         ret=true;
                     QuerySolution row = rs.nextSolution();
                     Property p = model.createProperty(row.getResource("p").getURI());
                     RDFNode o = row.get("o");
                     m.add(m.createStatement(ds,p,o));
         }
         return ret;
	}
	

	public Resource getResource(String collectionname,String datasourcename,String datasetname) {
		Resource r = model.createResource(ApplicationConfig.getBaseUrl()+"datasets/"+collectionname+"/"+datasourcename+"/"+datasetname); 
		if (model.contains( r, null, (RDFNode) null )) {
			return r;
		}
		return null;
	}
	
	
	public void create(String collectionname,String datasourcename,String datasetname) {
		Resource datasource = model.createResource(ApplicationConfig.getBaseUrl()+"datasources/"+collectionname+"/"+datasourcename);
		Resource dataset = model.createResource(ApplicationConfig.getBaseUrl()+"datasets/"+collectionname+"/"+datasourcename+"/"+datasetname); 

		Resource type = model.createResource(BuildingDataOntology.DataSources.DataSource);
        Property name_property = ResourceFactory.createProperty(BuildingDataOntology.DataSources.name);
        Property hasDataSets = ResourceFactory.createProperty(BuildingDataOntology.DataSources.hasDataSets);
        Property isDataSet = ResourceFactory.createProperty(BuildingDataOntology.DataSets.isDataSet);
   
        datasource.addProperty(hasDataSets, dataset);
        dataset.addProperty(isDataSet, datasource);
        
        dataset.addProperty(RDF.type,type);
        dataset.addProperty(name_property,datasourcename , XSDDatatype.XSDstring);
	}
	
	public void delete(String collectionname,String datasourcename,String datasetname)  {
		Resource r = model.createResource(ApplicationConfig.getBaseUrl()+"datasets/"+collectionname+"/"+datasourcename+"/"+datasetname); 
		model.removeAll(r, null, null );
		model.removeAll(null, null, r);
	}
	
	public void importData(ServletContext servletContext, InputStream inputStream, Model jenaModel) throws Exception
	{		
		synchronized (DataSetManager.class) {
			if (!ifcSchemaLoaded) {
				ConfigurationDocument.load(ApplicationConfig.Paths.IFC2LD_CONFIG_FILE_PATH);				
				Ifc2RdfExporter.parseSchemas(ApplicationConfig.Paths.IFC_SCHEMA_FOLDER_PATH);
			}			
		}
		
		IfcModel ifcModel = IfcModelParser.parse(inputStream);

		ComplexProcessorConfiguration groundingConfiguration = IfcModelAnalyser.getDefaultGroundingRuleSets();
		
		// ground nodes in the model
		IfcModelAnalyser modelAnalyser = new IfcModelAnalyser(ifcModel);			
		modelAnalyser.groundNodes(groundingConfiguration);
		
		Ifc2RdfExportUtil.exportModelToJenaModel(jenaModel, ifcModel);
	}	
	

}

