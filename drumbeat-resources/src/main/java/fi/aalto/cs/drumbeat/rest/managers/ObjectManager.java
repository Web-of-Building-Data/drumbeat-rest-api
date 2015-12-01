package fi.aalto.cs.drumbeat.rest.managers;

import org.apache.log4j.Logger;

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


public class ObjectManager {
	private static final Logger logger = Logger.getLogger(ObjectManager .class);
	private final Model model;	
	
	public Model getModel() {
		return model;
	}

	public ObjectManager(Model model) {
		this.model = model;
	}
	
	public boolean get(String guid,Model m) {
		boolean ret=false;
		final QueryExecution queryExecution = 
				QueryExecutionFactory.create(
						QueryFactory.create(
								String.format("SELECT ?p ?o  WHERE {<%s> ?p ?o} ",ApplicationConfig.getBaseUrl()+"Collection/"+guid)),
						model);

         ResultSet rs = queryExecution.execSelect();
         Resource c = model.createResource(BuildingDataOntology.Collections.Collection+"/"+guid);        	 
         while (rs.hasNext()) {
        	         ret=true;
                     QuerySolution row = rs.nextSolution();
                     Property p = model.createProperty(row.getResource("p").getURI());
                     RDFNode o = row.get("o");
                     m.add(m.createStatement(c,p,o));
         }
         return ret;
	}
	

	public Resource getResource(String guid) {
		Resource r = ResourceFactory.createResource(ApplicationConfig.getBaseUrl()+"Collection/"+guid); 
		if (model.contains( r, null, (RDFNode) null )) {
			return r;
		}
		return null;
	}
}
