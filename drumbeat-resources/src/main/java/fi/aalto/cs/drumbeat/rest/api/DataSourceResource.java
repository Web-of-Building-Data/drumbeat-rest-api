package fi.aalto.cs.drumbeat.rest.api;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

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

import com.github.jsonldjava.jena.JenaJSONLD;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import fi.aalto.cs.drumbeat.rest.accessory.PrettyPrinting;
import fi.aalto.cs.drumbeat.rest.application.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.managers.DataSourceManager;

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

@Path("/datasources")
public class DataSourceResource extends AbstractResource {
	private static DataSourceManager manager;

	@Context
	private ServletContext servletContext;

	@Path("/alive")
	@GET
	public String isAlive() {
		return "{\"status\":\"LIVE\"}";
	}
	
	
	@Path("/{collectionid}")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String listHTML(@PathParam("collectionid") String collectionid) {
		Model m = ModelFactory.createDefaultModel();
		try
		{
		if(!getManager().listAll2Model(m,collectionid))
			   return "<HTML><BODY>Status:\"No datasources\"</BODY></HTML>";
		} catch (Exception e) {
			return PrettyPrinting.formatErrorHTML("Check that the RDF store is started: "+e.getMessage());
		}

			
		return PrettyPrinting.prettyPrintingHTML(m);	
	}

	@Path("/{collectionid}")
	@GET
	@Produces({MediaType.APPLICATION_JSON,"application/ld+json"})
	public String listJSON_LD(@PathParam("collectionid") String collectionid) {
		Model m = ModelFactory.createDefaultModel();
		
		try {
			try{
			if(!getManager().listAll2Model(m,collectionid))
			   return "{\"Status\":\"No datasources\"}";
			} catch (Exception e) {
				return PrettyPrinting.formatErrorJSON("Check that the RDF store is started: "+e.getMessage());
			}

			return model2JSON_LD(m);
		} catch (Exception e) {
			return "{\"Status\":\"ERROR:" + e.getMessage() + "\"}";
		}
		
	}
	

	@Path("/{collectionid}")
	@GET
	@Produces("text/turtle")
	public String listTurtle(@PathParam("collectionid") String collectionid) {
		Model m = ModelFactory.createDefaultModel();
		
		try {
			try{
			if(!getManager().listAll2Model(m,collectionid))
			   return "{\"Status\":\"No datasources\"}";
			} catch (Exception e) {
				return PrettyPrinting.formatErrorTURTLE("Check that the RDF store is started: "+e.getMessage());
			}

			return model2TURTLE(m);
		} catch (Exception e) {
			return "{\"Status\":\"ERROR:" + e.getMessage() + "\"}";
		}
		
	}
	
	@Path("/{collectionid}")
	@GET
	@Produces("application/rdf+xml")
	public String listRDF(@PathParam("collectionid") String collectionid) {
		Model m = ModelFactory.createDefaultModel();
		
		try {
			try{
			if(!getManager().listAll2Model(m,collectionid))
			   return "{\"Status\":\"No datasources\"}";
			} catch (Exception e) {
				return PrettyPrinting.formatErrorRDF("Check that the RDF store is started: "+e.getMessage());
			}

			return model2RDF(m);
		} catch (Exception e) {
			return "{\"Status\":\"ERROR:" + e.getMessage() + "\"}";
		}
		
	}
	
	
	@Path("/{collectionid}/{datasourceid}")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String getHTML(@Context HttpServletRequest httpRequest,@PathParam("collectionid") String collectionid,@PathParam("datasourceid") String datasourceid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try{
		if(!getManager().get2Model(m,collectionid, datasourceid))
			   return "<HTML><BODY>Status:\"The ID does not exists\"</BODY></HTML>";
		} catch (Exception e) {
			return PrettyPrinting.formatErrorHTML("Check that the RDF store is started: "+e.getMessage());
		}

		return PrettyPrinting.prettyPrintingHTML(m);	
	}

	@Path("/{collectionid}/{datasourceid}")
	@GET
	@Produces({MediaType.APPLICATION_JSON,"application/ld+json"})
	public String getJSON(@Context HttpServletRequest httpRequest,@PathParam("collectionid") String collectionid,@PathParam("datasourceid") String datasourceid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try{
		if(!getManager().get2Model(m,collectionid, datasourceid))
			   return "{\"Status\":\"The ID does not exists\"}";
		} catch (Exception e) {
			return PrettyPrinting.formatErrorJSON("Check that the RDF store is started: "+e.getMessage());
		}

		return model2JSON_LD(m);
	}

	@Path("/{collectionid}/{datasourceid}")
	@GET
	@Produces("text/turtle")
	public String getTURTLE(@Context HttpServletRequest httpRequest,@PathParam("collectionid") String collectionid,@PathParam("datasourceid") String datasourceid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try{
		if(!getManager().get2Model(m,collectionid, datasourceid))
			   return "{\"Status\":\"The ID does not exists\"}";
		} catch (Exception e) {
			return PrettyPrinting.formatErrorTURTLE("Check that the RDF store is started: "+e.getMessage());
		}

		return model2TURTLE(m);
	}
	
	@Path("/{collectionid}/{datasourceid}")
	@GET
	@Produces("application/rdf+xml")
	public String getRDF(@Context HttpServletRequest httpRequest,@PathParam("collectionid") String collectionid,@PathParam("datasourceid") String datasourceid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try{
		if(!getManager().get2Model(m,collectionid, datasourceid))
			   return "{\"Status\":\"The ID does not exists\"}";
		} catch (Exception e) {
			return PrettyPrinting.formatErrorRDF("Check that the RDF store is started: "+e.getMessage());
		}

		return model2RDF(m);
	}
	
	
	@Path("/{collectionid}/{datasourceid}")
	@PUT
	public String create(@Context HttpServletRequest httpRequest,@PathParam("collectionid") String collectionid,@PathParam("datasourceid") String datasourceid,@FormDataParam("name") String name) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		try {
			getManager().create(collectionid, datasourceid,name);
		} catch (Exception e) {
			return PrettyPrinting.formatErrorJSON("Check that the RDF store is started: "+e.getMessage());
		}

		return "{\"Status\":\"Done\"}";
	}

	@Path("/{collectionid}/{datasourceid}")
	@DELETE
	public String delete(@Context HttpServletRequest httpRequest,@PathParam("collectionid") String collectionid,@PathParam("datasourceid") String datasourceid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		try {
			getManager().delete(collectionid, datasourceid);
		} catch (Exception e) {
			return PrettyPrinting.formatErrorJSON("Check that the RDF store is started: "+e.getMessage());
		}

		return "{\"Status\":\"Done\"}";
	}
	
	@Override
	public DataSourceManager getManager() {
		if (manager == null) {
			try {
				Model model = DrumbeatApplication.getInstance().getJenaProvider().openDefaultModel();
				manager = new DataSourceManager(model);
			} catch (Exception e) {
				throw new RuntimeException("Could not get Jena model: " + e.getMessage(), e);
			}
		}
		return manager;
	}

}