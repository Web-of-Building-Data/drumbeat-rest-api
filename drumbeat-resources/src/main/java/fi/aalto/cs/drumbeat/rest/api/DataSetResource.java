package fi.aalto.cs.drumbeat.rest.api;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.AlreadyExistsException;
import com.hp.hpl.jena.shared.NotFoundException;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication.RequestParams;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatResponseBuilder;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatWebException;
import fi.aalto.cs.drumbeat.rest.managers.DataSourceObjectManager;
import fi.aalto.cs.drumbeat.rest.managers.DataSetManager;
import fi.aalto.cs.drumbeat.rest.ontology.LinkedBuildingDataOntology;
import fi.aalto.cs.drumbeat.common.DrumbeatException;
import fi.aalto.cs.drumbeat.ifc.convert.stff2ifc.IfcParserException;


@Path("/{elementType: datasets|linksets}")
//@Path("/datasets")
public class DataSetResource {

	private static final Logger logger = Logger.getLogger(DataSetResource.class);
	
	
	@GET
	@Path("/{collectionId}/{dataSourceId}")
	public Response getAll(			
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {		
			Model model = getDataSetManager().getAll(collectionId, dataSourceId);
			return DrumbeatResponseBuilder.build(
					Status.OK,
					model,
					headers.getAcceptableMediaTypes());			
		} catch (NotFoundException e) {
			throw new DrumbeatWebException(Status.NOT_FOUND, e);
		}
	}
	
	@GET
	@Path("/{collectionId}/{dataSourceId}/{dataSetId}")
	public Response getById(			
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@PathParam("dataSetId") String dataSetId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {		
			Model model = getDataSetManager().getById(collectionId, dataSourceId, dataSetId);
			return DrumbeatResponseBuilder.build(
					Status.OK,
					model,
					headers.getAcceptableMediaTypes());			
		} catch (NotFoundException e) {
			throw new DrumbeatWebException(Status.NOT_FOUND, e);
		}
	}
	
	@DELETE
	@Path("/{collectionId}/{dataSourceId}/{dataSetId}")
	public void delete(			
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@PathParam("dataSetId") String dataSetId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {		
			getDataSetManager().delete(collectionId, dataSourceId, dataSetId);
		} catch (NotFoundException e) {
			throw new DrumbeatWebException(Status.NOT_FOUND, e);
		}
	}
	
	@POST
	@Path("/{collectionId}/{dataSourceId}/{dataSetId}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response create(			
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@PathParam("dataSetId") String dataSetId,
			@FormParam("name") String name,
			@DefaultValue(RequestParams.NONE) @FormParam("overwritingMethod") String overwritingMethod,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {
			Model model = getDataSetManager().create(collectionId, dataSourceId, dataSetId, name, overwritingMethod);
			return DrumbeatResponseBuilder.build(
					Status.CREATED,
					model,
					headers.getAcceptableMediaTypes());			
		} catch (NotFoundException e) {
			throw new DrumbeatWebException(Status.NOT_FOUND, e);
		} catch (AlreadyExistsException e) {
			throw new DrumbeatWebException(Status.CONFLICT, e);			
		}
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
			@DefaultValue("") @FormParam("dataFormat") String dataFormat,
			@DefaultValue("") @FormParam("compressionFormat") String compressionFormat,
			@FormParam("filePath") String filePath,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);

		String graphName = LinkedBuildingDataOntology.formatGraphUri(collectionId, dataSourceId, dataSetId);
		logger.info(String.format("UploadServerFile: DataSet=%s, ServerFilePath=%s", graphName, filePath));
		
		InputStream in;
		try {
			in = new FileInputStream(filePath);
		} catch (FileNotFoundException e) {
			throw new DrumbeatWebException(Status.NOT_FOUND, e);
		}
		
		return internalUploadDataSet(collectionId, dataSourceId, dataSetId, dataType, dataFormat, compressionFormat, in, headers);
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
			@DefaultValue("") @FormParam("dataFormat") String dataFormat,
			@DefaultValue("") @FormParam("compressionFormat") String compressionFormat,
			@FormParam("url") String url,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);

		String graphName = LinkedBuildingDataOntology.formatGraphUri(collectionId, dataSourceId, dataSetId);			
		logger.info(String.format("UploadUrl: DataSet=%s, Url=%s", graphName, url));
		
		InputStream in;
		try {
			in = new URL(url).openStream();
		} catch (IOException e) {			
			throw new DrumbeatWebException(Status.NOT_FOUND, e);
		}
		
		return internalUploadDataSet(collectionId, dataSourceId, dataSetId, dataType, dataFormat, compressionFormat, in, headers);
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
			@DefaultValue("") @FormParam("dataFormat") String dataFormat,			
			@DefaultValue("") @FormParam("compressionFormat") String compressionFormat,
			@FormParam("content") String content,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);

		String graphName = LinkedBuildingDataOntology.formatGraphUri(collectionId, dataSourceId, dataSetId);			
		logger.info(String.format("UploadContent: DataSet=%s, Content=%s", graphName, content));
		
		InputStream in = new ByteArrayInputStream(content.getBytes());
		return internalUploadDataSet(collectionId, dataSourceId, dataSetId, dataType, dataFormat, compressionFormat, in, headers);
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
			@DefaultValue("") @FormDataParam("dataFormat") String dataFormat,
			@DefaultValue("") @FormDataParam("compressionFormat") String compressionFormat,
			@FormDataParam("file") InputStream in,
	        @FormDataParam("file") FormDataContentDisposition fileDetail,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		if (fileDetail == null || in == null) {
			throw new DrumbeatWebException(Status.BAD_REQUEST, "Client file is unavailable", null);			
		}
	        
		String graphName = LinkedBuildingDataOntology.formatGraphUri(collectionId, dataSourceId, dataSetId);
		logger.info(String.format("UploadContent: DataSet=%s, FileName=%s", graphName, fileDetail.getFileName()));		

		return internalUploadDataSet(collectionId, dataSourceId, dataSetId, dataType, dataFormat, compressionFormat, in, headers);
	}
	
	private Response internalUploadDataSet(
			String collectionId,
			String dataSourceId,
			String dataSetId,
			String dataType,
			String dataFormat,
			String compressionFormat,
			InputStream in,
			HttpHeaders headers)
	{	
		
		try {
			DataSourceObjectManager objectManager = new DataSourceObjectManager();
			boolean saveToFiles = DrumbeatApplication.getInstance().getSaveUploads();
			
			Model dataSetInfoModel = objectManager.upload(
					collectionId,
					dataSourceId,
					dataSetId,
					dataType,
					dataFormat,
					compressionFormat,
					in,
					saveToFiles);
			
			return DrumbeatResponseBuilder.build(
					Status.CREATED,
					dataSetInfoModel,
					headers.getAcceptableMediaTypes());
		} catch (DrumbeatWebException drumbeatWebException) {
			throw drumbeatWebException;
		} catch (NotFoundException e) {
			throw new DrumbeatWebException(Status.NOT_FOUND, e);
		} catch (IfcParserException e) {
			throw new DrumbeatWebException(Status.BAD_REQUEST, e);			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			
			throw new DrumbeatWebException(
					Status.INTERNAL_SERVER_ERROR,
					String.format("Unexpected error: %s", e.getMessage()),
					e);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		
		
	}
	
//	@Path("/{collectionId}/{dataSourceId}/{dataSetId}/createLinkSet")
//	@POST
//	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response createLinkSet(
//			@PathParam("collectionId") String collectionId,
//			@PathParam("dataSourceId") String dataSourceId,
//			@PathParam("dataSetId") String dataSetId,
//			@FormParam("sourceUrl") String sourceUrl,
//			@FormParam("targetUrl") String targetUrl,
//			@Context UriInfo uriInfo,
//			@Context HttpHeaders headers)
//	{
//		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
//
//		String graphName = LinkedBuildingDataOntology.formatGraphUri(collectionId, dataSourceId, dataSetId);
//		logger.info(String.format("CreateLinkSet: Name=%s, Source=%s, Target=%s", graphName, sourceUrl, targetUrl));
//		
//		return getDataSetManager().createLinkSet(collectionId, dataSourceId, dataSetId, sourceUrl, targetUrl);
//	}	
	
	
	private DataSetManager getDataSetManager() {
		try {
			return new DataSetManager();
		} catch (DrumbeatException e) {
			logger.error(e);
			throw new DrumbeatWebException(
					Status.INTERNAL_SERVER_ERROR,
					"Error getting DataSetManager instance: " + e.getMessage(),
					e);
		}
	}
	
	
}