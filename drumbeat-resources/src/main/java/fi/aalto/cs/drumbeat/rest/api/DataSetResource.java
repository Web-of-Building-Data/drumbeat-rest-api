package fi.aalto.cs.drumbeat.rest.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.Lang;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.github.jsonldjava.jena.JenaJSONLD;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import fi.aalto.cs.drumbeat.rest.accessory.PrettyPrinting;
import fi.aalto.cs.drumbeat.rest.application.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.managers.CollectionManager;
import fi.aalto.cs.drumbeat.rest.managers.DataManager;
import fi.aalto.cs.drumbeat.rest.managers.DataSetManager;
import fi.aalto.cs.drumbeat.rest.ontology.BuildingDataOntology.Collections;
import fi.aalto.cs.drumbeat.rest.ontology.BuildingDataOntology.DataSets;
import fi.aalto.cs.drumbeat.rest.ontology.BuildingDataOntology.DataSources;
import fi.hut.cs.drumbeat.common.file.FileManager;

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
public class DataSetResource  extends AbstractResource{
	private static DataSetManager manager;

	@Context
	private ServletContext servletContext;

	
	public static final String DATA_TYPE_IFC = "IFC";
	public static final String DATA_TYPE_RDF = "RDF";
	public static final String DATA_TYPE_CSV = "CSV";
	
	private static final String DATASET_NAME_FORMAT = "%s_%s_%s";	

	private static final Logger logger = Logger.getLogger(DataSetResource.class);

	
	@Path("/{collectionid}/{datasourceid}")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String listHTML(@PathParam("collectionid") String collectionid, @PathParam("datasourceid") String datasourceid) {
		Model m = ModelFactory.createDefaultModel();
		try{
		if (!getManager().listAll2Model(m, collectionid, datasourceid))
			return "<HTML><BODY>Status:\"No datasources\"</BODY></HTML>";
		} catch (Exception e) {
			return PrettyPrinting.formatErrorHTML("Check that the RDF store is started: "+e.getMessage());
		}

		return PrettyPrinting.prettyPrintingHTML(m);
	}

	@Path("/{collectionid}/{datasourceid}")
	@GET
	@Produces({MediaType.APPLICATION_JSON,"application/ld+json"})
	public String listJSON(@PathParam("collectionid") String collectionid, @PathParam("datasourceid") String datasourceid) {
		Model m = ModelFactory.createDefaultModel();

		try {
			try{
			if (!getManager().listAll2Model(m, collectionid, datasourceid))
				return "{\"Status\":\"No datasources\"}";
			} catch (Exception e) {
				return PrettyPrinting.formatErrorJSON("Check that the RDF store is started: "+e.getMessage());
			}

			return model2JSON_LD(m);
		} catch (Exception e) {
			return PrettyPrinting.formatErrorJSON(e.getMessage());
		}

	}

	@Path("/{collectionid}/{datasourceid}")
	@GET
	@Produces("text/turtle")
	public String listTURTLE(@PathParam("collectionid") String collectionid, @PathParam("datasourceid") String datasourceid) {
		Model m = ModelFactory.createDefaultModel();

		try {
			try
			{
			if (!getManager().listAll2Model(m, collectionid, datasourceid))
				return "{\"Status\":\"No datasources\"}";
			} catch (Exception e) {
				return PrettyPrinting.formatErrorTURTLE("Check that the RDF store is started: "+e.getMessage());
			}

			return model2TURTLE(m);
		} catch (Exception e) {
			return PrettyPrinting.formatErrorTURTLE(e.getMessage());
		}

	}

	@Path("/{collectionid}/{datasourceid}")
	@GET
	@Produces("application/rdf+xml")
	public String listRDF(@PathParam("collectionid") String collectionid, @PathParam("datasourceid") String datasourceid) {
		Model m = ModelFactory.createDefaultModel();

		try {
			try{
			if (!getManager().listAll2Model(m, collectionid, datasourceid))
				return "{\"Status\":\"No datasources\"}";
			} catch (Exception e) {
				return PrettyPrinting.formatErrorRDF("Check that the RDF store is started: "+e.getMessage());
			}

			return model2RDF(m);
		} catch (Exception e) {
			return PrettyPrinting.formatErrorRDF(e.getMessage());
		}

	}

	@Path("/{collectionid}/{datasourceid}/{datasetid}")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String getHTML(@Context HttpServletRequest httpRequest,@PathParam("collectionid") String collectionid, @PathParam("datasourceid") String datasourceid, @PathParam("datasetid") String datasetid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try{
		if (!getManager().get2Model(m, collectionid, datasourceid, datasetid))
			return "<HTML><BODY>Status:\"The ID does not exists\"</BODY></HTML>";
		} catch (Exception e) {
			return PrettyPrinting.formatErrorHTML("Check that the RDF store is started: "+e.getMessage());
		}
		return PrettyPrinting.prettyPrintingHTML(m);
	}

	@Path("/{collectionid}/{datasourceid}/{datasetid}")
	@GET
	@Produces({MediaType.APPLICATION_JSON,"application/ld+json"})
	public String getJSON(@Context HttpServletRequest httpRequest,@PathParam("collectionid") String collectionid, @PathParam("datasourceid") String datasourceid, @PathParam("datasetid") String datasetid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try{
		if (!getManager().get2Model(m, collectionid, datasourceid, datasetid))
			return "{\"Status\":\"The ID does not exists\"}";
		} catch (Exception e) {
			return PrettyPrinting.formatErrorJSON("Check that the RDF store is started: "+e.getMessage());
		}

		return model2JSON_LD(m);
	}

	@Path("/{collectionid}/{datasourceid}/{datasetid}")
	@GET
	@Produces("text/turtle")
	public String getTURTLE(@Context HttpServletRequest httpRequest,@PathParam("collectionid") String collectionid, @PathParam("datasourceid") String datasourceid, @PathParam("datasetid") String datasetid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try
		{
		if (!getManager().get2Model(m, collectionid, datasourceid, datasetid))
			return "{\"Status\":\"The ID does not exists\"}";
		} catch (Exception e) {
			return PrettyPrinting.formatErrorTURTLE("Check that the RDF store is started: "+e.getMessage());
		}

		return model2TURTLE(m);
	}

	@Path("/{collectionid}/{datasourceid}/{datasetid}")
	@GET
	@Produces("application/rdf+xml")
	public String getRDF(@Context HttpServletRequest httpRequest,@PathParam("collectionid") String collectionid, @PathParam("datasourceid") String datasourceid, @PathParam("datasetid") String datasetid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try{
		if (!getManager().get2Model(m, collectionid, datasourceid, datasetid))
			return "{\"Status\":\"The ID does not exists\"}";
		} catch (Exception e) {
			return PrettyPrinting.formatErrorRDF("Check that the RDF store is started: "+e.getMessage());
		}

		return model2RDF(m);
	}

	
	@Path("/{collectionid}/{datasourceid}/{datasetid}")
	@PUT
	public String create(@Context HttpServletRequest httpRequest,@PathParam("collectionid") String collectionid, @PathParam("datasourceid") String datasourceid, @PathParam("datasetid") String datasetid,@FormDataParam("name") String name) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		try {
			getManager().create(collectionid, datasourceid, datasetid,name);
		} catch (Exception e) {
			return PrettyPrinting.formatErrorJSON("Check that the RDF store is started: "+e.getMessage());
		}

		return "{\"Status\":\"Done\"}";
	}

	
	
	
	@Path("/{collectionid}/{datasourceid}/{datasetid}")
	@DELETE
	public String delete(@Context HttpServletRequest httpRequest,@PathParam("collectionid") String collectionid, @PathParam("datasourceid") String datasourceid, @PathParam("datasetid") String datasetid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		try {
			getManager().delete(collectionid, datasourceid, datasetid);
		} catch (Exception e) {
			return PrettyPrinting.formatErrorJSON("Check that the RDF store is started: "+e.getMessage());
		}

		return "{\"Status\":\"Done\"}";
	}
	
	
	@Path("/{collectionId}/{dataSourceId}/{dataSetId}/uploadServerFile")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> uploadServerFile(
			@Context HttpServletRequest httpRequest,
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@PathParam("dataSetId") String dataSetId,
			@FormParam("dataType") String dataType,
			@FormParam("dataFormat") String dataFormat,
			@FormParam("filePath") String filePath)
	{
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		String dataSetName = getDataSetName(collectionId, dataSourceId, dataSetId);
		logger.info(String.format("UploadServerFile: DataSet=%s, ServerFilePath=%s", dataSetName, filePath));
		
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(filePath);
		} catch (FileNotFoundException e) {
			throw new WebApplicationException(
					Response
						.status(Response.Status.NOT_FOUND)
						.entity(e.getMessage())
						.build());
		}
		
		return internalUploadDataSet(collectionId, dataSourceId, dataSetId, dataType, dataFormat, inputStream);
	}
	
	@Path("/{collectionId}/{dataSourceId}/{dataSetId}/uploadUrl")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> uploadUrl(
			@Context HttpServletRequest httpRequest,
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@PathParam("dataSetId") String dataSetId,
			@FormParam("dataType") String dataType,
			@FormParam("dataFormat") String dataFormat,
			@FormParam("url") String url)
	{
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		String dataSetName = getDataSetName(collectionId, dataSourceId, dataSetId);			
		logger.info(String.format("UploadUrl: DataSet=%s, Url=%s", dataSetName, url));
		
		InputStream inputStream;
		try {
			inputStream = new URL(url).openStream();
		} catch (IOException e) {			
			throw new WebApplicationException(
					Response
						.status(Response.Status.NOT_FOUND)
						.entity(e.getMessage())
						.build());
		}
		
		return internalUploadDataSet(collectionId, dataSourceId, dataSetId, dataType, dataFormat, inputStream);
	}


	@Path("/{collectionId}/{dataSourceId}/{dataSetId}/uploadContent")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> uploadContent(
			@Context HttpServletRequest httpRequest,
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@PathParam("dataSetId") String dataSetId,
			@FormParam("dataType") String dataType,
			@FormParam("dataFormat") String dataFormat,			
			@FormParam("content") String content)
	{
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		String dataSetName = getDataSetName(collectionId, dataSourceId, dataSetId);			
		logger.info(String.format("UploadContent: DataSet=%s, Content=%s", dataSetName, content));
		
		InputStream inputStream = new ByteArrayInputStream(content.getBytes());
		return internalUploadDataSet(collectionId, dataSourceId, dataSetId, dataType, dataFormat, inputStream);
	}

	
	@Path("/{collectionId}/{dataSourceId}/{dataSetId}/uploadClientFile")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> uploadClientFile(
			@Context HttpServletRequest httpRequest,
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@PathParam("dataSetId") String dataSetId,
			@FormDataParam("dataType") String dataType,
			@FormDataParam("dataFormat") String dataFormat,
			@FormDataParam("file") InputStream inputStream,
	        @FormDataParam("file") FormDataContentDisposition fileDetail)
	{
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		String dataSetName = getDataSetName(collectionId, dataSourceId, dataSetId);
		logger.info(String.format("UploadContent: DataSet=%s, FileName=%s", dataSetName, fileDetail.getFileName()));		
		return internalUploadDataSet(collectionId, dataSourceId, dataSetId, dataType, dataFormat, inputStream);
	}
	
	private String getUploadFilePath(String dataSetName, String dataType, String dataFormat) {
		return String.format("%s/%s/%s.%s",
				DrumbeatApplication.getInstance().getRealPath(DrumbeatApplication.Paths.UPLOADS_FOLDER_PATH),
				dataType,
				dataSetName,
				dataFormat);
	}
	
	
	private Map<String, Object> internalUploadDataSet(
			String collectionId,
			String dataSourceId,
			String dataSetId,
			String dataType,
			String dataFormat,
			InputStream inputStream)
	{	
		
		try {
			Model mainModel = DrumbeatApplication.getInstance().getMainModel();
			DataSetManager dataSetManager = new DataSetManager(mainModel);			

			String dataSetName = getDataSetName(collectionId, dataSourceId, dataSetId);
			if (!dataSetManager.checkExists(collectionId, dataSourceId, dataSetId)) {
				throw new WebApplicationException(
						Response
							.status(Response.Status.NOT_FOUND)
							.entity(
									String.format(
											"Data set not found: collection=<%s>, dataSource=<%s>, dataSet=<%s>",
											Collections.formatUrl(collectionId),
											DataSources.formatUrl(collectionId, dataSourceId),
											DataSets.formatUrl(collectionId, dataSourceId, dataSetId)))
							.build());				
			}
			
			if (DrumbeatApplication.getInstance().getSaveUploads()) {
				String outputFilePath = getUploadFilePath(dataSetName, dataType, dataFormat); 
				OutputStream outputStream = FileManager.createFileOutputStream(outputFilePath);
				IOUtils.copy(inputStream, outputStream);
				inputStream.close();
				outputStream.close();
				
				inputStream = new FileInputStream(outputFilePath);
			}

			Model jenaModel = DrumbeatApplication.getInstance().getJenaProvider().openModel(dataSetName);
			
			Map<String, Object> responseEntity = new HashMap<>();
			responseEntity.put("dataSetName", dataSetName);
			responseEntity.put("oldSize", jenaModel.size());
			
			if (dataType.equalsIgnoreCase(DATA_TYPE_IFC)) {
				jenaModel = new DataManager().uploadIfcData(inputStream, jenaModel);				
			} else if (dataType.equalsIgnoreCase(DATA_TYPE_RDF)) {
				// TODO: convert dataFormat string to Lang
				jenaModel = new DataManager().uploadRdfData(inputStream, Lang.TURTLE, jenaModel);				
			} else {
				throw new WebApplicationException(
						Response
							.status(Response.Status.BAD_REQUEST)
							.entity(String.format("Unknown data type=%s", dataType))
							.build());
			}
			
			responseEntity.put("newSize", jenaModel.size());
			
			return responseEntity;

		} catch (WebApplicationException wae) {
			throw wae;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			
			throw new WebApplicationException(
					e,
					Response
						.serverError()
						.entity(String.format("Unexpected error: %s", e.getMessage()))
						.build());

		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		
		
	}
	
	
	private static String getDataSetName(
			String collectionId,
			String dataSourceId,
			String dataSetId)
	{
		return String.format(DATASET_NAME_FORMAT, collectionId, dataSourceId, dataSetId);
	}



	public DataSetManager getManager() {
		if (manager == null) {
			try {
				Model model = DrumbeatApplication.getInstance().getJenaProvider()
						.openDefaultModel();
				manager = new DataSetManager(model);
			} catch (Exception e) {
				throw new RuntimeException("Could not get Jena model: " + e.getMessage(), e);
			}
		}
		return manager;

	}

}