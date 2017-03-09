package fi.aalto.cs.drumbeat.rest.api;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.AlreadyExistsException;
import org.apache.jena.shared.NotFoundException;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication.RequestParams;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatResponseBuilder;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatWebException;
import fi.aalto.cs.drumbeat.rest.common.NameFormatter;
import fi.aalto.cs.drumbeat.rest.managers.LinkSetManager;
import fi.aalto.cs.drumbeat.rest.managers.LinkSetObjectManager;
import fi.aalto.cs.drumbeat.common.DrumbeatException;
import fi.aalto.cs.drumbeat.common.file.FileManager;
import fi.aalto.cs.drumbeat.common.params.BooleanParam;
import fi.aalto.cs.drumbeat.common.string.StringUtils;
import fi.aalto.cs.drumbeat.ifc.convert.stff2ifc.IfcParserException;


@Path("/linksets")
public class LinkSetResource extends DrumbeatApiBase {

	private static final Logger logger = Logger.getLogger(LinkSetResource.class);
	
	
	@GET
	@Path("/{collectionId}/{linkSourceId}")
	public Response getAll(			
			@PathParam("collectionId") String collectionId,
			@PathParam("linkSourceId") String linkSourceId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers,
			@Context HttpServletRequest request)
	{
		notifyRequest(uriInfo, headers, request);
		
		try {		
			Model model = getLinkSetManager().getAll(collectionId, linkSourceId);
			return DrumbeatResponseBuilder.build(
					Status.OK,
					model,
					headers.getAcceptableMediaTypes());			
		} catch (NotFoundException e) {
			throw new DrumbeatWebException(Status.NOT_FOUND, e);
		}
	}
	
	@GET
	@Path("/{collectionId}/{linkSourceId}/{linkSetId}")
	public Response getById(			
			@PathParam("collectionId") String collectionId,
			@PathParam("linkSourceId") String linkSourceId,
			@PathParam("linkSetId") String linkSetId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers,
			@Context HttpServletRequest request)
	{
		notifyRequest(uriInfo, headers, request);
		
		try {		
			Model model = getLinkSetManager().getById(collectionId, linkSourceId, linkSetId);
			return DrumbeatResponseBuilder.build(
					Status.OK,
					model,
					headers.getAcceptableMediaTypes());			
		} catch (NotFoundException e) {
			throw new DrumbeatWebException(Status.NOT_FOUND, e);
		}
	}
	
	@DELETE
	@Path("/{collectionId}/{linkSourceId}/{linkSetId}")
	public void delete(			
			@PathParam("collectionId") String collectionId,
			@PathParam("linkSourceId") String linkSourceId,
			@PathParam("linkSetId") String linkSetId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers,
			@Context HttpServletRequest request)
	{
		notifyRequest(uriInfo, headers, request);
		
		try {		
			getLinkSetManager().delete(collectionId, linkSourceId, linkSetId);
		} catch (NotFoundException e) {
			throw new DrumbeatWebException(Status.NOT_FOUND, e);
		}
	}
	
	@PUT
	@Path("/{collectionId}/{linkSourceId}/{linkSetId}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response create(			
			@PathParam("collectionId") String collectionId,
			@PathParam("linkSourceId") String linkSourceId,
			@PathParam("linkSetId") String linkSetId,
			@FormParam("name") String name,
			@DefaultValue(RequestParams.NONE) @FormParam("overwritingMethod") String overwritingMethod,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers,
			@Context HttpServletRequest request)
	{
		notifyRequest(uriInfo, headers, request);
		
		try {
			Model model = getLinkSetManager().create(collectionId, linkSourceId, linkSetId, name, overwritingMethod);
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
	
	
	@POST
	@Path("/{collectionId}/{linkSourceId}/{linkSetId}/uploadServerFile")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response uploadServerFile(
			@PathParam("collectionId") String collectionId,
			@PathParam("linkSourceId") String linkSourceId,
			@PathParam("linkSetId") String linkSetId,
			@FormParam("dataType") String dataType,
			@DefaultValue("") @FormParam("dataFormat") String dataFormat,
			@DefaultValue("false") @FormParam("clearBefore") String clearBefore,
			@DefaultValue("false") @FormParam("notifyRemote") String notifyRemote,
			@FormParam("filePath") String filePath,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers,
			@Context HttpServletRequest request)
	{
		notifyRequest(uriInfo, headers, request);

		if (StringUtils.isEmptyOrNull(dataFormat)) {
			dataFormat = FileManager.getFileName(filePath);
		}		

		String graphName = NameFormatter.formatLinkSetGraphUri(collectionId, linkSourceId, linkSetId);
		logger.info(String.format("UploadServerFile: LinkSet=%s, ServerFilePath=%s", graphName, filePath));
		
		InputStream in;
		try {
			in = new FileInputStream(filePath);
		} catch (FileNotFoundException e) {
			throw new DrumbeatWebException(Status.NOT_FOUND, e);
		}
		
		return internalUploadLinkSet(collectionId, linkSourceId, linkSetId, dataType, dataFormat, clearBefore, notifyRemote, in, headers);
	}
	
	@POST
	@Path("/{collectionId}/{linkSourceId}/{linkSetId}/uploadUrl")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response uploadUrl(
			@PathParam("collectionId") String collectionId,
			@PathParam("linkSourceId") String linkSourceId,
			@PathParam("linkSetId") String linkSetId,
			@FormParam("dataType") String dataType,
			@DefaultValue("") @FormParam("dataFormat") String dataFormat,
			@DefaultValue("false") @FormParam("clearBefore") String clearBefore,
			@DefaultValue("false") @FormParam("notifyRemote") String notifyRemote,
			@FormParam("url") String url,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers,
			@Context HttpServletRequest request)
	{
		notifyRequest(uriInfo, headers, request);

		if (StringUtils.isEmptyOrNull(dataFormat)) {
			throw new DrumbeatWebException(Status.BAD_REQUEST, "Undefined param 'dataFormat'", null);
		}

		String graphName = NameFormatter.formatLinkSetGraphUri(collectionId, linkSourceId, linkSetId);			
		logger.info(String.format("UploadUrl: LinkSet=%s, Url=%s", graphName, url));
		
		InputStream in;
		try {
			in = new URL(url).openStream();
		} catch (IOException e) {			
			throw new DrumbeatWebException(Status.NOT_FOUND, e);
		}
		
		return internalUploadLinkSet(collectionId, linkSourceId, linkSetId, dataType, dataFormat, clearBefore, notifyRemote, in, headers);
	}


	@POST
	@Path("/{collectionId}/{linkSourceId}/{linkSetId}/uploadContent")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response uploadContent(
			@PathParam("collectionId") String collectionId,
			@PathParam("linkSourceId") String linkSourceId,
			@PathParam("linkSetId") String linkSetId,
			@FormParam("dataType") String dataType,
			@DefaultValue("") @FormParam("dataFormat") String dataFormat,			
			@DefaultValue("false") @FormParam("clearBefore") String clearBefore,
			@DefaultValue("false") @FormParam("notifyRemote") String notifyRemote,
			@FormParam("content") String content,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers,
			@Context HttpServletRequest request)
	{
		notifyRequest(uriInfo, headers, request);

		if (StringUtils.isEmptyOrNull(dataFormat)) {
			throw new DrumbeatWebException(Status.BAD_REQUEST, "Undefined param 'dataFormat'", null);
		}		

		String graphName = NameFormatter.formatLinkSetGraphUri(collectionId, linkSourceId, linkSetId);			
		logger.info(String.format("UploadContent: LinkSet=%s, Content=%s", graphName, content));
		
		InputStream in = new ByteArrayInputStream(content.getBytes());
		return internalUploadLinkSet(collectionId, linkSourceId, linkSetId, dataType, dataFormat, clearBefore, notifyRemote, in, headers);
	}

	
	@POST
	@Path("/{collectionId}/{linkSourceId}/{linkSetId}/uploadClientFile")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadClientFile(
			@PathParam("collectionId") String collectionId,
			@PathParam("linkSourceId") String linkSourceId,
			@PathParam("linkSetId") String linkSetId,
			@FormDataParam("dataType") String dataType,
			@DefaultValue("") @FormDataParam("dataFormat") String dataFormat,
			@DefaultValue("") @FormDataParam("compressionFormat") String compressionFormat,
			@DefaultValue("false") @FormDataParam("clearBefore") String clearBefore,
			@DefaultValue("false") @FormDataParam("notifyRemote") String notifyRemote,
			@FormDataParam("file") InputStream in,
	        @FormDataParam("file") FormDataContentDisposition fileDetail,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers,
			@Context HttpServletRequest request)
	{
		notifyRequest(uriInfo, headers, request);
		
		if (fileDetail == null || in == null) {
			throw new DrumbeatWebException(Status.BAD_REQUEST, "Client file is unavailable", null);			
		}
	        
		if (StringUtils.isEmptyOrNull(dataFormat)) {
			dataFormat = fileDetail.getFileName();
		}

		String graphName = NameFormatter.formatLinkSetGraphUri(collectionId, linkSourceId, linkSetId);
		logger.info(String.format("UploadContent: LinkSet=%s, FileName=%s", graphName, fileDetail.getFileName()));		

		return internalUploadLinkSet(collectionId, linkSourceId, linkSetId, dataType, dataFormat, clearBefore, notifyRemote, in, headers);
	}
	
	private Response internalUploadLinkSet(
			String collectionId,
			String linkSourceId,
			String linkSetId,
			String dataType,
			String dataFormat,
			String clearBefore,
			String notifyRemote,
			InputStream in,
			HttpHeaders headers)
	{	
		
		try {
			LinkSetObjectManager objectManager = new LinkSetObjectManager();
			boolean saveToFiles = DrumbeatApplication.getInstance().isSavingUploadEnabled();
			
			BooleanParam clearBeforeParam = new BooleanParam();
			clearBeforeParam.setStringValue(clearBefore);

			BooleanParam notifyRemoteParam = new BooleanParam();
			notifyRemoteParam.setStringValue(notifyRemote);

			Model linkSetInfoModel = objectManager.upload(
					collectionId,
					linkSourceId,
					linkSetId,
					dataType,
					dataFormat,
					clearBeforeParam.getValue(),
					notifyRemoteParam.getValue(),
					in,
					saveToFiles);
			
			return DrumbeatResponseBuilder.build(
					Status.CREATED,
					linkSetInfoModel,
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
	
//	@Path("/{collectionId}/{linkSourceId}/{linkSetId}/createLinkSet")
//	@PUT
//	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response createLinkSet(
//			@PathParam("collectionId") String collectionId,
//			@PathParam("linkSourceId") String linkSourceId,
//			@PathParam("linkSetId") String linkSetId,
//			@FormParam("sourceUrl") String sourceUrl,
//			@FormParam("targetUrl") String targetUrl,
//			@Context UriInfo uriInfo,
//			@Context HttpHeaders headers)
//	{
//		onRequest(uriInfo, headers, request);
//
//		String graphName = NameFormatter.formatGraphUri(collectionId, linkSourceId, linkSetId);
//		logger.info(String.format("CreateLinkSet: Name=%s, Source=%s, Target=%s", graphName, sourceUrl, targetUrl));
//		
//		return getLinkSetManager().createLinkSet(collectionId, linkSourceId, linkSetId, sourceUrl, targetUrl);
//	}	
	
	
	@POST
	@Path("/{collectionId}/{linkSourceId}/{linkSetId}/generateLinks")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response generateLinks(
			@PathParam("collectionId") String collectionId,
			@PathParam("linkSourceId") String linkSourceId,
			@PathParam("linkSetId") String linkSetId,
			@DefaultValue("false") @FormParam("clearBefore") String clearBefore,
			@FormParam("linkType") String linkType,
			@FormParam("localDataSourceUri") String localDataSourceUri,
			@FormParam("remoteDataSourceUri") String remoteDataSourceUri,
			@DefaultValue("false") @FormParam("notifyRemote") String notifyRemote,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers,
			@Context HttpServletRequest request)
	{
		notifyRequest(uriInfo, headers, request);

		String graphName = NameFormatter.formatLinkSetGraphUri(collectionId, linkSourceId, linkSetId);			
		logger.info(String.format("UpdateSparql: LinkSet=%s, LinkType=%s", graphName, linkType));
		
		try {
			LinkSetObjectManager objectManager = new LinkSetObjectManager();
			
			BooleanParam clearBeforeParam = new BooleanParam();
			clearBeforeParam.setStringValue(clearBefore);

			BooleanParam notifyRemoteParam = new BooleanParam();
			notifyRemoteParam.setStringValue(notifyRemote);

			Model linkSetInfoModel = objectManager.generateLinks(
					collectionId,
					linkSourceId,
					linkSetId,
					clearBeforeParam.getValue(),
					linkType,
					localDataSourceUri,
					remoteDataSourceUri,
					notifyRemoteParam.getValue());
			
			return DrumbeatResponseBuilder.build(
					Status.CREATED,
					linkSetInfoModel,
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
		}
	}

	
	private LinkSetManager getLinkSetManager() {
		try {
			return new LinkSetManager();
		} catch (DrumbeatException e) {
			logger.error(e);
			throw new DrumbeatWebException(
					Status.INTERNAL_SERVER_ERROR,
					"Error getting LinkSetManager instance: " + e.getMessage(),
					e);
		}
	}
	
	
}