package fi.aalto.cs.drumbeat.rest.managers;



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

import fi.aalto.cs.drumbeat.rest.common.DrumbeatWebApplication;
import fi.aalto.cs.drumbeat.rest.ontology.BuildingDataOntology;
import fi.aalto.cs.drumbeat.rest.ontology.BuildingDataOntology.Collections;
import fi.aalto.cs.drumbeat.rest.ontology.BuildingDataOntology.DataSets;
import fi.aalto.cs.drumbeat.rest.ontology.BuildingDataOntology.DataSources;

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


public class DataSourceManager  extends AbstractManager{
	private static final Logger logger = Logger.getLogger(DataSourceManager .class);
	private final Model model;	
	
	public Model getModel() {
		return model;
	}

	public DataSourceManager(Model model) {
		this.model = model;
	}
	
	public boolean listAll2Model(Model m,String collectionid) {
		boolean ret=false;
		final QueryExecution queryExecution = 
				QueryExecutionFactory.create(
						QueryFactory.create("PREFIX lbdh: <"+BuildingDataOntology.Ontology_BASE_URL+">"
								+ "SELECT ?datasource "
								+ "WHERE {"
								+  "<"+DrumbeatWebApplication.getInstance().getBaseUri()+"collections/"+collectionid+"> lbdh:hasDataSource ?datasource."								
								+ "}"
								),
						model);

         ResultSet rs = queryExecution.execSelect();
         Resource type = model.createResource(BuildingDataOntology.DataSources.DataSource); 		
         while (rs.hasNext()) {
        	         ret=true;
                     QuerySolution row = rs.nextSolution();                     
                     Resource c = model.createResource(row.getResource("datasource").getURI());
                     m.add(m.createStatement(c,RDF.type,type));
         }
         return ret;
	}
	
		
	@Override
	public boolean get2Model(Model m,String... specification) {
		return get2Model_implementation(m, specification[0],specification[1]);
	}
	
	private boolean get2Model_implementation(Model m,String collectionid,String datasourcerid) {
		boolean ret=false;
		final QueryExecution queryExecution = 
				QueryExecutionFactory.create(
						QueryFactory.create(
								String.format("SELECT ?p ?o  WHERE {<%s> ?p ?o} ",DrumbeatWebApplication.getInstance().getBaseUri()+"datasources/"+collectionid+"/"+datasourcerid)),
						model);

         ResultSet rs = queryExecution.execSelect();
         Resource ds = model.createResource(DrumbeatWebApplication.getInstance().getBaseUri()+"datasources/"+collectionid+"/"+datasourcerid); 
         while (rs.hasNext()) {
        	         ret=true;
                     QuerySolution row = rs.nextSolution();
                     Property p = model.createProperty(row.getResource("p").getURI());
                     RDFNode o = row.get("o");
                     m.add(m.createStatement(ds,p,o));
         }
         return ret;
	}

	public boolean hasDataSets(String collectionid,String datasourceid) {
		final QueryExecution queryExecution = 
				QueryExecutionFactory.create(
						QueryFactory.create("PREFIX lbdh: <"+BuildingDataOntology.Ontology_BASE_URL+">"
								+ "SELECT ?dataset "
								+ "WHERE {"								
								+  "<"+DrumbeatWebApplication.getInstance().getBaseUri()+"datasets/"+collectionid+"/"+datasourceid+"> lbdh:hasDataSet ?dataset."		
								+ "}"
								),
						model);

         ResultSet rs = queryExecution.execSelect();
         Resource type = model.createResource(BuildingDataOntology.DataSets.DataSet); 		
         if (rs.hasNext())
        	         return true;
         return false;
	}
	
	public boolean isCollectionExisting(String collection_id) {
		final QueryExecution queryExecution = 
				QueryExecutionFactory.create(
						QueryFactory.create(
								String.format("SELECT ?p ?o  WHERE {<%s> ?p ?o} ",DrumbeatWebApplication.getInstance().getBaseUri()+"collections/"+collection_id)),
						model);

         ResultSet rs = queryExecution.execSelect();
         Resource c = model.createResource(DrumbeatWebApplication.getInstance().getBaseUri()+"collections/"+collection_id);        	 
         if(rs.hasNext())
        	          return true;
         return false;
	}

	
	@Override
	public boolean create(String... specification) {
		return create_implementation(specification[0],specification[1],specification[2]);
		
	}

	@Override
	public boolean  delete(String... specification) {
		return delete_implementation(specification[0],specification[1]);
	}
	
	

		
	private boolean create_implementation(String collectionid,String datasourceid,String name) {
		if(!isCollectionExisting(collectionid))
			return false;
		Resource collection = model.createResource(DrumbeatWebApplication.getInstance().getBaseUri()+"collections/"+collectionid); 
		Resource datasource = model.createResource(DrumbeatWebApplication.getInstance().getBaseUri()+"datasources/"+collectionid+"/"+datasourceid);
		Resource type = model.createResource(BuildingDataOntology.DataSources.DataSource);

        Property hasDataSource = ResourceFactory.createProperty(BuildingDataOntology.Collections.hasDataSource);
        Property inCollection = ResourceFactory.createProperty(BuildingDataOntology.DataSources.inCollection);
        Property name_property = ResourceFactory.createProperty(BuildingDataOntology.DataSources.name);

		collection.addProperty(hasDataSource, datasource);
		datasource.addProperty(inCollection, collection);
		
        datasource.addProperty(RDF.type,type);
        datasource.addProperty(name_property,name , XSDDatatype.XSDstring);
        return true;
	}
	
	private boolean delete_implementation(String collectionid,String datasourceid) {
		if(hasDataSets(collectionid,datasourceid))
			return false;
		String item=DrumbeatWebApplication.getInstance().getBaseUri()+"datasources/"+collectionid+"/"+datasourceid;
		String update1=String.format("DELETE {<%s> ?p ?o} WHERE {<%s> ?p ?o }",item,item);
		String update2=String.format("DELETE {?s ?p <%s>} WHERE {<%s> ?p ?o }",item,item);
		DatasetGraphMaker gs= new DatasetGraphMaker(model.getGraph()); 
		UpdateAction.parseExecute(update1, gs);
		UpdateAction.parseExecute(update2, gs);
		return true;
	}
	


}

