package fi.aalto.cs.drumbeat.rest.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.github.jsonldjava.jena.JenaJSONLD;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import fi.aalto.cs.drumbeat.rest.accessory.HTMLPrettyPrinting;
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

	@Path("/{collectionname}/{datasourcename}")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String listHTML(@PathParam("collectionname") String collectionname, @PathParam("datasourcename") String datasourcename) {
		Model m = ModelFactory.createDefaultModel();
		try{
		if (!getManager(servletContext).listAll(m, collectionname, datasourcename))
			return "<HTML><BODY>Status:\"No datasources\"</BODY></HTML>";
		} catch (Exception e) {
			return "{\"Status\":\"ERROR: Check that the RDF store is started: cd /etc/init.d;sudo sh virtuoso start \"}";
		}

		return HTMLPrettyPrinting.prettyPrinting(m);
	}

	@Path("/{collectionname}/{datasourcename}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String listJSON_LD(@PathParam("collectionname") String collectionname, @PathParam("datasourcename") String datasourcename) {
		Model m = ModelFactory.createDefaultModel();

		try {
			try{
			if (!getManager(servletContext).listAll(m, collectionname, datasourcename))
				return "{\"Status\":\"No datasources\"}";
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

	@Path("/{collectionname}/{datasourcename}")
	@GET
	@Produces("text/turtle")
	public String listTurtle(@PathParam("collectionname") String collectionname, @PathParam("datasourcename") String datasourcename) {
		Model m = ModelFactory.createDefaultModel();

		try {
			try
			{
			if (!getManager(servletContext).listAll(m, collectionname, datasourcename))
				return "{\"Status\":\"No datasources\"}";
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

	@Path("/{collectionname}/{datasourcename}")
	@GET
	@Produces("application/rdf+xml")
	public String listRDF(@PathParam("collectionname") String collectionname, @PathParam("datasourcename") String datasourcename) {
		Model m = ModelFactory.createDefaultModel();

		try {
			try{
			if (!getManager(servletContext).listAll(m, collectionname, datasourcename))
				return "{\"Status\":\"No datasources\"}";
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

	@Path("/{collectionname}/{datasourcename}/{datasetname}")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String getHTML(@Context HttpServletRequest httpRequest,@PathParam("collectionname") String collectionname, @PathParam("datasourcename") String datasourcename, @PathParam("datasetname") String datasetname) {
		ApplicationConfig.setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		if (!getManager(servletContext).get(m, collectionname, datasourcename, datasetname))
			return "<HTML><BODY>Status:\"The ID does not exists\"</BODY></HTML>";
		return HTMLPrettyPrinting.prettyPrinting(m);
	}

	@Path("/{collectionname}/{datasourcename}/{datasetname}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getJSON(@Context HttpServletRequest httpRequest,@PathParam("collectionname") String collectionname, @PathParam("datasourcename") String datasourcename, @PathParam("datasetname") String datasetname) {
		ApplicationConfig.setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try{
		if (!getManager(servletContext).get(m, collectionname, datasourcename, datasetname))
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

	@Path("/{collectionname}/{datasourcename}/{datasetname}")
	@GET
	@Produces("text/turtle")
	public String getTURTLE(@Context HttpServletRequest httpRequest,@PathParam("collectionname") String collectionname, @PathParam("datasourcename") String datasourcename, @PathParam("datasetname") String datasetname) {
		ApplicationConfig.setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try
		{
		if (!getManager(servletContext).get(m, collectionname, datasourcename, datasetname))
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

	@Path("/{collectionname}/{datasourcename}/{datasetname}")
	@GET
	@Produces("application/rdf+xml")
	public String getRDF(@Context HttpServletRequest httpRequest,@PathParam("collectionname") String collectionname, @PathParam("datasourcename") String datasourcename, @PathParam("datasetname") String datasetname) {
		ApplicationConfig.setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try{
		if (!getManager(servletContext).get(m, collectionname, datasourcename, datasetname))
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

	@Path("/{collectionname}/{datasourcename}/{datasetname}")
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public String createJSON(@Context HttpServletRequest httpRequest,@PathParam("collectionname") String collectionname, @PathParam("datasourcename") String datasourcename, @PathParam("datasetname") String datasetname) {
		ApplicationConfig.setBaseUrl(httpRequest);
		try {
			getManager(servletContext).create(collectionname, datasourcename, datasetname);
		} catch (Exception e) {
			return "{\"Status\":\"ERROR: Check that the RDF store is started: cd /etc/init.d;sudo sh virtuoso start \"}";
		}

		return "{\"Status\":\"Done\"}";
	}

	@Path("/{collectionname}/{datasourcename}/{datasetname}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public String deleteJSON(@Context HttpServletRequest httpRequest,@PathParam("collectionname") String collectionname, @PathParam("datasourcename") String datasourcename, @PathParam("datasetname") String datasetname) {
		ApplicationConfig.setBaseUrl(httpRequest);
		try {
			getManager(servletContext).delete(collectionname, datasourcename, datasetname);
		} catch (Exception e) {
			return "{\"Status\":\"ERROR: Check that the RDF store is started: cd /etc/init.d;sudo sh virtuoso start \"}";
		}

		return "{\"Status\":\"Done\"}";
	}

	@Path("/{collectionId}/{dataSourceId}/{dataSetId}/importServerFile")
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public Response importServerFile(@Context HttpServletRequest httpRequest,@PathParam("collectionId") String collectionId, @PathParam("dataSourceId") String dataSourceId, @PathParam("dataSetId") String dataSetId,
			@FormDataParam("filePath") String filePath) {
		ApplicationConfig.setBaseUrl(httpRequest);
		try {

			String dataSetName = String.format(DATASET_NAME_FORMAT, collectionId, dataSourceId, dataSetId);

			String message = String.format("ImportServerFile: DataSet=%s, ServerFilePath=%s", dataSetName, filePath);
			logger.info(message);

			InputStream inputStream = new FileInputStream(filePath);
			DataSetManager dataSetManager = new DataSetManager(null);
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
	public Response importUrl(@Context HttpServletRequest httpRequest,@PathParam("collectionId") String collectionId, @PathParam("dataSourceId") String dataSourceId, @PathParam("dataSetId") String dataSetId, @FormDataParam("url") String url) {
		ApplicationConfig.setBaseUrl(httpRequest);
		try {

			String dataSetName = String.format(DATASET_NAME_FORMAT, collectionId, dataSourceId, dataSetId);

			String message = String.format("ImportUrl: DataSet=%s, Url=%s", dataSetName, url);
			logger.info(message);

			InputStream inputStream = new URL(url).openStream();
			DataSetManager dataSetManager = new DataSetManager(null);
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
	public Response importContent(@Context HttpServletRequest httpRequest,@PathParam("collectionId") String collectionId, @PathParam("dataSourceId") String dataSourceId, @PathParam("dataSetId") String dataSetId,
			@FormDataParam("content") String content) {
		ApplicationConfig.setBaseUrl(httpRequest);
		try {

			String dataSetName = String.format(DATASET_NAME_FORMAT, collectionId, dataSourceId, dataSetId);

			String message = String.format("ImportContent: DataSet=%s, Content=%s", dataSetName, content);
			logger.info(message);

			InputStream inputStream = new ByteArrayInputStream(content.getBytes());
			DataSetManager dataSetManager = new DataSetManager(null);
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
	public Response importClientFile(@Context HttpServletRequest httpRequest,@PathParam("collectionId") String collectionId, @PathParam("dataSourceId") String dataSourceId, @PathParam("dataSetId") String dataSetId,
			@FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition fileDetail) {
		ApplicationConfig.setBaseUrl(httpRequest);
		try {

			String dataSetName = String.format(DATASET_NAME_FORMAT, collectionId, dataSourceId, dataSetId);

			String message = String.format("ImportContent: DataSet=%s, FileName=%s", dataSetName, fileDetail.getFileName());
			logger.info(message);

			DataSetManager dataSetManager = new DataSetManager(null);
			Model jenaModel = ApplicationConfig.getJenaProvider().openModel(dataSetName);
			dataSetManager.importData(servletContext, inputStream, jenaModel);

			return Response.ok(message).build();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Response.serverError().entity(e).build();
		}
	}

	private static final String SERVER_UPLOAD_LOCATION_FOLDER = "C://jo/";
    // For small datasets
	@POST
	@Path("/{collectionId}/{dataSourceId}/{dataSetId}/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(@Context HttpServletRequest httpRequest,@PathParam("collectionId") String collectionId, @PathParam("dataSourceId") String dataSourceId, @PathParam("dataSetId") String dataSetId, @FormDataParam("file") InputStream fileInputStream, @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {
		ApplicationConfig.setBaseUrl(httpRequest);
		String filePath = SERVER_UPLOAD_LOCATION_FOLDER + contentDispositionHeader.getFileName();
		try {
			OutputStream outpuStream = new FileOutputStream(new File(filePath));
			int read = 0;
			byte[] bytes = new byte[1024];
			outpuStream = new FileOutputStream(new File(filePath));
			while ((read = fileInputStream.read(bytes)) != -1) {
				outpuStream.write(bytes, 0, read);
			}
			outpuStream.flush();
			outpuStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String output = "File saved to server location : " + filePath;
		return Response.status(200).entity(output).build();
	}

	private static DataSetManager getManager(ServletContext servletContext) {
		if (dataSetManager == null) {
			try {
				Model model = ApplicationConfig.getJenaProvider().openDefaultModel();
				dataSetManager = new DataSetManager(model);
			} catch (Exception e) {
				throw new RuntimeException("Could not get Jena model: " + e.getMessage(), e);
			}
		}
		return dataSetManager;

	}

}