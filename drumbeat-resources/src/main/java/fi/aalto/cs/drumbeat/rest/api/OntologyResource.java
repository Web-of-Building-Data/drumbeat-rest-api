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
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.AlreadyExistsException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.NotFoundException;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatResponseBuilder;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatWebException;
import fi.aalto.cs.drumbeat.rest.common.NameFormatter;
import fi.aalto.cs.drumbeat.rest.managers.OntologyManager;
import fi.aalto.cs.drumbeat.common.DrumbeatException;
import fi.aalto.cs.drumbeat.common.file.FileManager;
import fi.aalto.cs.drumbeat.common.params.BooleanParam;
import fi.aalto.cs.drumbeat.common.string.StringUtils;
import fi.aalto.cs.drumbeat.ifc.convert.stff2ifc.IfcParserException;


@Path("/owl")
public class OntologyResource {

	private static final Logger logger = Logger.getLogger(OntologyResource.class);

	@GET
	public Response getAll(			
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {		
			Model model = getOntologyManager().getAll();
			return DrumbeatResponseBuilder.build(
					Status.OK,
					model,
					headers.getAcceptableMediaTypes());			
		} catch (NotFoundException exception) {
			throw new DrumbeatWebException(
					Status.NOT_FOUND,
					exception.getMessage(),
					exception);
		}
	}
	
	@GET
	@Path("/{ontologyId}")
	public Response getById(			
			@PathParam("ontologyId") String ontologyId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {		
			Model model = getOntologyManager().getById(ontologyId);
			return DrumbeatResponseBuilder.build(
					Status.OK,
					model,
					headers.getAcceptableMediaTypes());			
		} catch (NotFoundException exception) {
			throw new DrumbeatWebException(
					Status.NOT_FOUND,
					exception.getMessage(),
					exception);
		}
	}
	
	@DELETE
	@Path("/{ontologyId}")
	public void delete(			
			@PathParam("ontologyId") String ontologyId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {		
			getOntologyManager().delete(ontologyId);
		} catch (NotFoundException exception) {
			throw new DrumbeatWebException(
					Status.NOT_FOUND,
					exception.getMessage(),
					exception);
		} catch (DeleteDeniedException exception) {
			throw new DrumbeatWebException(
					Status.FORBIDDEN,
					exception.getMessage(),
					exception);			
		}
	}
	
	@PUT
	@Path("/{ontologyId}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response create(			
			@PathParam("ontologyId") String ontologyId,
			@FormParam("name") String name,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {
			Model model = getOntologyManager().create(ontologyId, name);
			return DrumbeatResponseBuilder.build(
					Status.CREATED,
					model,
					headers.getAcceptableMediaTypes());
		} catch (NotFoundException exception) {
			throw new DrumbeatWebException(
					Status.NOT_FOUND,
					exception.getMessage(),
					exception);
		} catch (AlreadyExistsException exception) {
			throw new DrumbeatWebException(
					Status.CONFLICT,
					exception.getMessage(),
					exception);			
		}
	}
	
	
	@POST
	@Path("/{ontologyId}/uploadServerFile")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response uploadServerFile(
			@PathParam("ontologyId") String ontologyId,
			@FormParam("dataType") String dataType,
			@DefaultValue("") @FormParam("dataFormat") String dataFormat,
			@DefaultValue("false") @FormParam("clearBefore") String clearBefore,
			@FormParam("filePath") String filePath,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		if (StringUtils.isEmptyOrNull(dataFormat)) {
			dataFormat = FileManager.getFileName(filePath);
		}		

		String ontologyUri = NameFormatter.formatLocalOntologyUri(ontologyId);
		logger.info(String.format("UploadServerFile: DataSet=%s, ServerFilePath=%s", ontologyUri, filePath));
		
		InputStream in;
		try {
			in = new FileInputStream(filePath);
		} catch (FileNotFoundException e) {
			throw new DrumbeatWebException(Status.NOT_FOUND, e);
		}
		
		return internalUploadDataSet(ontologyId, dataType, dataFormat, clearBefore, in, headers);
	}
	
	@POST
	@Path("/{ontologyId}/uploadUrl")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response uploadUrl(
			@PathParam("ontologyId") String ontologyId,
			@FormParam("dataType") String dataType,
			@DefaultValue("") @FormParam("dataFormat") String dataFormat,
			@DefaultValue("false") @FormParam("clearBefore") String clearBefore,
			@FormParam("url") String url,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);

		if (StringUtils.isEmptyOrNull(dataFormat)) {
			throw new DrumbeatWebException(Status.BAD_REQUEST, "Undefined param 'dataFormat'", null);
		}

		String ontologyUri = NameFormatter.formatLocalOntologyUri(ontologyId);			
		logger.info(String.format("UploadUrl: DataSet=%s, Url=%s", ontologyUri, url));
		
		InputStream in;
		try {
			in = new URL(url).openStream();
		} catch (IOException e) {			
			throw new DrumbeatWebException(Status.NOT_FOUND, e);
		}
		
		return internalUploadDataSet(ontologyId, dataType, dataFormat, clearBefore, in, headers);
	}


	@POST
	@Path("/{ontologyId}/uploadContent")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response uploadContent(
			@PathParam("ontologyId") String ontologyId,
			@FormParam("dataType") String dataType,
			@DefaultValue("") @FormParam("dataFormat") String dataFormat,			
			@DefaultValue("false") @FormParam("clearBefore") String clearBefore,
			@FormParam("content") String content,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);

		if (StringUtils.isEmptyOrNull(dataFormat)) {
			throw new DrumbeatWebException(Status.BAD_REQUEST, "Undefined param 'dataFormat'", null);
		}		
		
		String ontologyUri = NameFormatter.formatLocalOntologyUri(ontologyId);			
		logger.info(String.format("UploadContent: DataSet=%s, Content=%s", ontologyUri, content));
		
		InputStream in = new ByteArrayInputStream(content.getBytes());
		return internalUploadDataSet(ontologyId, dataType, dataFormat, clearBefore, in, headers);
	}

	
	@POST
	@Path("/{ontologyId}/uploadClientFile")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadClientFile(
			@PathParam("ontologyId") String ontologyId,
			@FormDataParam("file") InputStream in,
	        @FormDataParam("file") FormDataContentDisposition fileDetail,
			@FormDataParam("dataType") String dataType,
			@DefaultValue("") @FormDataParam("dataFormat") String dataFormat,
			@DefaultValue("false") @FormDataParam("clearBefore") String clearBefore,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		if (fileDetail == null || in == null) {
			throw new DrumbeatWebException(Status.BAD_REQUEST, "Client file is unavailable", null);			
		}
	        
		if (StringUtils.isEmptyOrNull(dataFormat)) {
			dataFormat = fileDetail.getFileName();
		}

		String ontologyUri = NameFormatter.formatLocalOntologyUri(ontologyId);
		logger.info(String.format("UploadContent: DataSet=%s, FileName=%s", ontologyUri, fileDetail.getFileName()));		

		return internalUploadDataSet(ontologyId, dataType, dataFormat, clearBefore, in, headers);
	}
	
	private Response internalUploadDataSet(
			String ontologyId,
			String dataType,
			String dataFormat,
			String clearBefore,
			InputStream in,
			HttpHeaders headers)
	{	
		
		try {
			OntologyManager ontologyManager = new OntologyManager();
			boolean saveToFiles = DrumbeatApplication.getInstance().isSavingUploadEnabled();
			
			BooleanParam clearBeforeParam = new BooleanParam();
			clearBeforeParam.setStringValue(clearBefore);

			Model dataSetInfoModel = ontologyManager.upload(
					ontologyId,
					dataType,
					dataFormat,
					clearBeforeParam.getValue(),
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
	
	
	private OntologyManager getOntologyManager() {
		try {
			return new OntologyManager();
		} catch (DrumbeatException e) {
			logger.error(e);
			throw new DrumbeatWebException(
					Status.INTERNAL_SERVER_ERROR,
					"Error getting OntologyManager instance: " + e.getMessage(),
					e);
		}
	}
	
	
}