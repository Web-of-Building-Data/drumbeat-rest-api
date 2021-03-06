package fi.aalto.cs.drumbeat.rest.managers;

import static fi.aalto.cs.drumbeat.rest.common.NameFormatter.*;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.NotFoundException;
import org.apache.jena.update.UpdateAction;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatOntology;
import fi.aalto.cs.drumbeat.rest.common.NameFormatter;
import fi.aalto.cs.drumbeat.rest.managers.upload.DataSetUploadManager;
import fi.aalto.cs.drumbeat.rest.managers.upload.DataSetUploadOptions;

import java.io.File;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import fi.aalto.cs.drumbeat.common.DrumbeatException;
import fi.aalto.cs.drumbeat.common.string.StringUtils;
import fi.aalto.cs.drumbeat.rdf.jena.provider.JenaProvider;

public class DataSetObjectManager extends DrumbeatManager {
	
	private static final Logger logger = Logger.getLogger(DataSetObjectManager.class);	

	public DataSetObjectManager() throws DrumbeatException {
	}
	
	public DataSetObjectManager(Model metaDataModel, JenaProvider jenaProvider) {
		super(metaDataModel, jenaProvider);
	}	

	public Model getDataModel(String collectionId, String dataSourceId, String dataSetId) throws DrumbeatException {
		String graphName = formatDataSetGraphUri(collectionId, dataSourceId, dataSetId);
		return DrumbeatApplication.getInstance().getDataModel(graphName);
	}
	
	/**
	 * Gets all attributes of a specified object 
	 * @param collectionId
	 * @param dataSourceId
	 * @param dataSetId
	 * @return List of statements <<dataSet>> ?predicate ?object
	 * @throws NotFoundException if the dataSet is not found
	 * @throws DrumbeatException 
	 */
	public Model getAll(String collectionId, String dataSourceId, String dataSetId)
		throws NotFoundException, DrumbeatException
	{
		Model dataModel = getDataModel(collectionId, dataSourceId, dataSetId);

		Query query = new ParameterizedSparqlString() {{
			setCommandText(
						"CONSTRUCT { \n" +
						"	?o rdf:type ?type \n" +
						"} \n " +
//						"FROM NAMED ?dataSetUri \n " +
//						"FROM NAMED ?ifcOwlUri \n " +
						"WHERE { \n " +
						"	GRAPH ?dataSetUri { \n " +
						"		?o a ?type ; ifc:globalId_IfcRoot _:globalId . \n " +
						"	} \n " +
//						"	GRAPH ?ifcOwlUri { \n " +
//						"   	?type rdfs:subClassOf* ifc:IfcRoot . \n " +
//						"	} \n " +
						"} \n "
					);
			
			DrumbeatOntology.fillParameterizedSparqlString(this);
			setIri("dataSetUri", formatDataSetResourceUri(collectionId, dataSourceId, dataSetId));
			setIri("ifcOwlUri", DrumbeatOntology.formatDrumbeatOntologyBaseUri("ifc2x3"));
		}}.asQuery();
		
		logger.debug(String.format("%s.%s() is running query\n%s", getClass().getName(), LoggerUtil.getMethodName(1), query));

		Model resultModel = 
				createQueryExecution(query, dataModel)
					.execConstruct();
		
		if (resultModel.isEmpty()) {
			throw ErrorFactory.createObjectNotFoundException(collectionId, dataSourceId, "");
		}
		
		return resultModel;
	}
	
	
	
	/**
	 * Gets all attributes of a specified object 
	 * @param collectionId
	 * @param dataSourceId
	 * @param dataSetId
	 * @return List of statements <<dataSet>> ?predicate ?object
	 * @throws NotFoundException if the dataSet is not found
	 * @throws DrumbeatException 
	 */
	public Model getAllNonBlank(String collectionId, String dataSourceId, String dataSetId, String filterType)
		throws NotFoundException, DrumbeatException
	{
		Model dataModel = getDataModel(collectionId, dataSourceId, dataSetId);
		
		String filter = "";
		if (!StringUtils.isEmptyOrNull(filterType)) {
			filter +=
				"		FILTER REGEX( ?type , ?typeFilter ) . \n";				
		}
		
		String command = 
				"CONSTRUCT { \n" +
				"	?o rdf:type ?type \n" +
				"} \n " +
				"WHERE { \n " +
				"	GRAPH ?dataSetUri { \n " +
				"		?o a ?type . \n " +
				"		FILTER ( !regex (str(?o), \"^.*/" + NameFormatter.BLANK_NODE_PATH + "/.*$\" ) ) \n" +
				filter +
				"	} \n " +
				"} \n ";

		ParameterizedSparqlString sparql = new ParameterizedSparqlString();
		sparql.setCommandText(command);
		DrumbeatOntology.fillParameterizedSparqlString(sparql);
		sparql.setIri("dataSetUri", formatDataSetResourceUri(collectionId, dataSourceId, dataSetId));
		sparql.setIri("ifcOwlUri", DrumbeatOntology.formatDrumbeatOntologyBaseUri("ifc2x3"));
		if (!StringUtils.isEmptyOrNull(filterType)) {
			sparql.setLiteral("typeFilter", filterType);
		}
		
		Model resultModel =
				createQueryExecution(sparql.asQuery(), dataModel)
					.execConstruct();
		
		if (resultModel.isEmpty()) {
			throw ErrorFactory.createObjectNotFoundException(collectionId, dataSourceId, "");
		}
		
		return resultModel;
	}
		
	
	
	/**
	 * Gets all attributes of a specified object 
	 * @param collectionId
	 * @param dataSourceId
	 * @param dataSetId
	 * @param excludeProperties
	 * @return List of statements <<dataSet>> ?predicate ?object
	 * @throws NotFoundException if the dataSet is not found
	 * @throws DrumbeatException 
	 */
	public Model getById(
			String collectionId,
			String dataSourceId,
			String dataSetId, 
			String objectId,
			boolean excludeProperties,
			boolean expandBlankObjects,
			String filterProperties,
			String filterObjectTypes)			
		throws NotFoundException, DrumbeatException
	{
		String objectUri = formatObjectResourceUri(collectionId, dataSourceId, objectId);
		return getByUri(collectionId, dataSourceId, dataSetId, objectUri, excludeProperties, expandBlankObjects, filterProperties, filterObjectTypes);		
	}
	
	

	/**
	 * Gets all attributes of a specified object 
	 * @param collectionId
	 * @param dataSourceId
	 * @param dataSetId
	 * @param excludeProperties
	 * @param expandBlankObjects
	 * @return List of statements <<dataSet>> ?predicate ?object
	 * @throws NotFoundException if the dataSet is not found
	 * @throws DrumbeatException 
	 */
	public Model getByUri(
			String collectionId,
			String dataSourceId,
			String dataSetId,
			String objectUri,
			boolean excludeProperties,
			boolean expandBlankObjects,
			String filterProperties,
			String filterObjectTypes)
		throws NotFoundException, DrumbeatException
	{
		Model dataModel = getDataModel(collectionId, dataSourceId, dataSetId);
		
		Model resultModel = getByUri(dataModel, objectUri, excludeProperties, expandBlankObjects, filterProperties, filterObjectTypes);
		
		if (resultModel.isEmpty()) {
			throw ErrorFactory.createObjectNotFoundException(collectionId, dataSourceId, objectUri);
		}
		
		return resultModel;
	}
	
	
	/**
	 * Gets all attributes of a specified object
	 * @param dataModel
	 * @param objectUri
	 * @param excludeProperties
	 * @param expandBlankObjects
	 * @param filterProperties
	 * @param filterObjectTypes
	 * @return
	 * @throws DrumbeatException
	 */
	public Model getByUri(
			Model dataModel,
			String objectUri,
			boolean excludeProperties,
			boolean expandBlankObjects,
			String filterProperties,
			String filterObjectTypes)
		throws DrumbeatException
	{
		ParameterizedSparqlString sparql;
		
		String filter = "";
		if (!StringUtils.isEmptyOrNull(filterProperties)) {
			filter +=
					"	FILTER REGEX( ?predicate , ?predicateFilter ) . \n";				
		}
		
		if (!StringUtils.isEmptyOrNull(filterObjectTypes)) {
			filter +=
					"	?objet a ?objectType . \n" +
					"	FILTER REGEX( ?objectType , ?objectTypeFilter ) . \n";				
		}
		
		String command;
		

		
		if (!excludeProperties) {
			
			command = 
					"CONSTRUCT { \n" +
					"	?objectUri ?predicate ?object \n" +
					"} WHERE { \n" + 
					"	?objectUri ?predicate ?object . \n" +
					filter +
					"} \n" + 
					"ORDER BY ?predicate ?object";
			
		} else {
			command = 
					"CONSTRUCT { \n" +
					"	?objectUri a ?object \n" +
					"} WHERE { \n" + 
					"	?objectUri a ?object . \n" +
					filter +
					"} \n" + 
					"ORDER BY ?object";
		}
		
		sparql = new ParameterizedSparqlString();
		
		sparql.setCommandText(command);
		DrumbeatOntology.fillParameterizedSparqlString(sparql);
		sparql.setIri("objectUri", objectUri);

		if (!StringUtils.isEmptyOrNull(filterProperties)) {
			sparql.setLiteral("predicateFilter", filterProperties);			
		}
		
		if (!StringUtils.isEmptyOrNull(filterObjectTypes)) {
			sparql.setLiteral("objectTypeFilter", filterObjectTypes);			
		}
		
		return internalGetByUri(dataModel, objectUri, sparql, expandBlankObjects, null);
	}
	
	
	/**
	 * Gets all attributes of a specified object
	 * @param graphUri
	 * @param dataModel
	 * @param objectUri
	 * @param sparql
	 * @param expandBlankObjects
	 * @param blankObjectUris
	 * @return
	 * @throws DrumbeatException
	 */
	public Model internalGetByUri(
			Model dataModel,
			String objectUri,
			ParameterizedSparqlString sparql,
			boolean expandBlankObjects,
			Set<String> blankObjectUris)
		throws DrumbeatException
	{		
		logger.debug(String.format("%s() is running query\n%s", LoggerUtil.getMethodName(1), sparql));

		sparql.setIri("objectUri", objectUri);		
		
		Model resultModel = 
				createQueryExecution(sparql.asQuery(), dataModel)
					.execConstruct();
		
		logger.debug(String.format("%s(): Query result: %d", LoggerUtil.getMethodName(1), resultModel.size()));		

		if (expandBlankObjects) {
			
			if (blankObjectUris == null) {
				blankObjectUris = new HashSet<>();
			}
		
			Set<String> newBlankObjectUris = new HashSet<>();
			NodeIterator nodeIterator = resultModel.listObjects();
			
			while (nodeIterator.hasNext()) {
				RDFNode node = nodeIterator.nextNode();
				if (node.isURIResource()) {
					String newBlankObjectUri = node.asResource().getURI();
					if (!blankObjectUris.contains(newBlankObjectUri) && NameFormatter.isBlankNode(newBlankObjectUri)) {
						newBlankObjectUris.add(node.asResource().getURI());
					}
				}
			}
			
			blankObjectUris.addAll(newBlankObjectUris);
			
			for (String newBlankObjectUri : newBlankObjectUris) {
				Model newResultModel = internalGetByUri(dataModel, newBlankObjectUri, sparql, expandBlankObjects, blankObjectUris);
				resultModel.add(newResultModel);
			}
			
		}
		
		logger.debug(String.format("%s() returning: %d", LoggerUtil.getMethodName(1), resultModel.size()));		
		
		return resultModel;
		
	}
	
	
	

	/**
	 * Gets type of a specified object 
	 * @param collectionId
	 * @param dataSourceId
	 * @param dataSetId
	 * @return List of statements <<dataSet>> ?predicate ?object
	 * @throws NotFoundException if the dataSet is not found
	 * @throws DrumbeatException 
	 */
	public Model getObjectType(String collectionId, String dataSourceId, String dataSetId, String objectId)
		throws NotFoundException, DrumbeatException
	{
		Model dataModel = getDataModel(collectionId, dataSourceId, dataSetId);
		
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"SELECT (?objectUri AS ?subject) (rdf:type AS ?predicate) (?type AS ?object) { \n" + 
					"	?objectUri a ?type . \n" +
					"} \n" + 
					"ORDER BY ?subject ?predicate ?object");
			
			DrumbeatOntology.fillParameterizedSparqlString(this);
			setIri("objectUri", formatObjectResourceUri(collectionId, dataSourceId, objectId));
		}}.asQuery();
		
		logger.debug("getObjectType() is running query\n" + query);
		
		Model resultModel = 
				createQueryExecution(query, dataModel)
					.execConstruct();
		
		if (resultModel.isEmpty()) {
			throw ErrorFactory.createObjectNotFoundException(collectionId, dataSourceId, objectId);
		}
		
		return resultModel;
	}
	
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
		
		deleteCachedRdfFile(collectionId, dataSourceId, dataSetId);		
		
		//
		// Format graphUri
		//		
		String graphUri = formatDataSetGraphUri(collectionId, dataSourceId, dataSetId);
		String objectBaseUri = formatObjectResourceBaseUri(collectionId, dataSourceId);
		String blankObjectBaseUri = formatBlankObjectResourceBaseUri(collectionId, dataSourceId, dataSetId);
		
		//
		// Read input stream to target model
		//
		
		DataSetUploadOptions options = new DataSetUploadOptions(
				collectionId,
				dataSourceId,
				dataSetId,
				graphUri,
				objectBaseUri,
				blankObjectBaseUri,
				dataType,
				dataFormat,
				clearBefore,
				saveToFiles);		
		File savedRdfFile = new DataSetUploadManager().upload(in, options);
		
		Model targetModel = DrumbeatApplication.getInstance().getDataModel(graphUri);
		
		
		if (notifyRemote) {
			notifyRemote(targetModel);
		}		
		
		//
		// Update meta data model
		//
		String dataSetUri = formatDataSetResourceUri(collectionId, dataSourceId, dataSetId);
		updateMetaModelAfterUploading(dataSetUri, graphUri, objectBaseUri, targetModel.size(), savedRdfFile);
		
		return dataSetManager.getById(collectionId, dataSourceId, dataSetId);
	}
	
	
	private void notifyRemote(Model modelWithLinks) throws DrumbeatException {
		
		DataSourceObjectManager dataSourceObjectManager = new DataSourceObjectManager(getMetaDataModel(), getJenaProvider());
		
		StmtIterator stmtIterator = modelWithLinks.listStatements();
		
		String baseUri = DrumbeatApplication.getInstance().getBaseUri();
		
		if (stmtIterator != null) {
		
			while (stmtIterator.hasNext()) {
				Statement statement = stmtIterator.next();
				if (!statement.getObject().isResource()) {
					continue;
				}
				
				String subjectUri = statement.getSubject().getURI();
				String predicateUri = statement.getPredicate().getURI();
				String objectUri = statement.getObject().asResource().getURI();
				
				if (objectUri.startsWith(baseUri)) {
					dataSourceObjectManager.onLinkCreated(subjectUri, predicateUri, objectUri);				
				} else {
					
					Form form = new Form();
					form.param("subject", subjectUri);
					form.param("predicate", predicateUri);
					form.param("object", objectUri);
					
					WebTarget target = 
						ClientBuilder
							.newClient()
							.target(objectUri)
							.path("linkCreated");
					
					try {
	
						target
								.request()
								.put(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));
						
					} catch (Exception e) {
						logger.warn(String.format("Link-created notification error <%s>: %s", target.getUri(), e.getMessage()));
					}
					
				}
			}
		}
		
	}
	

	private void updateMetaModelAfterUploading(String dataSetUri, String graphUri, String graphBaseUri, long sizeInTriples, File savedRdfFile) {
		
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
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?dataSetUri lbdho:cachedInRdfFile ?o } \n" +
								"WHERE { ?dataSetUri lbdho:cachedInRdfFile ?o }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("dataSetUri", dataSetUri);
					}}.asUpdate(),
					metaDataModel);
			
			if (savedRdfFile != null) {
				UpdateAction.execute(
						new ParameterizedSparqlString() {{
							setCommandText(
									"INSERT DATA { ?dataSetUri lbdho:cachedInRdfFile ?cachedInRdfFile }");
							DrumbeatOntology.fillParameterizedSparqlString(this);
							setIri("dataSetUri", dataSetUri);
							setLiteral("cachedInRdfFile", savedRdfFile.getName());
						}}.asUpdate(),
						metaDataModel);
			}
			
			
			if (metaDataModel.supportsTransactions()) {
				metaDataModel.commit();
			}
			
		} catch (Exception exception) {
			if (metaDataModel.supportsTransactions()) {
				metaDataModel.abort();
			}						
		}
		
	}
		
	
	public Model getCachedRdfFile(String collectionId, String linkSourceId, String linkSetId) {
		
		String linkSetUri = formatLinkSetGraphUri(collectionId, linkSourceId, linkSetId);		
		
		Query query =
				new ParameterizedSparqlString() {{
					setCommandText(
							"CONSTRUCT { \n" +
									"	?linkSetUri lbdho:cachedInRdfFile ?cachedInRdfFile \n" +
									"} WHERE { \n" + 
									"	?linkSetUri lbdho:cachedInRdfFile ?cachedInRdfFile . \n" +
									"} \n");							
					DrumbeatOntology.fillParameterizedSparqlString(this);
					setIri("linkSetUri", linkSetUri);
				}}.asQuery();
		
		Model result =
				createQueryExecution(query, getMetaDataModel())
					.execConstruct();
		
		return result;
	}
	
	
	public void deleteCachedRdfFile(String collectionId, String linkSourceId, String linkSetId) {
		Model cachedRdfFile = getCachedRdfFile(collectionId, linkSourceId, linkSetId);
		NodeIterator it = cachedRdfFile.listObjects();
		while (it.hasNext()) {
			String fileName = it.next().asLiteral().getString();
			new DataSetUploadManager().deleteCachedRdfFile(fileName);
		}
		
		String linkSetUri = formatLinkSetGraphUri(collectionId, linkSourceId, linkSetId);		
		UpdateAction.execute(
				new ParameterizedSparqlString() {{
					setCommandText(
							"DELETE { ?linkSetUri lbdho:cachedInRdfFile ?o } \n" +
							"WHERE { ?linkSetUri lbdho:cachedInRdfFile ?o }");
					DrumbeatOntology.fillParameterizedSparqlString(this);
					setIri("linkSetUri", linkSetUri);
				}}.asUpdate(),
				getMetaDataModel());
		
	}

	
}
