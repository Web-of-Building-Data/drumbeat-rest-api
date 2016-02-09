package fi.aalto.cs.drumbeat.rest.managers;

import static fi.aalto.cs.drumbeat.rest.common.NameFormatter.*;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.update.UpdateAction;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatVocabulary;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatOntology;
import fi.aalto.cs.drumbeat.rest.common.MediaTypeConverter;
import fi.aalto.cs.drumbeat.rest.common.NameFormatter;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Calendar;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import fi.aalto.cs.drumbeat.common.DrumbeatException;
import fi.aalto.cs.drumbeat.common.string.StringUtils;
import fi.aalto.cs.drumbeat.rdf.jena.provider.JenaProvider;

public class DataSourceObjectManager extends DrumbeatManager {
	
	private static Logger logger = Logger.getLogger(DataSourceObjectManager.class);
	
	public DataSourceObjectManager() throws DrumbeatException {
	}
	
	public DataSourceObjectManager(Model metaDataModel, JenaProvider jenaProvider) {
		super(metaDataModel, jenaProvider);
	}	

//	public Model getDataModels(String collectionId, String dataSourceId, String dataSetId) throws DrumbeatException {
//		String graphName = formatGraphUri(collectionId, dataSourceId, dataSetId);
//		return DrumbeatApplication.getInstance().getDataModel(graphName);
//	}
	
//	/**
//	 * Gets all attributes of a specified object 
//	 * @param collectionId
//	 * @param dataSourceId
//	 * @return List of statements <<dataSet>> ?predicate ?object
//	 * @throws NotFoundException if the dataSet is not found
//	 * @throws DrumbeatException 
//	 */
//	public Model getAll(String collectionId, String dataSourceId)
//		throws NotFoundException, DrumbeatException
//	{
//		Model dataModel = getDataModels(collectionId, dataSourceId);
//
//		Query query = new ParameterizedSparqlString() {{
//			setCommandText(
//						"CONSTRUCT { \n" +
//						"	?o rdf:type ?type \n" +
//						"} \n " +
////						"FROM NAMED ?dataSetUri \n " +
////						"FROM NAMED ?ifcOwlUri \n " +
//						"WHERE { \n " +
//						"	GRAPH ?dataSetUri { \n " +
//						"		?o a ?type ; ifc:globalId_IfcRoot _:globalId . \n " +
//						"	} \n " +
////						"	GRAPH ?ifcOwlUri { \n " +
////						"   	?type rdfs:subClassOf* ifc:IfcRoot . \n " +
////						"	} \n " +
//						"} \n "
//					);
//			
//			fillParameterizedSparqlString(this);
//			setIri("dataSetUri", formatDataSetResourceUri(collectionId, dataSourceId, dataSetId));
//			setIri("ifcOwlUri", formatOntologyUri("ifc2x3"));
//		}}.asQuery();
//		
//		Model resultModel = 
//				createQueryExecution(query, dataModel)
//					.execConstruct();
//		
//		if (resultModel.isEmpty()) {
//			throw ErrorFactory.createObjectNotFoundException(collectionId, dataSourceId, "");
//		}
//		
//		return resultModel;
//	}
	
	
	/**
	 * Gets all attributes of a specified object 
	 * @param collectionId
	 * @param dataSourceId
	 * @return List of statements <<dataSet>> ?predicate ?object
	 * @throws NotFoundException if the dataSet is not found
	 * @throws DrumbeatException 
	 */
	public Model getById(String collectionId, String dataSourceId, String objectId)
		throws NotFoundException, DrumbeatException
	{
		String objectUri = formatObjectResourceUri(collectionId, dataSourceId, objectId);
		return getByUri(collectionId, dataSourceId, objectUri);
	}
	
	
	
	/**
	 * Gets all attributes of a specified object 
	 * @param collectionId
	 * @param dataSourceId
	 * @return List of statements <<dataSet>> ?predicate ?object
	 * @throws NotFoundException if the dataSet is not found
	 * @throws DrumbeatException 
	 */
	public Model getByUri(String collectionId, String dataSourceId, String objectUri)
		throws NotFoundException, DrumbeatException
	{
		Model metaDataModel = getMetaDataModel();

		DataSetManager dataSetManager = new DataSetManager(metaDataModel, getJenaProvider());
		
		Resource dataSetResource = dataSetManager.getLastDataSetResource(collectionId, dataSourceId);
		if (dataSetResource != null) {
			dataSetResource = dataSetResource.inModel(metaDataModel);
		} else {
			throw ErrorFactory.createObjectNotFoundException(collectionId, dataSourceId, objectUri);			
		}
		
		DataSetObjectManager dataSetObjectManager = new DataSetObjectManager(metaDataModel, getJenaProvider());
		
		Model resultModel;

		for (;;) {
			
			String overwritingMethod = null;
			String overwrittenDataSetUri = null;
			
			if (dataSetResource.hasProperty(DrumbeatOntology.LBDHO.overwritingMethod)) {
				overwritingMethod = dataSetResource
					.getProperty(DrumbeatOntology.LBDHO.overwritingMethod)
					.getObject()
					.asLiteral()
					.getString();
				
				overwrittenDataSetUri = dataSetResource
						.getProperty(DrumbeatOntology.LBDHO.overwrites)
						.getObject()
						.asLiteral()
						.getString();
			}
			
			if (StringUtils.isEmptyOrNull(overwritingMethod)) {
				overwritingMethod = DrumbeatVocabulary.OVERWRITING_METHOD_OVERWRITE_GRAPH;
			}
			
			String dataSetId = dataSetResource.getLocalName();
			
			try {
				resultModel = dataSetObjectManager.getByUri(collectionId, dataSourceId, dataSetId, objectUri);
			} catch (NotFoundException e) {
				resultModel = ModelFactory.createDefaultModel();
			}
			
			if (overwritingMethod.equals(DrumbeatVocabulary.OVERWRITING_METHOD_OVERWRITE_GRAPH)) {
				break;
			} else if (overwritingMethod.equals(DrumbeatVocabulary.OVERWRITING_METHOD_OVERWRITE_OBJECTS)) {
				if (!resultModel.isEmpty()) {
					break;
				}
			} else {
				throw new NotImplementedException(DrumbeatVocabulary.OVERWRITING_METHOD_OVERWRITE_TRIPLES);
			}
			
			dataSetResource = metaDataModel.getResource(overwrittenDataSetUri);	
		}
		
		if (resultModel == null) {
			resultModel = ModelFactory.createDefaultModel();
		}
		
		
		LinkSourceManager linkSourceManager = new LinkSourceManager(metaDataModel, getJenaProvider());
		Model linkSources = linkSourceManager.getAllLinkSourcesOfDataSource(collectionId, dataSourceId);
		
		if (linkSources != null) {
			
			ResIterator resIterator = linkSources.listSubjects();
			
			while (resIterator.hasNext()) {
				Resource linkSourceResource = resIterator.next();
				String linkSourceId = linkSourceResource.getLocalName();
				try {
					Model newResultModel = getByUri(collectionId, linkSourceId, objectUri);
					resultModel.add(newResultModel);
				} catch (NotFoundException e) {					
				}
			}
			
		}
		
		return resultModel;
	}
	
	
//	/**
//	 * Gets type of a specified object 
//	 * @param collectionId
//	 * @param dataSourceId
//	 * @return List of statements <<dataSet>> ?predicate ?object
//	 * @throws NotFoundException if the dataSet is not found
//	 * @throws DrumbeatException 
//	 */
//	public Model getObjectType(String collectionId, String dataSourceId, String objectId)
//		throws NotFoundException, DrumbeatException
//	{
//		Model dataModel = getDataModel(collectionId, dataSourceId, dataSetId);
//		
//		Query query = new ParameterizedSparqlString() {{
//			setCommandText(
//					"SELECT (?objectUri AS ?subject) (rdf:type AS ?predicate) (?type AS ?object) { \n" + 
//					"	?objectUri a ?type . \n" +
//					"} \n" + 
//					"ORDER BY ?subject ?predicate ?object");
//			
//			fillParameterizedSparqlString(this);
//			setIri("objectUri", formatObjectResourceUri(collectionId, dataSourceId, objectId));
//		}}.asQuery();
//		
//		Model resultModel = 
//				createQueryExecution(query, dataModel)
//					.execConstruct();
//		
//		if (resultModel.isEmpty()) {
//			throw ErrorFactory.createObjectNotFoundException(collectionId, dataSourceId, objectId);
//		}
//		
//		return resultModel;
//	}
//	
//	/**
//	 * Gets type of a specified object 
//	 * @param collectionId
//	 * @param dataSourceId
//	 * @param dataSetId
//	 * @return List of statements <<dataSet>> ?predicate ?object
//	 * @throws NotFoundException if the dataSet is not found
//	 * @throws DrumbeatException 
//	 */
//	public Model getObjectProperty(String collectionId, String dataSourceId, String dataSetId, String objectId, String propertyName)
//		throws NotFoundException, DrumbeatException
//	{
//		Model dataModel = getDataModel(collectionId, dataSourceId, dataSetId);
//		
//		Query query = new ParameterizedSparqlString() {{
//			setCommandText(
//					"SELECT ?typeUri { \n" + 
//					"	?objectUri a ?typeUri . \n" +
//					"} \n" + 
//					"ORDER BY ?subject ?predicate ?object");
//			
//			fillParameterizedSparqlString(this);
//			setIri("objectUri", formatObjectResourceUri(collectionId, dataSourceId, dataSetId, objectId));
//		}}.asQuery();
//		
//		Model resultModel = 
//				createQueryExecution
//					.create(query, dataModel)
//					.execConstruct();
//		
//		if (resultModel.isEmpty()) {
//			throw ErrorFactory.createObjectNotFoundException(collectionId, dataSourceId, dataSetId, objectId);
//		}
//		
//		while (resultSet.hasNext()) {
//			Resource typeResource = resultSet.next().get("typeUri").asResource();
//			
//			String ifcModelName = formatGraphName("owl", "ifc", "ifc2x3");
//			Model ifcModel = DrumbeatApplication.getInstance().getDataModel(ifcModelName);
//			
//			
//		}
//		
//		 
//		
//		return resultModel;
//	}
//	
//	private Model internalGetObjectProperty(
//			String collectionId, String dataSourceId, String dataSetId, String objectId, String propertyName, Resource typeResource, Model ifcModel)
//	{
//		Query query = new ParameterizedSparqlString() {{
//			setCommandText(
//					"SELECT ?type { \n" + 
//					"	?objectUri a ?type . \n" +
//					"} \n" + 
//					"ORDER BY ?subject ?predicate ?object");
//			
//			fillParameterizedSparqlString(this);
//			setIri("objectUri", formatObjectResourceUri(collectionId, dataSourceId, dataSetId, objectId));
//		}}.asQuery();
//		
////		Model resultModel = 
////				createQueryExecution(query, dataModel)
////					.execConstruct();
////		
////		if (resultModel.isEmpty()) {
////			return null;
////		}
//		
//		ModelFactory.createOntologyModel(OntModelSpec., base)
//		
//	}
	
	
	
	/**
	 * Imports data set from an input stream
	 * @param collectionId
	 * @param dataSourceId
	 * @param dataSetId
	 * @param dataType
	 * @param dataFormat
	 * @param overwritingMehod
	 * @param in
	 * @param saveFiles
	 * @return
	 * @throws NotFoundException
	 */
	public Model upload(
			String collectionId,
			String dataSourceId,
			String dataSetId,
			String dataType,
			String dataFormat,
			String compressionFormat,
			boolean clearBefore,
			boolean notifyRemote,
			InputStream in,
			boolean saveToFiles)
		throws NotFoundException, IllegalArgumentException, Exception
	{
		//
		// Checking if dataSet exists
		//		
		DataSetManager dataSetManager = new DataSetManager();
		if (!dataSetManager.checkExists(collectionId, dataSourceId, dataSetId)) {
			throw ErrorFactory.createDataSetNotFoundException(collectionId, dataSourceId, dataSetId);
		}
		
		//
		// Format graphUri
		//		
		String graphUri = formatDataSetGraphUri(collectionId, dataSourceId, dataSetId);
		String graphBaseUri = formatObjectResourceBaseUri(collectionId, dataSourceId);
		
		//
		// Read input stream to target model
		//
		Model targetModel = new DataSetUploadManager().upload(graphUri, graphBaseUri, dataType, dataFormat, compressionFormat, clearBefore, in, saveToFiles);
		
		//
		// Update meta data model
		//
		String dataSetUri = formatDataSetResourceUri(collectionId, dataSourceId, dataSetId);
		updateMetaModelAfterUploading(dataSetUri, graphUri, graphBaseUri, targetModel.size());
		
		String applicationBaseUri = DrumbeatApplication.getInstance().getBaseUri();
		
		DataSetObjectManager dataSetObjectManager = new DataSetObjectManager(getMetaDataModel(), getJenaProvider());		
		
		if (notifyRemote) {
			
			StmtIterator stmtIterator = targetModel.listStatements();
			
			while (stmtIterator.hasNext()) {
				
				Statement statement = stmtIterator.next();
				
				RDFNode object = statement.getObject();
				if (object.isURIResource()) {
					
					String objectUri = object.asResource().getURI();
					
					if (objectUri.startsWith(applicationBaseUri)) {
						
						onLinkCreated(statement);
							
					} else {
						
						String content = String.format("%s %s %s",
								statement.getSubject().getURI(),
								statement.getPredicate().getURI(),
								objectUri);
						
						FormDataMultiPart multiPart = new FormDataMultiPart();
						multiPart
							.field("content", content);
						
						Response result = null;
						
						try {
							
							WebTarget target = 
									ClientBuilder
										.newClient()
										.register(MultiPartFeature.class)
										.target(objectUri)
										.path("linkCreated");					
		
							result = target
										.request(MediaTypeConverter.APPLICATION_LD_JSON)
										.put(Entity.entity(multiPart, multiPart.getMediaType()));
							
							if (result.getStatus() != Response.Status.CREATED.getStatusCode()) {
								logger.warn(String.format("LinkCreated processing error, status: %s, target: %s", result.getStatus(), target));
							}
							
						} catch (WebApplicationException e) {
							logger.error(e.getMessage() + ": " + e.getResponse());
						}
						
						
					}
					
				}
				
				
			}
			
			
		}
		
		return dataSetManager.getById(collectionId, dataSourceId, dataSetId);
	}
	
	
	private void updateMetaModelAfterUploading(String dataSetUri, String graphUri, String graphBaseUri, long sizeInTriples) {
		
		Model metaDataModel = getMetaDataModel();

		try {
		
			if (metaDataModel.supportsTransactions()) {
				metaDataModel.begin();
			}		
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?dataSetUri lbdho:graphUri ?o } \n" +
								"WHERE { ?dataSetUri lbdho:graphUri ?o }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("dataSetUri", dataSetUri);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"INSERT DATA { ?dataSetUri lbdho:graphUri ?graphUri }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("dataSetUri", dataSetUri);
						setLiteral("graphUri", metaDataModel.createLiteral(graphUri));
					}}.asUpdate(),
					metaDataModel);
	
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?dataSetUri lbdho:graphBaseUri ?o } \n" +
								"WHERE { ?dataSetUri lbdho:graphBaseUri ?o }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("dataSetUri", dataSetUri);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"INSERT DATA { ?dataSetUri lbdho:graphBaseUri ?graphBaseUri }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("dataSetUri", dataSetUri);
						setLiteral("graphBaseUri", metaDataModel.createLiteral(graphBaseUri));
					}}.asUpdate(),
					metaDataModel);
	
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?dataSetUri lbdho:lastModified ?o } \n" +
								"WHERE { ?dataSetUri lbdho:lastModified ?o }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("dataSetUri", dataSetUri);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"INSERT DATA { ?dataSetUri lbdho:lastModified ?lastModified }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("dataSetUri", dataSetUri);
						setLiteral("lastModified", Calendar.getInstance());
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?dataSetUri lbdho:sizeInTriples ?o } \n" +
								"WHERE { ?dataSetUri lbdho:sizeInTriples ?o }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("dataSetUri", dataSetUri);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"INSERT DATA { ?dataSetUri lbdho:sizeInTriples ?sizeInTriples }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("dataSetUri", dataSetUri);
						setLiteral("sizeInTriples", sizeInTriples);
					}}.asUpdate(),
					metaDataModel);
			
			if (metaDataModel.supportsTransactions()) {
				metaDataModel.commit();
			}
			
		} catch (Exception exception) {
			if (metaDataModel.supportsTransactions()) {
				metaDataModel.abort();
			}						
		}
		
	}
	
	
	public void onLinksCreated(Model links) throws DrumbeatException {
		
		StmtIterator stmtIterator = links.listStatements();
		
		while (stmtIterator.hasNext()) {
			onLinkCreated(stmtIterator.next());
		}
		
	}
	
	
	public void onLinkCreated(Statement linkStatement) throws DrumbeatException {
		
		if (linkStatement.getPredicate().equals(DrumbeatOntology.BLO.implements1)) {

			String dataSourceUri = NameFormatter.getDataSourceUriFromObjectUri(linkStatement.getObject().asResource().getURI());
			
			String backLinkDataSetUri = NameFormatter.formatBackLinkSourceUri(dataSourceUri);
			
			Model targetModel = DrumbeatApplication.getInstance().getDataModel(backLinkDataSetUri);
			
			targetModel.add(
					linkStatement.getObject().asResource(),
					DrumbeatOntology.BLO.isImplementedBy,
					linkStatement.getSubject());

		}
	}

	
}
