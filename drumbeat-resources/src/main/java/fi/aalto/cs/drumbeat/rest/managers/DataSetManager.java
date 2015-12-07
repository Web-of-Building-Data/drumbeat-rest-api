package fi.aalto.cs.drumbeat.rest.managers;

import java.io.InputStream;


import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.Logger;

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
import com.hp.hpl.jena.sparql.core.DatasetGraphMaker;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.aalto.cs.drumbeat.rest.application.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.ontology.BuildingDataOntology;
import fi.aalto.cs.drumbeat.rest.ontology.BuildingDataVocabulary;
import fi.hut.cs.drumbeat.common.config.ComplexProcessorConfiguration;
import fi.hut.cs.drumbeat.common.config.document.ConfigurationDocument;
import fi.hut.cs.drumbeat.ifc.convert.ifc2ld.Ifc2RdfConversionContext;
import fi.hut.cs.drumbeat.ifc.convert.ifc2ld.Ifc2RdfModelExporter;
import fi.hut.cs.drumbeat.ifc.convert.ifc2ld.cli.Ifc2RdfExporter;
import fi.hut.cs.drumbeat.ifc.convert.stff2ifc.IfcModelParser;
import fi.hut.cs.drumbeat.ifc.data.model.IfcModel;
import fi.hut.cs.drumbeat.ifc.processing.IfcModelAnalyser;

import static fi.aalto.cs.drumbeat.rest.ontology.BuildingDataOntology.*;

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
	private static final Logger logger = Logger.getLogger(DataSetManager.class);
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
								+  "<"+DrumbeatApplication.getInstance().getBaseUri()+"datasets/"+collectionname+"/"+datasourcename+"> lbdh:hasDataSets ?dataset."		
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
								String.format("SELECT ?p ?o  WHERE {<%s> ?p ?o} ",DrumbeatApplication.getInstance().getBaseUri()+"datasources/"+collectionname+"/"+datasourcename)),
						model);

         ResultSet rs = queryExecution.execSelect();
         Resource ds = model.createResource(DrumbeatApplication.getInstance().getBaseUri()+"datasources/"+collectionname+"/"+datasourcename); 
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
		Resource r = model.createResource(DrumbeatApplication.getInstance().getBaseUri()+"datasets/"+collectionname+"/"+datasourcename+"/"+datasetname); 
		if (model.contains( r, null, (RDFNode) null )) {
			return r;
		}
		return null;
	}
	
	
	public void create(String collectionname,String datasourcename,String datasetname) {
		Resource datasource = model.createResource(DrumbeatApplication.getInstance().getBaseUri()+"datasources/"+collectionname+"/"+datasourcename);
		Resource dataset = model.createResource(DrumbeatApplication.getInstance().getBaseUri()+"datasets/"+collectionname+"/"+datasourcename+"/"+datasetname); 

		Resource type = model.createResource(BuildingDataOntology.DataSources.DataSource);
        Property name_property = ResourceFactory.createProperty(BuildingDataOntology.DataSources.name);
        Property hasDataSets = ResourceFactory.createProperty(BuildingDataOntology.DataSources.hasDataSets);
        Property isDataSet = ResourceFactory.createProperty(BuildingDataOntology.DataSets.isDataSet);
   
        datasource.addProperty(hasDataSets, dataset);
        dataset.addProperty(isDataSet, datasource);
        
        dataset.addProperty(RDF.type,type);
        dataset.addProperty(name_property,datasourcename , XSDDatatype.XSDstring);
	}
	
	public void delete(String collectionname,String datasourcename,String datasetname) {
		String item=DrumbeatApplication.getInstance().getBaseUri()+"datasets/"+collectionname+"/"+datasourcename+"/"+datasetname;
		String update1=String.format("DELETE {<%s> ?p ?o} WHERE {<%s> ?p ?o }",item,item);
		String update2=String.format("DELETE {?s ?p <%s>} WHERE {<%s> ?p ?o }",item,item);
		DatasetGraphMaker gs= new DatasetGraphMaker(model.getGraph()); 
		UpdateAction.parseExecute(update1, gs);
		UpdateAction.parseExecute(update2, gs);
	}
	
	
	public boolean exists(String collectionId, String dataSourceId, String dataSetId) {
		String collectionUri = Collections.formatUrl(collectionId);
		String dataSourceUri = DataSources.formatUrl(collectionId, dataSourceId);
		String dataSetUri = DataSets.formatUrl(collectionId, dataSourceId, dataSetId);
		
		String queryString =
				String.format(
					"PREFIX ldbho: <%s> \n" +
					"ASK { \n" + 
					"<%s> a ldbho:Collection ; ldbho:hasDataSource <%s> . \n" +
					"<%s> a ldbho:DataSource ; ldbho:hasDataSet <%s> . \n" +
					"<%s> a ldbho:DataSet . }",
					BuildingDataVocabulary.BASE_URL,
					collectionUri,
					dataSourceUri,
					dataSourceUri,
					dataSetUri,
					dataSetUri
					);
		
		QueryExecution queryExecution = 
				QueryExecutionFactory.create(
						QueryFactory.create(queryString),
						model);
		
		return queryExecution.execAsk();
	}
	
	public Model uploadIfcData(InputStream inputStream, Model jenaModel) throws Exception
	{		
		logger.info("Uploading IFC model");
		try {			
			// loading schemas and config files
			synchronized (DataSetManager.class) {
				if (!ifcSchemaLoaded) {
					ConfigurationDocument.load(DrumbeatApplication.getInstance().getRealPath(DrumbeatApplication.Paths.IFC2LD_CONFIG_FILE_PATH));				
					Ifc2RdfExporter.parseSchemas(DrumbeatApplication.getInstance().getRealPath(DrumbeatApplication.Paths.IFC_SCHEMA_FOLDER_PATH));
				}			
			}
			
			// parse model
			logger.debug("Parsing model");
			IfcModel ifcModel = IfcModelParser.parse(inputStream);			

			// ground nodes in the model
			logger.debug("Grounding nodes");
			IfcModelAnalyser modelAnalyser = new IfcModelAnalyser(ifcModel);			
			ComplexProcessorConfiguration groundingConfiguration = IfcModelAnalyser.getDefaultGroundingRuleSets();		
			modelAnalyser.groundNodes(groundingConfiguration);

			// export model
			logger.debug("exporting model");
			Ifc2RdfConversionContext conversionContext = DrumbeatApplication.getInstance().getDefaultIfc2RdfConversionContext();
			conversionContext.setModelNamespaceUriFormat(DrumbeatApplication.getInstance().getBaseUri());
			
			Ifc2RdfModelExporter modelExporter = new Ifc2RdfModelExporter(ifcModel, conversionContext, jenaModel);
			jenaModel = modelExporter.export();
			logger.info("Uploading IFC model completed successfully");
			return jenaModel;
			
		} catch (Exception e) {
			logger.error("Uploading IFC model failed", e);
			throw e;			
		}
	}
	
	
	public Model uploadRdfData(InputStream inputStream, Lang rdfLang, Model jenaModel) throws Exception
	{		
		logger.info("Uploading RDF model");
		try {			
			// export model
			logger.debug("exporting model");
			RDFDataMgr.read(jenaModel, inputStream, rdfLang);
			
			logger.info("Uploading RDF model completed successfully");
			return jenaModel;
			
		} catch (Exception e) {
			logger.error("Uploading RDF model failed", e);
			throw e;			
		}
	}	

}

