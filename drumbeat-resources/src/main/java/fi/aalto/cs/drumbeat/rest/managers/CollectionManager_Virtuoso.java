package fi.aalto.cs.drumbeat.rest.managers;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.aalto.cs.drumbeat.rest.ontology.BuildingDataOntology;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

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


public class CollectionManager_Virtuoso {
	private static final Logger logger = Logger.getLogger(CollectionManager_Virtuoso .class);
	private final Model model;	
	
	public Model getModel() {
		return model;
	}

	public CollectionManager_Virtuoso(Model model) {
		this.model = model;
	}
	
	public Model listAll() {
		Query sparql = QueryFactory.create("PREFIX lbdh: <http://drumbeat.cs.hut.fi/owl/LDBHO#>"
				+ "CONSTRUCT {?collection a lbdh:Collection}"
				+ "WHERE {"
				+ "?collection a lbdh:Collection ."
				+ "}") ;
		VirtGraph set = new VirtGraph ("jdbc:virtuoso://localhost:1111", "dba", "dba");	
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, set);

		return vqe.execConstruct();
	}

	
	public Model get(String guid) {
		Query sparql = QueryFactory.create(String.format("CONSTRUCT {<%s> ?p ?o .}  WHERE {<%s> ?p ?o .} ",BuildingDataOntology.Collections.Collection+"/"+guid,BuildingDataOntology.Collections.Collection+"/"+guid)) ;
		VirtGraph set = new VirtGraph ("jdbc:virtuoso://localhost:1111", "dba", "dba");	
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, set);
		return vqe.execConstruct();
	}
	

	public Resource getResource(String guid) {
		Resource r = ResourceFactory.createResource(BuildingDataOntology.Collections.Collection+"/"+guid); 
		if (model.contains( r, null, (RDFNode) null )) {
			return r;
		}
		return null;
	}
	
	public Model  listDataSources(String guid) {
		Query sparql = QueryFactory.create(String.format("CONSTRUCT {<%s> <%s> ?ds} WHERE {<%s> <%s> ?ds} ",BuildingDataOntology.Collections.Collection+"/"+guid,BuildingDataOntology.Collections.hasDataSources,BuildingDataOntology.Collections.Collection+"/"+guid,BuildingDataOntology.Collections.hasDataSources)) ;
		VirtGraph set = new VirtGraph ("jdbc:virtuoso://localhost:1111", "dba", "dba");	
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, set);
		return vqe.execConstruct();
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

}
