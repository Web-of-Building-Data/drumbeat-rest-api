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
import fi.aalto.cs.drumbeat.rest.managers.DataSourceObjectManager;
import fi.aalto.cs.drumbeat.rest.ontology.LinkedBuildingDataOntology;
import fi.aalto.cs.drumbeat.common.DrumbeatException;

@Path("/objects")
public class DataSourceObjectResource {

	private static final Logger logger = Logger.getLogger(DataSourceObjectResource.class);

	private DataSourceObjectManager getObjectManager() {
		try {
			return new DataSourceObjectManager();
		} catch (DrumbeatException e) {
			logger.error(e);
			throw new DrumbeatWebException(
					Status.INTERNAL_SERVER_ERROR,
					"Error getting ObjectManager instance: " + e.getMessage(),
					e);
		}
	}
	
//	@GET
//	@Path("/{collectionId}/{dataSourceId}")
//	public Response getAll(			
//			@PathParam("collectionId") String collectionId,
//			@PathParam("dataSourceId") String dataSourceId,
//			@Context UriInfo uriInfo,
//			@Context HttpHeaders headers)
//	{
//		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
//		
//		try {		
//			Model model = getObjectManager().getAll(collectionId, dataSourceId);
//			String modelBaseUri = LinkedBuildingDataOntology.formatObjectResourceBaseUri(collectionId, dataSourceId);
//			return DrumbeatResponseBuilder.build(
//					Status.OK,
//					model,
//					modelBaseUri,					
//					headers.getAcceptableMediaTypes());			
//		} catch (NotFoundException e) {
//			throw new DrumbeatWebException(Status.NOT_FOUND, e);
//		} catch (DrumbeatException e) {
//			throw new DrumbeatWebException(Status.INTERNAL_SERVER_ERROR, e);			
//		}
//	}		

	@GET
	@Path("/{collectionId}/{dataSourceId}/{objectId}")
	public Response getById(			
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@PathParam("objectId") String objectId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {		
			Model model = getObjectManager().getById(collectionId, dataSourceId, objectId);
			String modelBaseUri = LinkedBuildingDataOntology.formatObjectResourceBaseUri(collectionId, dataSourceId);
			return DrumbeatResponseBuilder.build(
					Status.OK,
					model,
					modelBaseUri,
					headers.getAcceptableMediaTypes());			
		} catch (NotFoundException e) {
			throw new DrumbeatWebException(Status.NOT_FOUND, e);
		} catch (DrumbeatException e) {
			throw new DrumbeatWebException(Status.INTERNAL_SERVER_ERROR, e);			
		}
	}		

//	@GET
//	@Path("/{collectionId}/{dataSourceId}/{objectId}/type")
//	public Response getType(			
//			@PathParam("collectionId") String collectionId,
//			@PathParam("dataSourceId") String dataSourceId,
//			@PathParam("objectId") String objectId,
//			@Context UriInfo uriInfo,
//			@Context HttpHeaders headers)
//	{
//		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
//		
//		try {		
//			Model model = getObjectManager().getObjectType(collectionId, dataSourceId, objectId);
//			String modelBaseUri = LinkedBuildingDataOntology.formatObjectResourceBaseUri(collectionId, dataSourceId);
//			return DrumbeatResponseBuilder.build(
//					Status.OK,
//					model,
//					modelBaseUri,
//					headers.getAcceptableMediaTypes());			
//		} catch (NotFoundException e) {
//			throw new DrumbeatWebException(Status.NOT_FOUND, e);
//		} catch (DrumbeatException e) {
//			throw new DrumbeatWebException(Status.INTERNAL_SERVER_ERROR, e);			
//		}
//	}		

}
