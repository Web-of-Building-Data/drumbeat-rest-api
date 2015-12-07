package fi.aalto.cs.drumbeat.rest.api;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;

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

import com.github.jsonldjava.jena.JenaJSONLD;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.aalto.cs.drumbeat.rest.accessory.HTMLPrettyPrinting;
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

	@Path("/url")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getURL(@Context HttpServletRequest httpRequest) {
		String ret = "";
		try {

			ret = "http://" + httpRequest.getLocalAddr() + ":" + httpRequest.getLocalPort() + "/";

		} catch (Exception e) {
			return "{\"Status\":\"ERROR in URL \"}";
		}

		return "{\"requested url beginning\":" + ret + "\"}";
	}

	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String listHTML() {
		
		
		Model m = ModelFactory.createDefaultModel();
		try {
			if (!getManager(servletContext).listAll(m))
				return "<HTML><BODY>Status:\"No collections\"</BODY></HTML>";
		} catch (Exception e) {
			return "{\"Status\":\"ERROR: Check that the RDF store is started: cd /etc/init.d;sudo sh virtuoso start \"}";
		}

		return HTMLPrettyPrinting.prettyPrinting(m);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String listJSON_LD() {
		Model m = ModelFactory.createDefaultModel();

		try {
			try {
				if (!getManager(servletContext).listAll(m))
					return "{\"Status\":\"No collections\"}";
			} catch (Exception e) {
				return "{\"Status\":\"ERROR: Check that the RDF store is started: cd /etc/init.d;sudo sh virtuoso start \"}";
			}

			JenaJSONLD.init();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			m.write(os, "JSON-LD");
			try {
				return new String(os.toByteArray(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				return "{\"Status\":\"ERROR:" + e.getMessage() + "\"}";
			}
		} catch (Exception e) {
			return "{\"Status\":\"ERROR:" + e.getMessage() + "\"}";
		}

	}

	@GET
	@Produces("text/turtle")
	public String listTurtle() {
		Model m = ModelFactory.createDefaultModel();

		try {
			try{
			if (!getManager(servletContext).listAll(m))
				return "{\"Status\":\"No collections\"}";
			} catch (Exception e) {
				return "{\"Status\":\"ERROR: Check that the RDF store is started: cd /etc/init.d;sudo sh virtuoso start \"}";
			}

			JenaJSONLD.init();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			m.write(os, "TURTLE");
			try {
				return new String(os.toByteArray(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				return "{\"Status\":\"ERROR:" + e.getMessage() + "\"}";
			}
		} catch (Exception e) {
			return "{\"Status\":\"ERROR:" + e.getMessage() + "\"}";
		}

	}

	@GET
	@Produces("application/rdf+xml")
	public String listRDF() {
		Model m = ModelFactory.createDefaultModel();

		try {
			try{
			if (!getManager(servletContext).listAll(m))
				return "{\"Status\":\"No collections\"}";
			} catch (Exception e) {
				return "{\"Status\":\"ERROR: Check that the RDF store is started: cd /etc/init.d;sudo sh virtuoso start \"}";
			}

			JenaJSONLD.init();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			m.write(os, "RDF/XML");
			try {
				return new String(os.toByteArray(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				return "{\"Status\":\"ERROR:" + e.getMessage() + "\"}";
			}
		} catch (Exception e) {
			return "{\"Status\":\"ERROR:" + e.getMessage() + "\"}";
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
		return HTMLPrettyPrinting.prettyPrinting(model);
	}

	@Path("/example")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String listSampleJSON_LD() {
		Model model = listSample();
		JenaJSONLD.init();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		model.write(os, "JSON-LD");
		try {
			return new String(os.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "{\"Status\":\"ERROR:" + e.getMessage() + "\"}";
		}
	}

	@Path("/example")
	@GET
	@Produces("text/turtle")
	public String listSampleTurtle() {
		Model model = listSample();

		JenaJSONLD.init();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		model.write(os, "TURTLE");
		try {
			return new String(os.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "{\"Status\":\"ERROR:" + e.getMessage() + "\"}";
		}
	}

	@Path("/example")
	@GET
	@Produces("application/rdf+xml")
	public String listSampleRDF() {
		Model model = listSample();

		JenaJSONLD.init();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		model.write(os, "RDF/XML");
		try {
			return new String(os.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "{\"Status\":\"ERROR:" + e.getMessage() + "\"}";
		}
	}

	@Path("/{collectionname}")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String getHTML(@Context HttpServletRequest httpRequest,@PathParam("collectionname") String collectionname) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try{
		if (!getManager(servletContext).get(collectionname, m))
			return "<HTML><BODY>Status:\"The ID does not exists\"</BODY></HTML>";
		} catch (Exception e) {
			return "{\"Status\":\"ERROR: Check that the RDF store is started: cd /etc/init.d;sudo sh virtuoso start \"}";
		}

		return HTMLPrettyPrinting.prettyPrinting(m);
	}

	@Path("/{collectionname}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getJSON(@Context HttpServletRequest httpRequest,@PathParam("collectionname") String collectionname) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try{
		if (!getManager(servletContext).get(collectionname, m))
			return "{\"Status\":\"The ID does not exists\"}";
		} catch (Exception e) {
			return "{\"Status\":\"ERROR: Check that the RDF store is started: cd /etc/init.d;sudo sh virtuoso start \"}";
		}

		JenaJSONLD.init();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		m.write(os, "JSON-LD");
		try {
			return new String(os.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "{\"Status\":\"ERROR:" + e.getMessage() + "\"}";
		}
	}

	@Path("/{collectionname}")
	@GET
	@Produces("text/turtle")
	public String getTURTLE(@Context HttpServletRequest httpRequest,@PathParam("collectionname") String collectionname) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try{
		if (!getManager(servletContext).get(collectionname, m))
			return "{\"Status\":\"The ID does not exists\"}";
		} catch (Exception e) {
			return "{\"Status\":\"ERROR: Check that the RDF store is started: cd /etc/init.d;sudo sh virtuoso start \"}";
		}

		JenaJSONLD.init();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		m.write(os, "TURTLE");
		try {
			return new String(os.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "{\"Status\":\"ERROR:" + e.getMessage() + "\"}";
		}
	}

	@Path("/{collectionname}")
	@GET
	@Produces("application/rdf+xml")
	public String getRDF(@Context HttpServletRequest httpRequest,@PathParam("collectionname") String collectionname) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try{
		if (!getManager(servletContext).get(collectionname, m))
			return "{\"Status\":\"The ID does not exists\"}";
		} catch (Exception e) {
			return "{\"Status\":\"ERROR: Check that the RDF store is started: cd /etc/init.d;sudo sh virtuoso start \"}";
		}

		JenaJSONLD.init();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		m.write(os, "RDF/XML");
		try {
			return new String(os.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "{\"Status\":\"ERROR:" + e.getMessage() + "\"}";
		}
	}

	@Path("/{collectionname}")
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public String createJSON(@Context HttpServletRequest httpRequest,@PathParam("collectionname") String collectionname) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		try {
			getManager(servletContext).create(collectionname);
		} catch (Exception e) {
			return "{\"Status\":\"ERROR: Check that the RDF store is started: cd /etc/init.d;sudo sh virtuoso start \"}";
		}

		return "{\"Status\":\"Done\"}";
	}

	@Path("/{collectionname}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public String deleteJSON(@Context HttpServletRequest httpRequest,@PathParam("collectionname") String collectionname) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		try {
			getManager(servletContext).delete(collectionname);
		} catch (Exception e) {
			return "{\"Status\":\"ERROR: Check that the RDF store is started: cd /etc/init.d;sudo sh virtuoso start \"}";
		}

		return "{\"Status\":\"Done\"}";
	}

	private static CollectionManager getManager(ServletContext servletContext) {
		if (collectionManager == null) {
			try {
				Model model = DrumbeatApplication.getInstance().getJenaProvider().openDefaultModel();
				collectionManager = new CollectionManager(model);
			} catch (Exception e) {
				throw new RuntimeException("Could not get Jena model: " + e.getMessage(), e);
			}
		}
		return collectionManager;
	}

}