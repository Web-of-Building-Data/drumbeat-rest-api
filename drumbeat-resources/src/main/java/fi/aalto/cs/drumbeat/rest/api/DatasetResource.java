package fi.aalto.cs.drumbeat.rest.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletContext;
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

import fi.aalto.cs.drumbeat.rest.accessory.HTMLPrettyPrinting;
import fi.aalto.cs.drumbeat.rest.managers.DatasetManager;

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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
//import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
//import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.FormDataParam;

@Path("/datasets")
public class DatasetResource {
	
	private static final String DATASET_NAME_FORMAT = "%s_%s_%s";

	private static final Logger logger = Logger.getLogger(DatasetResource.class);
	
	private static DatasetManager dataSetManager;

	@Context
	private ServletContext servletContext;

	@Path("/alive")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String isAlive() {
		return "{\"status\":\"LIVE\"}";
	}
	
	
	@Path("/{collectionname}/{datasourcename}")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String listHTML(@PathParam("collectionname") String collectionname,@PathParam("datasourcename") String datasourcename) {
		Model m = ModelFactory.createDefaultModel();
		if(!getDatasetManager(servletContext).listAll(m,collectionname,datasourcename))
			   return "<HTML><BODY>Status:\"No datasources\"</BODY></HTML>";
			
		return HTMLPrettyPrinting.prettyPrinting(m);	
	}

	@Path("/{collectionname}/{datasourcename}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String listJSON_LD(@PathParam("collectionname") String collectionname,@PathParam("datasourcename") String datasourcename) {
		Model m = ModelFactory.createDefaultModel();
		
		try {
			if(!getDatasetManager(servletContext).listAll(m,collectionname,datasourcename))
			   return "{\"Status\":\"No datasources\"}";
			
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
	

	@Path("/{collectionname}/{datasourcename}")
	@GET
	@Produces("text/turtle")
	public String listTurtle(@PathParam("collectionname") String collectionname,@PathParam("datasourcename") String datasourcename) {
		Model m = ModelFactory.createDefaultModel();
		
		try {
			if(!getDatasetManager(servletContext).listAll(m,collectionname,datasourcename))
			   return "{\"Status\":\"No datasources\"}";
			
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
	
	@Path("/{collectionname}/{datasourcename}")
	@GET
	@Produces("application/rdf+xml")
	public String listRDF(@PathParam("collectionname") String collectionname,@PathParam("datasourcename") String datasourcename) {
		Model m = ModelFactory.createDefaultModel();
		
		try {
			if(!getDatasetManager(servletContext).listAll(m,collectionname,datasourcename))
			   return "{\"Status\":\"No datasources\"}";
			
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
	
	
	@Path("/{collectionname}/{datasourcename}/{datasetname}")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String getHTML(@PathParam("collectionname") String collectionname,@PathParam("datasourcename") String datasourcename,@PathParam("datasetname") String datasetname) {
		Model m = ModelFactory.createDefaultModel();
		if(!getDatasetManager(servletContext).get(m,collectionname, datasourcename, datasetname))
			   return "<HTML><BODY>Status:\"The ID does not exists\"</BODY></HTML>";
		return HTMLPrettyPrinting.prettyPrinting(m);	
	}

	@Path("/{collectionname}/{datasourcename}/{datasetname}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getJSON(@PathParam("collectionname") String collectionname,@PathParam("datasourcename") String datasourcename,@PathParam("datasetname") String datasetname) {
		Model m = ModelFactory.createDefaultModel();
		if(!getDatasetManager(servletContext).get(m,collectionname, datasourcename, datasetname))
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

	@Path("/{collectionname}/{datasourcename}/{datasetname}")
	@GET
	@Produces("text/turtle")
	public String getTURTLE(@PathParam("collectionname") String collectionname,@PathParam("datasourcename") String datasourcename,@PathParam("datasetname") String datasetname) {
		Model m = ModelFactory.createDefaultModel();
		if(!getDatasetManager(servletContext).get(m,collectionname, datasourcename, datasetname))
			   return "{\"Status\":\"The ID does not exists\"}";

		JenaJSONLD.init();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		m.write(os, "TURTLE");
		try {
			return new String(os.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "{\"Status\":\"ERROR:" + e.getMessage() + "\"}";
		}
	}
	
	@Path("/{collectionname}/{datasourcename}/{datasetname}")
	@GET
	@Produces("application/rdf+xml")
	public String getRDF(@PathParam("collectionname") String collectionname,@PathParam("datasourcename") String datasourcename,@PathParam("datasetname") String datasetname) {
		Model m = ModelFactory.createDefaultModel();
		if(!getDatasetManager(servletContext).get(m,collectionname, datasourcename, datasetname))
			   return "{\"Status\":\"The ID does not exists\"}";

		JenaJSONLD.init();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		m.write(os, "RDF/XML");
		try {
			return new String(os.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "{\"Status\":\"ERROR:" + e.getMessage() + "\"}";
		}
	}
	
	
	@Path("/{collectionname}/{datasourcename}/{datasetname}")
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public String createJSON(@PathParam("collectionname") String collectionname,@PathParam("datasourcename") String datasourcename,@PathParam("datasetname") String datasetname) {
		try {
			getDatasetManager(servletContext).create(collectionname, datasourcename,datasetname);
		} catch (RuntimeException r) {
			r.printStackTrace();
			return "{\"Status\":\"ERROR:" + r.getMessage() + " collectionname:" +  collectionname+ " datasourcename:" + datasourcename+ " datasetname:" + datasetname + "\"}";
		}
		return "{\"Status\":\"Done\"}";
	}

	@Path("/{collectionname}/{datasourcename}/{datasetname}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public String deleteJSON(@PathParam("collectionname") String collectionname,@PathParam("datasourcename") String datasourcename,@PathParam("datasetname") String datasetname) {
		try {
			getDatasetManager(servletContext).delete(collectionname, datasourcename,datasetname);
		} catch (RuntimeException r) {
			r.printStackTrace();
			return "{\"Status\":\"ERROR:" + r.getMessage() + " collectionname:" +  collectionname+ " datasourcename:" + datasourcename+ " datasetname:" + datasetname + "\"}";
		}
		return "{\"Status\":\"Done\"}";
	}
	

	private static DatasetManager getDatasetManager(
			ServletContext servletContext) {
		if (dataSetManager == null) {
			try {
				Model model = ApplicationConfig.getJenaProvider()
						.openDefaultModel();
				dataSetManager = new DatasetManager(model);
			} catch (Exception e) {
				throw new RuntimeException("Could not get Jena model: " + e.getMessage(), e);
			}
		}
		return dataSetManager;

	}
	
	@Path("/{collectionId}/{dataSourceId}/{dataSetId}/importServerFile")
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public Response importServerFile(
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@PathParam("dataSetId") String dataSetId,
			@FormDataParam("filePath") String filePath)
	{
		try {
			
			String dataSetName = String.format(DATASET_NAME_FORMAT, collectionId, dataSourceId, dataSetId);
			
			String message = String.format("ImportServerFile: Dataset=%s, ServerFilePath=%s", dataSetName, filePath);			
			logger.info(message);

			InputStream inputStream = new FileInputStream(filePath);
			DatasetManager dataSetManager = new DatasetManager(null);			
			Model jenaModel = ApplicationConfig.getJenaProvider().openModel(dataSetName);			
			dataSetManager.importData(servletContext, inputStream, jenaModel);
			
			return Response.ok(message).build();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Response.serverError().entity(e).build();
		}
	}
	
	@Path("/{collectionId}/{dataSourceId}/{dataSetId}/importUrl")
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public Response importUrl(
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@PathParam("dataSetId") String dataSetId,
			@FormDataParam("url") String url)
	{
		try {
			
			String dataSetName = String.format(DATASET_NAME_FORMAT, collectionId, dataSourceId, dataSetId);
			
			String message = String.format("ImportUrl: Dataset=%s, Url=%s", dataSetName, url);			
			logger.info(message);

			InputStream inputStream = new URL(url).openStream();
			DatasetManager dataSetManager = new DatasetManager(null);			
			Model jenaModel = ApplicationConfig.getJenaProvider().openModel(dataSetName);			
			dataSetManager.importData(servletContext, inputStream, jenaModel);
			
			return Response.ok(message).build();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Response.serverError().entity(e).build();
		}
	}

	@Path("/{collectionId}/{dataSourceId}/{dataSetId}/importContent")
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public Response importContent(
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@PathParam("dataSetId") String dataSetId,
			@FormDataParam("content") String content)
	{
		try {
			
			String dataSetName = String.format(DATASET_NAME_FORMAT, collectionId, dataSourceId, dataSetId);
			
			String message = String.format("ImportContent: Dataset=%s, Content=%s", dataSetName, content);			
			logger.info(message);

			InputStream inputStream = new ByteArrayInputStream(content.getBytes());
			DatasetManager dataSetManager = new DatasetManager(null);			
			Model jenaModel = ApplicationConfig.getJenaProvider().openModel(dataSetName);			
			dataSetManager.importData(servletContext, inputStream, jenaModel);
			
			return Response.ok(message).build();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Response.serverError().entity(e).build();
		}
	}

	
	@Path("/{collectionId}/{dataSourceId}/{dataSetId}/importClientFile")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	public Response importClientFile(
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@PathParam("dataSetId") String dataSetId,
			@FormDataParam("file") InputStream inputStream,
	        @FormDataParam("file") FormDataContentDisposition fileDetail)
	{
		try {
			
			String dataSetName = String.format(DATASET_NAME_FORMAT, collectionId, dataSourceId, dataSetId);
			
			String message = String.format("ImportContent: Dataset=%s, FileName=%s", dataSetName, fileDetail.getFileName());			
			logger.info(message);

			DatasetManager dataSetManager = new DatasetManager(null);			
			Model jenaModel = ApplicationConfig.getJenaProvider().openModel(dataSetName);			
			dataSetManager.importData(servletContext, inputStream, jenaModel);
			
			return Response.ok(message).build();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Response.serverError().entity(e).build();
		}
	}
	
	
}