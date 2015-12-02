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

import fi.aalto.cs.drumbeat.rest.api.ApplicationConfig;
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


public class CollectionManager {
	private static final Logger logger = Logger.getLogger(CollectionManager .class);
	private final Model model;	
	
	public Model getModel() {
		return model;
	}

	public CollectionManager(Model model) {
		this.model = model;
	}
	
	public boolean listAll(Model m) {
		boolean ret=false;
		final QueryExecution queryExecution = 
				QueryExecutionFactory.create(
						QueryFactory.create("PREFIX lbdh: <http://drumbeat.cs.hut.fi/owl/LDBHO#>"
								+ "SELECT ?collection "
								+ "WHERE {"
								+ "?collection ?p lbdh:Collection ."
								+ "}"
								),
						model);

         ResultSet rs = queryExecution.execSelect();
         Resource type = model.createResource(BuildingDataOntology.Collections.Collection); 		
         while (rs.hasNext()) {
        	         ret=true;
                     QuerySolution row = rs.nextSolution();                     
                     Resource c = model.createResource(row.getResource("collection").getURI());
                     m.add(m.createStatement(c,RDF.type,type));
         }
         return ret;
	}
	
	public boolean get(String name,Model m) {
		boolean ret=false;
		final QueryExecution queryExecution = 
				QueryExecutionFactory.create(
						QueryFactory.create(
								String.format("SELECT ?p ?o  WHERE {<%s> ?p ?o} ",ApplicationConfig.getBaseUrl()+"collections/"+name)),
						model);

         ResultSet rs = queryExecution.execSelect();
         Resource c = model.createResource(BuildingDataOntology.Collections.Collection+"/"+name);        	 
         while (rs.hasNext()) {
        	         ret=true;
                     QuerySolution row = rs.nextSolution();
                     Property p = model.createProperty(row.getResource("p").getURI());
                     RDFNode o = row.get("o");
                     m.add(m.createStatement(c,p,o));
         }
         return ret;
	}
	


	public void create(String name) {
		Resource collection = model.createResource(ApplicationConfig.getBaseUrl()+"collections/"+name); 
		Resource type = model.createResource(BuildingDataOntology.Collections.Collection);
        Property name_property = ResourceFactory.createProperty(BuildingDataOntology.Collections.property_name);
        collection.addProperty(RDF.type,type);
        collection.addProperty(name_property,name , XSDDatatype.XSDstring);
	}
	
	public void delete(String name) {
		Resource collection = ResourceFactory.createResource(ApplicationConfig.getBaseUrl()+"collections/"+name); 
		model.removeAll(collection, null, null );
		model.removeAll(null, null, collection);
	}

}
