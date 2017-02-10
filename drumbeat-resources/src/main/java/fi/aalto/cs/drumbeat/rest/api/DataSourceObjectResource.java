package fi.aalto.cs.drumbeat.rest.api;


import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.AlreadyExistsException;
import org.apache.jena.shared.NotFoundException;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatOntology;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatResponseBuilder;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatWebException;
import fi.aalto.cs.drumbeat.rest.common.NameFormatter;
import fi.aalto.cs.drumbeat.rest.managers.DataSourceObjectManager;
import fi.aalto.cs.drumbeat.common.DrumbeatException;
import fi.aalto.cs.drumbeat.common.params.BooleanParam;
import fi.aalto.cs.drumbeat.common.string.StringUtils;

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
//			String modelBaseUri = NameFormatter.formatObjectResourceBaseUri(collectionId, dataSourceId);
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
	@Path("/{collectionId}/{dataSourceId}")
	public Response getAllNonBlank(			
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@QueryParam("filterType") String filterType,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {		
			Model model = getObjectManager().getAllNonBlank(collectionId, dataSourceId, filterType);
			String modelBaseUri = NameFormatter.formatObjectResourceBaseUri(collectionId, dataSourceId);
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
	
	

	@GET
	@Path("/{collectionId}/{dataSourceId}/{objectId}")
	public Response getById(			
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@PathParam("objectId") String objectId,
			@QueryParam("excludeProperties") String excludeProperties,
			@QueryParam("excludeLinks") String excludeLinks,
			@QueryParam("expandBlankObjects") String expandBlankObjects,
			@QueryParam("filterProperties") String filterProperties,
			@QueryParam("filterObjectTypes") String filterObjectTypes,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		String objectUri = NameFormatter.formatObjectResourceUri(collectionId, dataSourceId, objectId);
		return internalGetByUri(
				collectionId,
				dataSourceId,
				null,
				objectUri,
				excludeProperties,
				excludeLinks,
				expandBlankObjects,				
				filterProperties,
				filterObjectTypes,
				headers);
	}
	
	
	@GET
	@Path("/{collectionId}/{dataSourceId}/{dataSetId}/" + NameFormatter.BLANK_NODE_PATH + "/{objectId}")	
//	@Path("/{collectionId}/{dataSourceId}/{dataSetId}/{objectId}")
	public Response getBlankById(			
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@PathParam("dataSetId") String dataSetId,
			@PathParam("objectId") String objectId,
			@QueryParam("excludeProperties") String excludeProperties,
			@QueryParam("excludeLinks") String excludeLinks,
			@QueryParam("expandBlankObjects") String expandBlankObjects,
			@QueryParam("filterProperties") String filterProperties,
			@QueryParam("filterObjectTypes") String filterObjectTypes,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		String objectUri = NameFormatter.formatBlankObjectResourceUri(collectionId, dataSourceId, dataSetId, objectId);
		return internalGetByUri(collectionId, dataSourceId, dataSetId, objectUri, excludeProperties, excludeLinks, expandBlankObjects, filterProperties, filterObjectTypes, headers);
	}
	
	
	private Response internalGetByUri(
			String collectionId,
			String dataSourceId,
			String dataSetId,
			String objectUri,
			String excludeProperties,
			String excludeLinks,
			String expandBlankObjects,
			String filterProperties,
			String filterObjectTypes,
			HttpHeaders headers)
	{
		BooleanParam excludePropertiesParam = new BooleanParam();
		excludePropertiesParam.setStringValue(excludeProperties);		

		BooleanParam excludeLinksParam = new BooleanParam();
		excludeLinksParam.setStringValue(excludeLinks);		
		
		BooleanParam expandBlankObjectsParam = new BooleanParam();
		expandBlankObjectsParam.setStringValue(expandBlankObjects);		

		try {		
			Model model = getObjectManager().getByUri(
					collectionId,
					dataSourceId,
					null,
					objectUri,
					excludePropertiesParam.getValue(),
					excludeLinksParam.getValue(),
					expandBlankObjectsParam.getValue(),
					filterProperties,
					filterObjectTypes);
			String modelBaseUri = NameFormatter.formatObjectResourceBaseUri(collectionId, dataSourceId);
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
	
	
	

	@GET
	@Path("/{collectionId}/{dataSourceId}/{objectId}/type")
	public Response getType(			
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@PathParam("objectId") String objectId,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		String objectUri = NameFormatter.formatObjectResourceUri(collectionId, dataSourceId, objectId);
		return internalGetByUri(collectionId, dataSourceId, null, objectUri, "true", "true", "false", null, null, headers);
	}
	
	
	@PUT
	@Path("/{collectionId}/{dataSourceId}/{objectId}/linkCreated")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void linkCreated(
			@PathParam("collectionId") String collectionId,
			@PathParam("dataSourceId") String dataSourceId,
			@PathParam("objectId") String objectId,
			@FormParam("subject") String subjectUri,
			@FormParam("predicate") String predicateUri,
			@FormParam("object") String objectUri,
			@Context UriInfo uriInfo,
			@Context HttpHeaders headers)
	{
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		try {
			
			logger.info(String.format("linkCreated: <%s> <%s> <%s>",
					//uriInfo.getAbsolutePath(),
					subjectUri,
					predicateUri,
					objectUri
					));
			
			if (StringUtils.isEmptyOrNull(subjectUri)) {
				throw new DrumbeatWebException(Status.BAD_REQUEST, "Undefined subject", null);
			}
			
			if (StringUtils.isEmptyOrNull(predicateUri)) {
				throw new DrumbeatWebException(Status.BAD_REQUEST, "Undefined predicate", null);
			}

			if (StringUtils.isEmptyOrNull(objectUri)) {
				throw new DrumbeatWebException(Status.BAD_REQUEST, "Undefined object", null);
			}

			DataSourceObjectManager dataSourceObjectManager = new DataSourceObjectManager();

			dataSourceObjectManager.onLinkCreated(subjectUri, predicateUri, objectUri);			
			
		} catch (NotFoundException e) {
			throw new DrumbeatWebException(Status.NOT_FOUND, e);
		} catch (AlreadyExistsException e) {
			throw new DrumbeatWebException(Status.CONFLICT, e);
		} catch (DrumbeatException e) {
			throw new DrumbeatWebException(Status.INTERNAL_SERVER_ERROR, e);
		}
	}	
	
	

}
