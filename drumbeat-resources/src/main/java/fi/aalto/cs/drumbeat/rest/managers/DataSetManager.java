package fi.aalto.cs.drumbeat.rest.managers;

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
Copyright (c) 2015 Nam Vu Hoang

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


public class DataSetManager  extends AbstractManager{

	private final Model model;	
	
	public Model getModel() {
		return model;
	}

	public DataSetManager(Model model) {
		this.model = model;
	}
	
	public boolean listAll2Model(Model m,String collectionid,String datasourceid) {
		boolean ret=false;
		final QueryExecution queryExecution = 
				QueryExecutionFactory.create(
						QueryFactory.create("PREFIX lbdh: <http://drumbeat.cs.hut.fi/owl/LDBHO#>"
								+ "SELECT ?dataset "
								+ "WHERE {"								
								+  "<"+DrumbeatWebApplication.getInstance().getBaseUri()+"datasets/"+collectionid+"/"+datasourceid+"> lbdh:hasDataSets ?dataset."		
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

	
	
	@Override
	public boolean get2Model(Model m,String... specification) {
		return get2Model_implementation(m, specification[0],specification[1],specification[2]);
	}

	private boolean get2Model_implementation(Model m,String collectionid,String datasourceid,String datasetid) {
		boolean ret=false;
		final QueryExecution queryExecution = 
				QueryExecutionFactory.create(
						QueryFactory.create(
								String.format("SELECT ?p ?o  WHERE {<%s> ?p ?o} ",DrumbeatWebApplication.getInstance().getBaseUri()+"datasources/"+collectionid+"/"+datasourceid)),
						model);

         ResultSet rs = queryExecution.execSelect();
         Resource ds = model.createResource(DrumbeatWebApplication.getInstance().getBaseUri()+"datasources/"+collectionid+"/"+datasourceid); 
         while (rs.hasNext()) {
        	         ret=true;
                     QuerySolution row = rs.nextSolution();
                     Property p = model.createProperty(row.getResource("p").getURI());
                     RDFNode o = row.get("o");
                     m.add(m.createStatement(ds,p,o));
         }
         return ret;
	}
	

	public Resource getResource(String collectionid,String datasourceid,String datasetid) {
		Resource r = model.createResource(DrumbeatWebApplication.getInstance().getBaseUri()+"datasets/"+collectionid+"/"+datasourceid+"/"+datasetid); 
		if (model.contains( r, null, (RDFNode) null )) {
			return r;
		}
		return null;
	}

	public boolean isCollectionAndDataSourceExisting(String collectionId, String dataSourceId) {
		String collectionUri = Collections.formatUrl(collectionId);
		String dataSourceUri = DataSources.formatUrl(collectionId, dataSourceId);
		
		String queryString =
				String.format(
					"PREFIX lbdho: <%s> \n" +
					"ASK { \n" + 
					"<%s> a lbdho:Collection ; lbdho:hasDataSource <%s> . \n" +					
					"}",
					BuildingDataOntology.Ontology_BASE_URL,
					collectionUri,
					dataSourceUri);
		
		QueryExecution queryExecution = 
				QueryExecutionFactory.create(
						QueryFactory.create(queryString),
						model);
		
		boolean result = queryExecution.execAsk();
		return result;
	}
	
	@Override
	public boolean create(String... specification) {
		return create_implementation(specification[0],specification[1],specification[2],specification[3]);
		
	}

	@Override
	public boolean delete(String... specification) {
		return delete_implementation(specification[0],specification[1],specification[2]);
	}
	
	
	private boolean create_implementation(String collectionid,String datasourceid,String datasetid,String name) {
		if(!isCollectionAndDataSourceExisting(collectionid,datasourceid))
			return false;
		Resource datasource = model.createResource(DrumbeatWebApplication.getInstance().getBaseUri()+"datasources/"+collectionid+"/"+datasourceid);
		Resource dataset = model.createResource(DrumbeatWebApplication.getInstance().getBaseUri()+"datasets/"+collectionid+"/"+datasourceid+"/"+datasetid); 

		Resource type = model.createResource(BuildingDataOntology.DataSources.DataSource);
        Property name_property = ResourceFactory.createProperty(BuildingDataOntology.DataSources.name);
        Property hasDataSets = ResourceFactory.createProperty(BuildingDataOntology.DataSources.hasDataSets);
        Property isDataSet = ResourceFactory.createProperty(BuildingDataOntology.DataSets.isDataSet);
   
        datasource.addProperty(hasDataSets, dataset);
        dataset.addProperty(isDataSet, datasource);
        
        dataset.addProperty(RDF.type,type);
        dataset.addProperty(name_property,name , XSDDatatype.XSDstring);
        return true;
	}
	
	private boolean delete_implementation(String collectionid,String datasourceid,String datasetid) {
		String item=DrumbeatWebApplication.getInstance().getBaseUri()+"datasets/"+collectionid+"/"+datasourceid+"/"+datasetid;
		String update1=String.format("DELETE {<%s> ?p ?o} WHERE {<%s> ?p ?o }",item,item);
		String update2=String.format("DELETE {?s ?p <%s>} WHERE {<%s> ?p ?o }",item,item);
		DatasetGraphMaker gs= new DatasetGraphMaker(model.getGraph()); 
		UpdateAction.parseExecute(update1, gs);
		UpdateAction.parseExecute(update2, gs);
		return true;
	}
	
	
	/**
	 * Checks if a dataset with the collection/datasource/dataset names exists
	 * @param collectionId
	 * @param dataSourceId
	 * @param dataSetId
	 * @return true if the dataset exists
	 * @author Nam Vu 
	 */
	public boolean checkExists(String collectionId, String dataSourceId, String dataSetId) {
		String collectionUri = Collections.formatUrl(collectionId);
		String dataSourceUri = DataSources.formatUrl(collectionId, dataSourceId);
		String dataSetUri = DataSets.formatUrl(collectionId, dataSourceId, dataSetId);
		
		String queryString =
				String.format(
					"PREFIX lbdho: <%s> \n" +
					"ASK { \n" + 
					"<%s> a lbdho:Collection ; lbdho:hasDataSource <%s> . \n" +
					"<%s> a lbdho:DataSource ; lbdho:hasDataSet <%s> . \n" +
					"<%s> a lbdho:DataSet . }",
					BuildingDataOntology.Ontology_BASE_URL,
					collectionUri,
					dataSourceUri,
					dataSourceUri,
					dataSetUri,
					dataSetUri);
		
		QueryExecution queryExecution = 
				QueryExecutionFactory.create(
						QueryFactory.create(queryString),
						model);
		
		boolean result = queryExecution.execAsk();
		return result;
	}

	
	
	
}

