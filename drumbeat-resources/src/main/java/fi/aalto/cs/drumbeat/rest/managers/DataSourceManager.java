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
import com.hp.hpl.jena.vocabulary.RDF;

import fi.aalto.cs.drumbeat.rest.ontology.BuildingDataOntology;

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


public class DataSourceManager {
	private static final Logger logger = Logger.getLogger(DataSourceManager .class);
	private final Model model;	
	
	public Model getModel() {
		return model;
	}

	public DataSourceManager(Model model) {
		this.model = model;
	}
	
	public boolean listAll(Model m,String collection) {
		boolean ret=false;
		final QueryExecution queryExecution = 
				QueryExecutionFactory.create(
						QueryFactory.create("PREFIX lbdh: <http://drumbeat.cs.hut.fi/owl/LDBHO#>"
								+ "SELECT ?datasource "
								+ "WHERE {"
								+  "<"+AppManager.BASE_URL+"collections/"+collection+"> lbdh:hasDataSources ?datasource."								
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
	
	public boolean get(Model m,String collectionname,String datasourcename) {
		boolean ret=false;
		final QueryExecution queryExecution = 
				QueryExecutionFactory.create(
						QueryFactory.create(
								String.format("SELECT ?p ?o  WHERE {<%s> ?p ?o} ",AppManager.BASE_URL+"datasources/"+collectionname+"/"+datasourcename)),
						model);

         ResultSet rs = queryExecution.execSelect();
         Resource ds = model.createResource(AppManager.BASE_URL+"datasources/"+collectionname+"/"+datasourcename); 
         while (rs.hasNext()) {
        	         ret=true;
                     QuerySolution row = rs.nextSolution();
                     Property p = model.createProperty(row.getResource("p").getURI());
                     RDFNode o = row.get("o");
                     m.add(m.createStatement(ds,p,o));
         }
         return ret;
	}
	

	public Resource getResource(String collectionname,String datasourcename) {
		Resource r = model.createResource(AppManager.BASE_URL+"datasources/"+collectionname+"/"+datasourcename); 
		if (model.contains( r, null, (RDFNode) null )) {
			return r;
		}
		return null;
	}
	
	
	public void create(String collectionname,String datasourcename) {
		Resource r = model.createResource(AppManager.BASE_URL+"datasources/"+collectionname+"/"+datasourcename); 
		Resource type = model.createResource(BuildingDataOntology.DataSources.DataSource);
        Property name_property = ResourceFactory.createProperty(BuildingDataOntology.DataSources.name);
        r.addProperty(RDF.type,type);
        r.addProperty(name_property,datasourcename , XSDDatatype.XSDstring);
	}
	
	public void delete(String collectionname,String datasourcename)  {
		Resource r = model.createResource(AppManager.BASE_URL+"datasources/"+collectionname+"/"+datasourcename); 
		model.removeAll(r, null, null );
		model.removeAll(null, null, r);
	}

}

