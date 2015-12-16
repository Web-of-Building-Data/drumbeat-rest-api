package fi.aalto.cs.drumbeat.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.NotFoundException;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatResponseBuilder;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatWebException;
import fi.aalto.cs.drumbeat.rest.managers.ObjectManager;
import fi.hut.cs.drumbeat.common.DrumbeatException;

@Path("/objects")
public class ObjectResource {

	private static final Logger logger = Logger.getLogger(ObjectResource.class);

	private ObjectManager getObjectManager() {
		try {
			return new ObjectManager();
		} catch (DrumbeatException e) {
			logger.error(e);
			throw new DrumbeatWebException(
					Status.INTERNAL_SERVER_ERROR,
					"Error getting ObjectManager instance: " + e.getMessage(),
					e);
		}
	}
	
	@GET
	@Path("/{collectionId}/{dataSourceId}/{dataSetId}/{objectId}")
	public Response getById(			
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@PathParam("dataSetId") String dataSetId,
			@PathParam("objectId") String objectId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {		
			Model model = getObjectManager().getById(collectionId, dataSourceId, dataSetId, objectId);
			return DrumbeatResponseBuilder.build(
					Status.OK,
					model,
					headers.getAcceptableMediaTypes());			
		} catch (NotFoundException e) {
			throw new DrumbeatWebException(Status.NOT_FOUND, e);
		} catch (DrumbeatException e) {
			throw new DrumbeatWebException(Status.INTERNAL_SERVER_ERROR, e);			
		}
	}		

	@GET
	@Path("/{collectionId}/{dataSourceId}/{dataSetId}/{objectId}/type")
	public Response getType(			
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@PathParam("dataSetId") String dataSetId,
			@PathParam("objectId") String objectId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {		
			Model model = getObjectManager().getObjectType(collectionId, dataSourceId, dataSetId, objectId);
			return DrumbeatResponseBuilder.build(
					Status.OK,
					model,
					headers.getAcceptableMediaTypes());			
		} catch (NotFoundException e) {
			throw new DrumbeatWebException(Status.NOT_FOUND, e);
		} catch (DrumbeatException e) {
			throw new DrumbeatWebException(Status.INTERNAL_SERVER_ERROR, e);			
		}
	}		

}
