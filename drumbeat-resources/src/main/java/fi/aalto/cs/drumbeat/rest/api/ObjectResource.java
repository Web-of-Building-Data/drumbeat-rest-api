package fi.aalto.cs.drumbeat.rest.api;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.github.jsonldjava.jena.JenaJSONLD;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import fi.aalto.cs.drumbeat.rest.accessory.PrettyPrinting;
import fi.aalto.cs.drumbeat.rest.application.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.managers.ObjectManager;

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

@Path("/object")
public class ObjectResource  extends AbstractResource{
	private static ObjectManager manager;

	@Context
	private ServletContext servletContext;

	@Path("/alive")
	@GET
	public String isAlive() {
		return "{\"status\":\"LIVE\"}";
	}

	@Path("/{collectionname}/{datasourcename}/{guid}")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String getHTML(@Context HttpServletRequest httpRequest,@PathParam("collectionname") String collectionname, @PathParam("datasourcename") String datasourcename, @PathParam("guid") String guid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try {
			if (!getManager().get2Model(m, collectionname, datasourcename, guid))
				return "<HTML><BODY>Status:\"The ID does not exists\"</BODY></HTML>";
		} catch (Exception e) {
			return PrettyPrinting.formatErrorHTML("Check that the RDF store is started: "+e.getMessage());
		}
		return PrettyPrinting.prettyPrintingHTML(m);
	}

	@Path("/{collectionname}/{datasourcename}/{guid}")
	@GET
	@Produces({MediaType.APPLICATION_JSON,"application/ld+json"})
	public String getJSON(@Context HttpServletRequest httpRequest,@PathParam("collectionname") String collectionname, @PathParam("datasourcename") String datasourcename, @PathParam("guid") String guid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try {
			if (!getManager().get2Model(m, collectionname, datasourcename, guid))
				return "{\"Status\":\"The ID does not exists\"}";
		} catch (Exception e) {
			return PrettyPrinting.formatErrorJSON("Check that the RDF store is started: "+e.getMessage());
		}

		return model2JSON_LD(m);
	}

	@Path("/{collectionname}/{datasourcename}/{guid}")
	@GET
	@Produces("text/turtle")
	public String getTURTLE(@Context HttpServletRequest httpRequest,@PathParam("collectionname") String collectionname, @PathParam("datasourcename") String datasourcename, @PathParam("guid") String guid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try {
			if (!getManager().get2Model(m, collectionname, datasourcename, guid))
				return "{\"Status\":\"The ID does not exists\"}";
		} catch (Exception e) {
			return PrettyPrinting.formatErrorTURTLE("Check that the RDF store is started: "+e.getMessage());
		}

		return model2TURTLE(m);
	}

	@Path("/{collectionname}/{datasourcename}/{guid}")
	@GET
	@Produces("application/rdf+xml")
	public String getRDF(@Context HttpServletRequest httpRequest,@PathParam("collectionname") String collectionname, @PathParam("datasourcename") String datasourcename, @PathParam("guid") String guid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try {
			if (!getManager().get2Model(m, collectionname, datasourcename, guid))
				return "{\"Status\":\"The ID does not exists\"}";
		} catch (Exception e) {
			return PrettyPrinting.formatErrorRDF("Check that the RDF store is started: "+e.getMessage());
		}

		return model2RDF(m);
	}

	@Path("/{collectionname}/{datasourcename}/{guid}/type")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String getTypeHTML(@Context HttpServletRequest httpRequest,@PathParam("collectionname") String collectionname, @PathParam("datasourcename") String datasourcename, @PathParam("guid") String guid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try {
			if (!getManager().getType2Model(m, collectionname, datasourcename, guid))
				return "<HTML><BODY>Status:\"The ID does not exists\"</BODY></HTML>";
		} catch (Exception e) {
			return PrettyPrinting.formatErrorHTML("Check that the RDF store is started: "+e.getMessage());
		}

		return PrettyPrinting.prettyPrintingHTML(m);
	}

	@Path("/{collectionname}/{datasourcename}/{guid}/type")
	@GET
	@Produces({MediaType.APPLICATION_JSON,"application/ld+json"})
	public String getTypeJSON(@Context HttpServletRequest httpRequest,@PathParam("collectionname") String collectionname, @PathParam("datasourcename") String datasourcename, @PathParam("guid") String guid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try {
			if (!getManager().getType2Model(m, collectionname, datasourcename, guid))
				return "{\"Status\":\"The ID does not exists\"}";
		} catch (Exception e) {
			return PrettyPrinting.formatErrorJSON("Check that the RDF store is started: "+e.getMessage());
		}

		return model2JSON_LD(m);
	}

	@Path("/{collectionname}/{datasourcename}/{guid}/type")
	@GET
	@Produces("text/turtle")
	public String getTypeTURTLE(@Context HttpServletRequest httpRequest,@PathParam("collectionname") String collectionname, @PathParam("datasourcename") String datasourcename, @PathParam("guid") String guid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try {
			if (!getManager().getType2Model(m, collectionname, datasourcename, guid))
				return "{\"Status\":\"The ID does not exists\"}";
		} catch (Exception e) {
			return PrettyPrinting.formatErrorTURTLE("Check that the RDF store is started: "+e.getMessage());
		}

		return model2TURTLE(m);
	}

	@Path("/{collectionname}/{datasourcename}/{guid}/type")
	@GET
	@Produces("application/rdf+xml")
	public String getTypeRDF(@Context HttpServletRequest httpRequest,@PathParam("collectionname") String collectionname, @PathParam("datasourcename") String datasourcename, @PathParam("guid") String guid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try {
			if (!getManager().getType2Model(m, collectionname, datasourcename, guid))
				return "{\"Status\":\"The ID does not exists\"}";
		} catch (Exception e) {
			return PrettyPrinting.formatErrorRDF("Check that the RDF store is started: "+e.getMessage());
		}

		return model2RDF(m);
	}

	@Override
	public ObjectManager getManager() {
		if (manager == null) {
			try {
				Model model = DrumbeatApplication.getInstance().getJenaProvider().openDefaultModel();
				manager = new ObjectManager(model);
			} catch (Exception e) {
				throw new RuntimeException("Could not get Jena model: " + e.getMessage(), e);
			}
		}
		return manager;
	}

}