package fi.aalto.cs.drumbeat.rest.api;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.AlreadyExistsException;
import com.hp.hpl.jena.shared.NotFoundException;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatWebException;
import fi.aalto.cs.drumbeat.rest.managers.CollectionManager;
import fi.hut.cs.drumbeat.common.DrumbeatException;


@Path("/collections")
public class CollectionResource {

	private static final Logger logger = Logger.getLogger(CollectionResource.class);

	@GET
	@Path("/")
	public String getAll(			
			@PathParam("collectionId") String collectionId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {		
			Model model = getCollectionManager().getAll();
			return ModelToMediaTypeConverter.convertModelToAcceptableMediaTypes(
					model,
					headers.getAcceptableMediaTypes());			
		} catch (NotFoundException exception) {
			throw new DrumbeatWebException(
					Response.Status.NOT_FOUND,
					exception.getMessage(),
					exception);
		}
	}
	
	@GET
	@Path("/{collectionId}")
	public String getById(			
			@PathParam("collectionId") String collectionId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {		
			Model model = getCollectionManager().getById(collectionId);
			return ModelToMediaTypeConverter.convertModelToAcceptableMediaTypes(
					model,
					headers.getAcceptableMediaTypes());			
		} catch (NotFoundException exception) {
			throw new DrumbeatWebException(
					Response.Status.NOT_FOUND,
					exception.getMessage(),
					exception);
		}
	}
	
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
					Response.Status.NOT_FOUND,
					exception.getMessage(),
					exception);
		}
	}
	
	@POST
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
			String entity = ModelToMediaTypeConverter.convertModelToAcceptableMediaTypes(
					model,
					headers.getAcceptableMediaTypes());
			return Response
					.status(Response.Status.CREATED)
					.entity(entity)
					.build();
		} catch (NotFoundException exception) {
			throw new DrumbeatWebException(
					Response.Status.NOT_FOUND,
					exception.getMessage(),
					exception);
		} catch (AlreadyExistsException exception) {
			throw new DrumbeatWebException(
					Response.Status.CONFLICT,
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
					Response.Status.INTERNAL_SERVER_ERROR,
					"Error getting CollectionManager instance: " + e.getMessage(),
					e);
		}
	}
	
	
}