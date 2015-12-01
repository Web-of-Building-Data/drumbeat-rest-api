package fi.aalto.cs.drumbeat.rest.api;

import java.io.ByteArrayInputStream;

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

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import fi.aalto.cs.drumbeat.rest.managers.AppManager;
import fi.aalto.cs.drumbeat.rest.managers.DataSetManager;
import fi.hut.cs.drumbeat.ifc.data.model.IfcModel;

@Path("/datasets")
public class DataSetResource {
	
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

	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String listDatasetsJSON() {
		String json=null;
		try {

			ResultSet results = getDatasetManager(servletContext).listAll();

			// write to a ByteArrayOutputStream
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			ResultSetFormatter.outputAsJSON(outputStream, results);
			// and turn that into a String
			json = new String(outputStream.toByteArray());
			
		} catch (RuntimeException r) {

		}
		return json;
	}

	@Path("/{name}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getDatasetJSON(@PathParam("name") String dataset_guid) {
		try {
			Resource dataset = getDatasetManager(servletContext).get(
					dataset_guid);
		} catch (RuntimeException r) {

		}
		return null;
	}

	@Path("/{name}")
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public void createDatasetJSON(@PathParam("name") String dataset_guid) {
		try {
			getDatasetManager(servletContext).create(dataset_guid);
			System.out.println("Dataset create name: " + dataset_guid);
		} catch (RuntimeException r) {

		}
	}

	private static DataSetManager getDatasetManager(
			ServletContext servletContext) {
		if (dataSetManager == null) {
			try {
				Model model = AppManager.getJenaProvider(servletContext)
						.openDefaultModel();
				dataSetManager = new DataSetManager(model);
			} catch (Exception e) {
				throw new RuntimeException("Could not get Jena model: "
						+ e.getMessage(), e);
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
			
			String message = String.format("ImportServerFile: DataSet=%s, ServerFilePath=%s", dataSetName, filePath);			
			logger.info(message);

			InputStream inputStream = new FileInputStream(filePath);
			DataSetManager dataSetManager = new DataSetManager(null);			
			Model jenaModel = AppManager.getJenaProvider(servletContext).openModel(dataSetName);			
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
			
			String message = String.format("ImportUrl: DataSet=%s, Url=%s", dataSetName, url);			
			logger.info(message);

			InputStream inputStream = new URL(url).openStream();
			DataSetManager dataSetManager = new DataSetManager(null);			
			Model jenaModel = AppManager.getJenaProvider(servletContext).openModel(dataSetName);			
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
			
			String message = String.format("ImportContent: DataSet=%s, Content=%s", dataSetName, content);			
			logger.info(message);

			InputStream inputStream = new ByteArrayInputStream(content.getBytes());
			DataSetManager dataSetManager = new DataSetManager(null);			
			Model jenaModel = AppManager.getJenaProvider(servletContext).openModel(dataSetName);			
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
			
			String message = String.format("ImportContent: DataSet=%s, FileName=%s", dataSetName, fileDetail.getFileName());			
			logger.info(message);

			DataSetManager dataSetManager = new DataSetManager(null);			
			Model jenaModel = AppManager.getJenaProvider(servletContext).openModel(dataSetName);			
			dataSetManager.importData(servletContext, inputStream, jenaModel);
			
			return Response.ok(message).build();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Response.serverError().entity(e).build();
		}
	}
	
	
}