package fi.aalto.cs.drumbeat.rest.managers;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
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


public class CollectionManager_Select {
	private static final Logger logger = Logger.getLogger(CollectionManager_Select .class);
	private final Model model;	
	
	public Model getModel() {
		return model;
	}

	public CollectionManager_Select(Model model) {
		this.model = model;
	}
	

	public Resource getResource(String guid) {
		Resource r = ResourceFactory.createResource(BuildingDataOntology.Collections.Collection+"/"+guid); 
		if (model.contains( r, null, (RDFNode) null )) {
			return r;
		}
		return null;
	}
	
	public void create(String guid,String name) {
		Resource r = model.createResource(BuildingDataOntology.Collections.Collection+"/"+guid); 
		Resource c = model.createResource(BuildingDataOntology.Collections.Collection);
        Property name_property = ResourceFactory.createProperty(BuildingDataOntology.Collections.name);
        r.addProperty(RDF.type,c);
        r.addProperty(name_property,name , XSDDatatype.XSDstring);
	}
	
	public void delete(String guid) {
		Resource r = ResourceFactory.createResource(BuildingDataOntology.Collections.Collection+"/"+guid); 
		model.removeAll(r, null, null );
		model.removeAll(null, null, r);
	}
	
	
	
	
	public ResultSet listAll() {
		final QueryExecution queryExecution = 
				QueryExecutionFactory.create(
						QueryFactory.create(
								String.format("SELECT ?collection WHERE { ?collection a <%s>} ",BuildingDataOntology.Collections.Collection)),
						model);
		
		return queryExecution.execSelect();
	}


	public ResultSet get(String guid) {
		final QueryExecution queryExecution = 
				QueryExecutionFactory.create(
						QueryFactory.create(
								String.format("SELECT ?p ?o  WHERE {<%s> ?p ?o} ",BuildingDataOntology.Collections.Collection+"/"+guid)),
						model);
		return queryExecution.execSelect();
	}
	

	
	public ResultSet listDataSources(String guid) {
		final QueryExecution queryExecution = 
				QueryExecutionFactory.create(
						QueryFactory.create(
								String.format("SELECT ?ds WHERE {<%s> <%p> ?ds} ",BuildingDataOntology.Collections.Collection+"/"+guid,BuildingDataOntology.Collections.hasDataSources)),
						model);
		
		return queryExecution.execSelect();
	}


}
