package fi.aalto.cs.drumbeat.rest.api;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.AlreadyExistsException;
import com.hp.hpl.jena.shared.DeleteDeniedException;
import com.hp.hpl.jena.shared.NotFoundException;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatResponseBuilder;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatWebException;
import fi.aalto.cs.drumbeat.rest.managers.DataSourceManager;
import fi.aalto.cs.drumbeat.common.DrumbeatException;


@Path("/{elementType: datasources|linksources}")
//@Path("/datasources")
public class DataSourceResource {

	private static final Logger logger = Logger.getLogger(DataSourceResource.class);
	
	
	@GET
	@Path("/{collectionId}")
	public Response getAll(			
			@PathParam("collectionId") String collectionId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {		
			Model model = getDataSourceManager().getAll(collectionId);
			return DrumbeatResponseBuilder.build(
					Status.OK,
					model,
					headers.getAcceptableMediaTypes());			
		} catch (NotFoundException e) {
			throw new DrumbeatWebException(Status.NOT_FOUND, e);
		}
	}
	
	@GET
	@Path("/{collectionId}/{dataSourceId}")
	public Response getById(			
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {		
			Model model = getDataSourceManager().getById(collectionId, dataSourceId);
			return DrumbeatResponseBuilder.build(
					Status.OK,
					model,
					headers.getAcceptableMediaTypes());			
		} catch (NotFoundException e) {
			throw new DrumbeatWebException(Status.NOT_FOUND, e);
		}
	}
	
	@DELETE
	@Path("/{collectionId}/{dataSourceId}")
	public void delete(			
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {		
			getDataSourceManager().delete(collectionId, dataSourceId);
		} catch (NotFoundException e) {
			throw new DrumbeatWebException(Status.NOT_FOUND, e);
		} catch (DeleteDeniedException e) {
			throw new DrumbeatWebException(Status.FORBIDDEN, e);
		}
	}
	
	@PUT
	@Path("/{collectionId}/{dataSourceId}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response create(
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@FormParam("name") String name,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {
			Model model = getDataSourceManager().create(collectionId, dataSourceId, name);
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
	
	
	private DataSourceManager getDataSourceManager() {
		try {
			return new DataSourceManager();
		} catch (DrumbeatException e) {
			logger.error(e);
			throw new DrumbeatWebException(
					Status.INTERNAL_SERVER_ERROR,
					"Error getting DataSourceManager instance: " + e.getMessage(),
					e);
		}
	}
	
	
}