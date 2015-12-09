package fi.aalto.cs.drumbeat.rest.api;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataParam;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.aalto.cs.drumbeat.rest.accessory.PrettyPrinting;
import fi.aalto.cs.drumbeat.rest.application.DrumbeatApplication;
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
public class CollectionResource extends AbstractResource{
	private static CollectionManager manager;

	@Context
	private ServletContext servletContext;

	@Path("/alive")
	@GET
	public String isAlive() {
		return "{\"status\":\"LIVE\"}";
	}

	@Path("/url")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getURL(@Context HttpServletRequest httpRequest) {
		String ret = "";
		try {

			ret = "http://" + httpRequest.getLocalAddr() + ":" + httpRequest.getLocalPort() + "/";

		} catch (Exception e) {
			return PrettyPrinting.formatErrorJSON("ERROR in URL");
		}

		return "{\"requested url beginning\":" + ret + "\"}";
	}

	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String listHTML() {
		
		
		Model m = ModelFactory.createDefaultModel();
		try {
			if (!getManager().listAll2Model(m))
				return PrettyPrinting.formatErrorHTML("No collections");
		} catch (Exception e) {
			return PrettyPrinting.formatErrorHTML("Check that the RDF store is started: "+e.getMessage());
		}

		return PrettyPrinting.prettyPrintingHTML(m);
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String listTXT() {
		Model m = ModelFactory.createDefaultModel();
		try {
			if (!getManager().listAll2Model(m))
				return PrettyPrinting.formatErrorTXT("No collections");
		} catch (Exception e) {
			return PrettyPrinting.formatErrorTXT("Check that the RDF store is started: "+e.getMessage());
		}

		return PrettyPrinting.prettyPrintingPlain(m);
	}

	@GET
	@Produces({MediaType.APPLICATION_JSON,"application/ld+json"})
	public String listJSON() {
		Model m = ModelFactory.createDefaultModel();

		try {
			try {
				if (!getManager().listAll2Model(m))
					return PrettyPrinting.formatErrorJSON("No collections");
			} catch (Exception e) {
				return PrettyPrinting.formatErrorJSON("Check that the RDF store is started: "+e.getMessage());
			}

			return model2JSON_LD(m);
		} catch (Exception e) {
			return PrettyPrinting.formatErrorJSON(e.getMessage());
		}

	}


	@GET
	@Produces("text/turtle")
	public String listTURTLE() {
		Model m = ModelFactory.createDefaultModel();

		try {
			try{
			if (!getManager().listAll2Model(m))
				return PrettyPrinting.formatErrorTURTLE("No collections");
			} catch (Exception e) {
				return PrettyPrinting.formatErrorTURTLE("Check that the RDF store is started: "+e.getMessage());
			}

			return model2TURTLE(m);
		} catch (Exception e) {
			return PrettyPrinting.formatErrorTURTLE(e.getMessage());
		}

	}

	@GET
	@Produces("application/rdf+xml")
	public String listRDF() {
		Model m = ModelFactory.createDefaultModel();

		try {
			try{
			if (!getManager().listAll2Model(m))
				return PrettyPrinting.formatErrorRDF("No collections");
			} catch (Exception e) {
				return PrettyPrinting.formatErrorRDF("Check that the RDF store is started: "+e.getMessage());
			}

			return model2RDF(m);
		} catch (Exception e) {
			return PrettyPrinting.formatErrorRDF(e.getMessage());
		}

	}

	private Model listSample() {
		Model model = ModelFactory.createDefaultModel();
		Resource ctype = model.createResource(BuildingDataOntology.Collections.Collection);
		Resource c1 = model.createResource(BuildingDataOntology.Collections.Collection + "/nonexisting_sample_1");
		Resource c2 = model.createResource(BuildingDataOntology.Collections.Collection + "/nonexisting_sample_2");
		Resource c3 = model.createResource(BuildingDataOntology.Collections.Collection + "/nonexisting_sample_3");
		c1.addProperty(RDF.type, ctype);
		c2.addProperty(RDF.type, ctype);
		c3.addProperty(RDF.type, ctype);
		return model;
	}

	@Path("/example")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String listSampleHTML() {
		Model model = listSample();
		return PrettyPrinting.prettyPrintingHTML(model);
	}
	
	@Path("/example")
	@GET
	@Produces({MediaType.APPLICATION_JSON,"application/ld+json"})
	public String listSampleJSON() {
		Model m = listSample();
		return model2JSON_LD(m);
	}

	@Path("/example")
	@GET
	@Produces("text/turtle")
	public String listSampleTURTLE() {
		Model m = listSample();
		return model2TURTLE(m);
	}

	@Path("/example")
	@GET
	@Produces("application/rdf+xml")
	public String listSampleRDF() {
		Model m = listSample();
		return model2RDF(m);
	}

	@Path("/{collectionid}")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String getHTML(@Context HttpServletRequest httpRequest,@PathParam("collectionid") String collectionid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try{
		if (!getManager().get2Model(m, collectionid))
			return PrettyPrinting.formatErrorHTML("The ID does not exists");
		} catch (Exception e) {
			return PrettyPrinting.formatErrorHTML("Check that the RDF store is started: "+e.getMessage());
		}

		return PrettyPrinting.prettyPrintingHTML(m);
	}

	
	@Path("/{collectionid}")
	@GET
	@Produces({MediaType.APPLICATION_JSON,"application/ld+json"})
	public String getJSON(@Context HttpServletRequest httpRequest,@PathParam("collectionid") String collectionid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try{
		if (!getManager().get2Model(m, collectionid))
			return PrettyPrinting.formatErrorJSON("The ID does not exists");
		} catch (Exception e) {
			return PrettyPrinting.formatErrorJSON("Check that the RDF store is started: "+e.getMessage());
		}

		return model2JSON_LD(m);
	}

	@Path("/{collectionid}")
	@GET
	@Produces("text/turtle")
	public String getTURTLE(@Context HttpServletRequest httpRequest,@PathParam("collectionid") String collectionid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try{
		if (!getManager().get2Model(m, collectionid))
			return PrettyPrinting.formatErrorTURTLE("The ID does not exists");
		} catch (Exception e) {
			return PrettyPrinting.formatErrorTURTLE("Check that the RDF store is started: "+e.getMessage());
		}

		return model2TURTLE(m);
	}

	@Path("/{collectionid}")
	@GET
	@Produces("application/rdf+xml")
	public String getRDF(@Context HttpServletRequest httpRequest,@PathParam("collectionid") String collectionid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try{
		if (!getManager().get2Model(m, collectionid))
			return PrettyPrinting.formatErrorRDF("The ID does not exists");
		} catch (Exception e) {
			return PrettyPrinting.formatErrorRDF("Check that the RDF store is started: "+e.getMessage());
		}

		return model2RDF(m);
	}

	@Path("/{collectionid}")
	@PUT
	public String create(@Context HttpServletRequest httpRequest,@PathParam("collectionid") String collectionid,
			@FormDataParam("name") String name) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		try {
			getManager().create(collectionid,name);
		} catch (Exception e) {
			return PrettyPrinting.formatErrorJSON("Check that the RDF store is started: "+e.getMessage());
		}

		return "{\"Status\":\"Done\"}";
	}

	@Path("/{collectionid}")
	@DELETE
	public String delete(@Context HttpServletRequest httpRequest,@PathParam("collectionid") String collectionid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		try {
			getManager().delete(collectionid);
		} catch (Exception e) {
			return PrettyPrinting.formatErrorJSON("Check that the RDF store is started: "+e.getMessage());
		}

		return "{\"Status\":\"Done\"}";
	}

	@Override
	public CollectionManager getManager() {
		if (manager == null) {
			try {
				Model model = DrumbeatApplication.getInstance().getJenaProvider().openDefaultModel();
				manager = new CollectionManager(model);
			} catch (Exception e) {
				throw new RuntimeException("Could not get Jena model: " + e.getMessage(), e);
			}
		}
		return manager;
	}

}