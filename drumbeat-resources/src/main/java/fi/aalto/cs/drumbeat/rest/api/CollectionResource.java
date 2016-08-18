package fi.aalto.cs.drumbeat.rest.api;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.AlreadyExistsException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.NotFoundException;
import org.apache.log4j.Logger;

import fi.aalto.cs.drumbeat.common.DrumbeatException;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatResponseBuilder;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatWebException;
import fi.aalto.cs.drumbeat.rest.managers.CollectionManager;


@Path("/collections")
public class CollectionResource {

	private static final Logger logger = Logger.getLogger(CollectionResource.class);

	@RolesAllowed("all")
	@GET
	public Response getAll(			
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
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
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
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
	
	@RolesAllowed("architect")
	@DELETE
	@Path("/{collectionId}")
	public void delete(			
			@PathParam("collectionId") String collectionId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
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
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
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