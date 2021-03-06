package fi.aalto.cs.drumbeat.rest.api;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.AlreadyExistsException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.NotFoundException;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatResponseBuilder;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatWebException;
import fi.aalto.cs.drumbeat.rest.managers.CollectionManager;
import fi.aalto.cs.drumbeat.common.DrumbeatException;


@Path("/collections")
public class CollectionResource extends DrumbeatApiBase {

	private static final Logger logger = Logger.getLogger(CollectionResource.class);

	@GET
	public Response getAll(			
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers,
			@Context HttpServletRequest request)
	{
		notifyRequest(uriInfo, headers, request);
		
		try {		
			Model model = getCollectionManager().getAll();
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
	@Path("/{collectionId}")
	public Response getById(			
			@PathParam("collectionId") String collectionId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers,
			@Context HttpServletRequest request)
	{
		notifyRequest(uriInfo, headers, request);
		
		try {		
			Model model = getCollectionManager().getById(collectionId);
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
	@Path("/{collectionId}")
	public void delete(			
			@PathParam("collectionId") String collectionId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers,
			@Context HttpServletRequest request)
	{
		notifyRequest(uriInfo, headers, request);
		
		try {		
			getCollectionManager().delete(collectionId);
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
	@Path("/{collectionId}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response create(			
			@PathParam("collectionId") String collectionId,
			@FormParam("name") String name,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers,
			@Context HttpServletRequest request)
	{
		notifyRequest(uriInfo, headers, request);
		
		try {
			Model model = getCollectionManager().create(collectionId, name);
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
	
	
	private CollectionManager getCollectionManager() {
		try {
			return new CollectionManager();
		} catch (DrumbeatException e) {
			logger.error(e);
			throw new DrumbeatWebException(
					Status.INTERNAL_SERVER_ERROR,
					"Error getting CollectionManager instance: " + e.getMessage(),
					e);
		}
	}
	
	
}