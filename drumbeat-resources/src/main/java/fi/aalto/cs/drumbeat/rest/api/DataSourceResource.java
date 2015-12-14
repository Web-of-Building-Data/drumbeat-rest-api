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
import fi.aalto.cs.drumbeat.rest.managers.DataSourceManager;
import fi.hut.cs.drumbeat.common.DrumbeatException;


@Path("/datasources")
public class DataSourceResource {

	private static final Logger logger = Logger.getLogger(DataSourceResource.class);
	
	
	@GET
	@Path("/{collectionId}")
	public String getAll(			
			@PathParam("collectionId") String collectionId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {		
			Model model = getDataSourceManager().getAll(collectionId);
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
	@Path("/{collectionId}/{dataSourceId}")
	public String getById(			
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {		
			Model model = getDataSourceManager().getById(collectionId, dataSourceId);
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
		} catch (NotFoundException exception) {
			throw new DrumbeatWebException(
					Response.Status.NOT_FOUND,
					exception.getMessage(),
					exception);
		}
	}
	
	@POST
	@Path("/{collectionId}/{dataSourceId}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String create(
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@FormParam("name") String name,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {
			Model model = getDataSourceManager().create(collectionId, dataSourceId, name);
			return ModelToMediaTypeConverter.convertModelToAcceptableMediaTypes(
					model,
					headers.getAcceptableMediaTypes());			
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
	
	
	private DataSourceManager getDataSourceManager() {
		try {
			return new DataSourceManager();
		} catch (DrumbeatException e) {
			logger.error(e);
			throw new DrumbeatWebException(
					Response.Status.INTERNAL_SERVER_ERROR,
					"Error getting DataSourceManager instance: " + e.getMessage(),
					e);
		}
	}
	
	
}