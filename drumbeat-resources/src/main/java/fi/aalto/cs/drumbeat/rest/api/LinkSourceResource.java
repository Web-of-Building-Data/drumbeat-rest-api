package fi.aalto.cs.drumbeat.rest.api;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.AlreadyExistsException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.NotFoundException;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatResponseBuilder;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatWebException;
import fi.aalto.cs.drumbeat.rest.managers.LinkSourceManager;
import fi.aalto.cs.drumbeat.common.DrumbeatException;


@Path("/linksources")
public class LinkSourceResource {

	private static final Logger logger = Logger.getLogger(LinkSourceResource.class);
	
	
	@GET
	@Path("/{collectionId}")
	public Response getAll(			
			@PathParam("collectionId") String collectionId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {		
			Model model = getLinkSourceManager().getAll(collectionId);
			return DrumbeatResponseBuilder.build(
					Status.OK,
					model,
					headers.getAcceptableMediaTypes());			
		} catch (NotFoundException e) {
			throw new DrumbeatWebException(Status.NOT_FOUND, e);
		}
	}
	
	@GET
	@Path("/{collectionId}/{linkSourceId}")
	public Response getById(			
			@PathParam("collectionId") String collectionId,
			@PathParam("linkSourceId") String linkSourceId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {		
			Model model = getLinkSourceManager().getById(collectionId, linkSourceId);
			return DrumbeatResponseBuilder.build(
					Status.OK,
					model,
					headers.getAcceptableMediaTypes());			
		} catch (NotFoundException e) {
			throw new DrumbeatWebException(Status.NOT_FOUND, e);
		}
	}
	
	@DELETE
	@Path("/{collectionId}/{linkSourceId}")
	public void delete(			
			@PathParam("collectionId") String collectionId,
			@PathParam("linkSourceId") String linkSourceId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {		
			getLinkSourceManager().delete(collectionId, linkSourceId);
		} catch (NotFoundException e) {
			throw new DrumbeatWebException(Status.NOT_FOUND, e);
		} catch (DeleteDeniedException e) {
			throw new DrumbeatWebException(Status.FORBIDDEN, e);
		}
	}
	
	@PUT
	@Path("/{collectionId}/{linkSourceId}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response create(
			@PathParam("collectionId") String collectionId,
			@PathParam("linkSourceId") String linkSourceId,
			@FormParam("name") String name,
			@FormParam("originalDataSourceId") String originalDataSourceId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {
			Model model = getLinkSourceManager().create(collectionId, linkSourceId, name, originalDataSourceId);
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
	
	
	private LinkSourceManager getLinkSourceManager() {
		try {
			return new LinkSourceManager();
		} catch (DrumbeatException e) {
			logger.error(e);
			throw new DrumbeatWebException(
					Status.INTERNAL_SERVER_ERROR,
					"Error getting LinkSourceManager instance: " + e.getMessage(),
					e);
		}
	}
	
	
}