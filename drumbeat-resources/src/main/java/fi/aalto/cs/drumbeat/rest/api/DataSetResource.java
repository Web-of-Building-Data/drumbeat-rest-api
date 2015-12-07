package fi.aalto.cs.drumbeat.rest.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.jsonldjava.jena.JenaJSONLD;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import fi.aalto.cs.drumbeat.rest.accessory.HTMLPrettyPrinting;
import fi.aalto.cs.drumbeat.rest.application.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.managers.DataSetManager;


/*
 The MIT License (MIT)

 Copyright (c) 2015 Jyrki Oraskari
 Copyright (c) 2015 Nam Vu Hoang

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

@Path("/datasets")
public class DataSetResource {
	
	public static final String DATA_TYPE_IFC = "IFC";
	public static final String DATA_TYPE_RDF = "RDF";
	public static final String DATA_TYPE_CSV = "CSV";
	
	private static final String DATASET_NAME_FORMAT = "%s_%s_%s";	

	private static final Logger logger = Logger.getLogger(DataSetResource.class);
	
	private static DataSetManager dataSetManager;

	@Context
	private ServletContext servletContext;

	@Path("/alive")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String isAlive() {
		return "{\"status\":\"LIVE\"}";
	}	
	
	@Path("/alive")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String isAlivePost() {
		return "{\"status\":\"LIVE\"}";
	}	

	
	@Path("/{collectionname}/{datasourcename}")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String listHTML(@PathParam("collectionname") String collectionname,@PathParam("datasourcename") String datasourcename) {
		Model m = ModelFactory.createDefaultModel();
		if(!getManager(servletContext).listAll(m,collectionname,datasourcename))
			   return "<HTML><BODY>Status:\"No datasources\"</BODY></HTML>";
			
		return HTMLPrettyPrinting.prettyPrinting(m);	
	}

	@Path("/{collectionname}/{datasourcename}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String listJSON_LD(@PathParam("collectionname") String collectionname,@PathParam("datasourcename") String datasourcename) {
		Model m = ModelFactory.createDefaultModel();
		
		try {
			if(!getManager(servletContext).listAll(m,collectionname,datasourcename))
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
			if(!getManager(servletContext).listAll(m,collectionname,datasourcename))
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
			if(!getManager(servletContext).listAll(m,collectionname,datasourcename))
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
		if(!getManager(servletContext).get(m,collectionname, datasourcename, datasetname))
			   return "<HTML><BODY>Status:\"The ID does not exists\"</BODY></HTML>";
		return HTMLPrettyPrinting.prettyPrinting(m);	
	}

	@Path("/{collectionname}/{datasourcename}/{datasetname}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getJSON(@PathParam("collectionname") String collectionname,@PathParam("datasourcename") String datasourcename,@PathParam("datasetname") String datasetname) {
		Model m = ModelFactory.createDefaultModel();
		if(!getManager(servletContext).get(m,collectionname, datasourcename, datasetname))
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
		if(!getManager(servletContext).get(m,collectionname, datasourcename, datasetname))
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
		if(!getManager(servletContext).get(m,collectionname, datasourcename, datasetname))
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
			getManager(servletContext).create(collectionname, datasourcename,datasetname);
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
			getManager(servletContext).delete(collectionname, datasourcename,datasetname);
		} catch (RuntimeException r) {
			r.printStackTrace();
			return "{\"Status\":\"ERROR:" + r.getMessage() + " collectionname:" +  collectionname+ " datasourcename:" + datasourcename+ " datasetname:" + datasetname + "\"}";
		}
		return "{\"Status\":\"Done\"}";
	}
	
	
	@Path("/{collectionId}/{dataSourceId}/{dataSetId}/uploadServerFile")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadServerFile(
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@PathParam("dataSetId") String dataSetId,
			@FormParam("dataType") String dataType,
			@FormParam("filePath") String filePath)
	{
		String dataSetName = getDataSetName(collectionId, dataSourceId, dataSetId);
		logger.info(String.format("UploadServerFile: DataSet=%s, ServerFilePath=%s", dataSetName, filePath));
		
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(filePath);
		} catch (FileNotFoundException e) {			
			return
					Response
						.status(Response.Status.NOT_FOUND)
						.entity(e.getMessage())
						.build();
		}
		
		return internalUploadDataSet(collectionId, dataSourceId, dataSetId, dataType, inputStream);
	}
	
	
	@Path("/{collectionId}/{dataSourceId}/{dataSetId}/uploadUrl")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadUrl(
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@PathParam("dataSetId") String dataSetId,
			@FormParam("dataType") String dataType,
			@FormParam("url") String url)
	{
		String dataSetName = getDataSetName(collectionId, dataSourceId, dataSetId);			
		logger.info(String.format("UploadUrl: DataSet=%s, Url=%s", dataSetName, url));
		
		InputStream inputStream;
		try {
			inputStream = new URL(url).openStream();
		} catch (IOException e) {			
			return
					Response
						.serverError()
						.entity(e.getMessage())
						.build();
		}
		
		return internalUploadDataSet(collectionId, dataSourceId, dataSetId, dataType, inputStream);
	}

	@Path("/{collectionId}/{dataSourceId}/{dataSetId}/uploadContent")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadContent(
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@PathParam("dataSetId") String dataSetId,
			@FormParam("dataType") String dataType,			
			@FormParam("content") String content)
	{
		String dataSetName = getDataSetName(collectionId, dataSourceId, dataSetId);			
		logger.info(String.format("UploadContent: DataSet=%s, Content=%s", dataSetName, content));
		
		InputStream inputStream = new ByteArrayInputStream(content.getBytes());
		return internalUploadDataSet(collectionId, dataSourceId, dataSetId, dataType, inputStream);
	}

	
	@Path("/{collectionId}/{dataSourceId}/{dataSetId}/uploadClientFile")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadClientFile(
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@PathParam("dataSetId") String dataSetId,
			@FormDataParam("dataType") String dataType,			
			@FormDataParam("file") InputStream inputStream,
	        @FormDataParam("file") FormDataContentDisposition fileDetail)
	{
		
		String dataSetName = getDataSetName(collectionId, dataSourceId, dataSetId);
		logger.info(String.format("UploadContent: DataSet=%s, FileName=%s", dataSetName, fileDetail.getFileName()));		
		return internalUploadDataSet(collectionId, dataSourceId, dataSetId, dataType, inputStream);
	}
	
	
	private Response internalUploadDataSet(
			String collectionId,
			String dataSourceId,
			String dataSetId,
			String dataType,
			InputStream inputStream)
	{	
		
		try {
			DataSetManager dataSetManager = new DataSetManager(DrumbeatApplication.getInstance().getJenaProvider().openDefaultModel());			

			String dataSetName = getDataSetName(collectionId, dataSourceId, dataSetId);
			if (!dataSetManager.exists(collectionId, dataSourceId, dataSetId)) {
				return
					Response
						.status(Response.Status.NOT_FOUND)
						.entity(String.format("Data set not found: collectionId=%s, dataSourceId=%s, dataSetId=%s", collectionId, dataSourceId, dataSetId))
						.build();
			}			

			Model jenaModel = DrumbeatApplication.getInstance().getJenaProvider().openModel(dataSetName);
			
			Map<String, Object> responseEntity = new HashMap<>();
			responseEntity.put("dataSetName", dataSetName);
			responseEntity.put("oldSize", jenaModel.size());
			
			if (dataType.equalsIgnoreCase(DATA_TYPE_IFC)) {
				jenaModel = dataSetManager.uploadIfcData(inputStream, jenaModel);				
			} else {
				return
						Response
						.status(Response.Status.NOT_FOUND)
						.entity(String.format("Unknown data type=%s", dataType))
						.build();
			}
			
			responseEntity.put("newSize", jenaModel.size());			

			return Response
					.ok(responseEntity, MediaType.APPLICATION_JSON)
					.build();
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			
			return Response
					.serverError()
					.entity(e.getMessage())
					.build();			
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		
		
	}
	
	public static String getDataSetName(
			String collectionId,
			String dataSourceId,
			String dataSetId)
	{
		return String.format(DATASET_NAME_FORMAT, collectionId, dataSourceId, dataSetId);
	}


	private static DataSetManager getManager(
			ServletContext servletContext) {
		if (dataSetManager == null) {
			try {
				Model model = DrumbeatApplication.getInstance().getJenaProvider()
						.openDefaultModel();
				dataSetManager = new DataSetManager(model);
			} catch (Exception e) {
				throw new RuntimeException("Could not get Jena model: " + e.getMessage(), e);
			}
		}
		return dataSetManager;

	}
	

}