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
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.aalto.cs.drumbeat.rest.managers.AppManager;
import fi.aalto.cs.drumbeat.rest.managers.CollectionManager;
import fi.aalto.cs.drumbeat.rest.managers.DataSourceManager;
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

@Path("/datasources")
public class DataSourceResource {

	private static DataSourceManager datasourceManager;

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
	public String listDataSourcesJSON() {
		Model m = ModelFactory.createDefaultModel();
		
		try {
			if(!getDataSourceManager(servletContext).listAll(m))
			   return "{\"Status\":\"No collections\"}";
			
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


	@Path("/{guid}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getDataSourceJSON(@PathParam("guid") String collection_guid) {
		Model m = ModelFactory.createDefaultModel();
		if(!getDataSourceManager(servletContext).get(collection_guid,m))
			   return "{\"Status\":\"The ID does not exists\"}";

		JenaJSONLD.init();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		m.write(os, "JSON-LD");
		try {
			return new String(os.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "{\"Status\":\"ERROR:" + e.getMessage() + "\"}";
		}
	}

	@Path("/{guid}")
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public String createDataSourceJSON(@PathParam("guid") String collection_guid, @QueryParam("name") String name) {
		try {
			getDataSourceManager(servletContext).create(collection_guid, name);
		} catch (RuntimeException r) {
			r.printStackTrace();
			return "{\"Status\":\"ERROR:" + r.getMessage() + " guid:" + collection_guid + " name:" + name + "\"}";
		}
		return "{\"Status\":\"Done\"}";
	}

	@Path("/{guid}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public String deleteDataSourceJSON(@PathParam("guid") String collection_guid) {
		try {
			getDataSourceManager(servletContext).delete(collection_guid);
		} catch (RuntimeException r) {

		}
		return "{\"Status\":\"Done\"}";
	}

	private static DataSourceManager getDataSourceManager(ServletContext servletContext) {
		if (datasourceManager == null) {
			try {
				Model model = AppManager.getJenaProvider(servletContext).openDefaultModel();
				datasourceManager = new DataSourceManager(model);
			} catch (Exception e) {
				throw new RuntimeException("Could not get Jena model: " + e.getMessage(), e);
			}
		}
		return datasourceManager;

	}

}