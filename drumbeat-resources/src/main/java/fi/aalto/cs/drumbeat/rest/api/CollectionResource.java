package fi.aalto.cs.drumbeat.rest.api;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.github.jsonldjava.jena.JenaJSONLD;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.aalto.cs.drumbeat.rest.managers.AppManager;
import fi.aalto.cs.drumbeat.rest.managers.CollectionManager;
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


@Path("/collections")
public class CollectionResource {

	private static CollectionManager collectionManager;

	@Context
	private ServletContext servletContext;

	@Path("/alive")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String isAlive() {		
		return "{\"status\":\"LIVE\"}";
	}
	
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String listCollectionsJSON() {
		String json=null;
		try {
			ResultSet rs = getCollectionManager(servletContext).listAll();

			StringBuffer json_ld = new StringBuffer();
            json_ld.append("[\n");
            boolean first=true;
            while (rs.hasNext()) {
            	        if(!first)
            	        	json_ld.append(",");	
                        QuerySolution row = rs.nextSolution();
                        json_ld.append("\n\"" + row.getResource("collection").getURI()+"\""); 
                        first=false;
            }
            json_ld.append("\n]\n");
            return json_ld.toString();
			
		} catch (RuntimeException r) {

		}
		return json;
	}

	@Path("/example")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String listCollectionsSample() {
		Model model = ModelFactory.createDefaultModel();
		Resource ctype = model.createResource(BuildingDataOntology.Collections.Collection);
		Resource c1 = model.createResource(BuildingDataOntology.Collections.Collection+"/id1");
		Resource c2 = model.createResource(BuildingDataOntology.Collections.Collection+"/id2");
		Resource c3 = model.createResource(BuildingDataOntology.Collections.Collection+"/id3");
		c1.addProperty(RDF.type, ctype);
		c2.addProperty(RDF.type, ctype);
		c3.addProperty(RDF.type, ctype);
		
		JenaJSONLD.init();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		model.write(os, "JSON-LD");
		try {
			return new String(os.toByteArray(),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	@Path("/{guid}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getCollectionResourceJSON(@PathParam("guid") String collection_guid) {
		try {
			Resource collection = getCollectionManager(servletContext).getResource(collection_guid);
			StringBuffer json_ld = new StringBuffer();
            json_ld.append("\n\"@id\":"+BuildingDataOntology.Collections.Collection+"/"+collection_guid+",");
            Resource name_property = ResourceFactory.createResource(BuildingDataOntology.Collections.name); 
            Resource name=collection.getPropertyResourceValue((Property) name_property);
            if (name != null) {
                json_ld.append("\n\"name\":"+name.getLocalName()+",");
            }
            json_ld.append("\n\"hasDataSources\":");
            json_ld.append("[\n");
              
            ResultSet rs = getCollectionManager(servletContext).get(collection_guid);
            boolean first=true;
            while (rs.hasNext()) {
            	        if(!first)
            	        	json_ld.append(",");	
                        QuerySolution row = rs.nextSolution();
                        json_ld.append("\n\"" + row.getResource("ds").getURI()+"\""); 
                        first=false;
            }
            json_ld.append("\n]\n");            
            json_ld.append("\n}\n");
            return json_ld.toString();
		} catch (RuntimeException r) {

		}
		return null;
	}

	@Path("/{guid}")
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public String createCollectionJSON(@PathParam("guid") String collection_guid, @QueryParam("name") String name) {
		try {
			getCollectionManager(servletContext).create(collection_guid, name);
		} catch (RuntimeException r) {

		}
		return "";
	}

	@Path("/{guid}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public String deleteCollectionJSON(@PathParam("guid") String collection_guid) {
		try {
			getCollectionManager(servletContext).delete(collection_guid);
		} catch (RuntimeException r) {

		}
		return "";
	}
	
	private static CollectionManager getCollectionManager(
			ServletContext servletContext) {
		if (collectionManager == null) {
			try {
				Model model = AppManager.getJenaProvider(servletContext)
						.openDefaultModel();
				collectionManager = new CollectionManager(model);
			} catch (Exception e) {
				throw new RuntimeException("Could not get Jena model: "
						+ e.getMessage(), e);
			}
		}
		return collectionManager;

	}

}