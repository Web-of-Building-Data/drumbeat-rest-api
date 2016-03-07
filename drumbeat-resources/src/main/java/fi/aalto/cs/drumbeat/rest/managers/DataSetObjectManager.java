package fi.aalto.cs.drumbeat.rest.managers;

import static fi.aalto.cs.drumbeat.rest.common.NameFormatter.*;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.update.UpdateAction;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatOntology;
import fi.aalto.cs.drumbeat.rest.managers.upload.DataSetUploadManager;
import fi.aalto.cs.drumbeat.rest.managers.upload.DataSetUploadOptions;

import java.io.File;
import java.io.InputStream;
import java.util.Calendar;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import fi.aalto.cs.drumbeat.common.DrumbeatException;
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
	 * @param excludeProperties
	 * @return List of statements <<dataSet>> ?predicate ?object
	 * @throws NotFoundException if the dataSet is not found
	 * @throws DrumbeatException 
	 */
	public Model getById(String collectionId, String dataSourceId, String dataSetId, String objectId, boolean excludeProperties)
		throws NotFoundException, DrumbeatException
	{
		String objectUri = formatObjectResourceUri(collectionId, dataSourceId, objectId);
		return getByUri(collectionId, dataSourceId, dataSetId, objectUri, excludeProperties);		
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
	public Model getByUri(String collectionId, String dataSourceId, String dataSetId, String objectUri, boolean excludeProperties)
		throws NotFoundException, DrumbeatException
	{
		Model dataModel = getDataModel(collectionId, dataSourceId, dataSetId);
		
		Model resultModel = getByUri(dataModel, objectUri, excludeProperties);
		
		if (resultModel.isEmpty()) {
			throw ErrorFactory.createObjectNotFoundException(collectionId, dataSourceId, objectUri);
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
	public Model getByUri(Model dataModel, String objectUri, boolean excludeProperties)
		throws DrumbeatException
	{
		Query query;
		
		if (!excludeProperties) {
		
			query = new ParameterizedSparqlString() {{
				setCommandText(
						"CONSTRUCT { \n" +
						"	?objectUri ?predicate ?object \n" +
						"} WHERE { \n" + 
						"	?objectUri ?predicate ?object . \n" +
						"} \n" + 
						"ORDER BY ?predicate ?object");
				
				DrumbeatOntology.fillParameterizedSparqlString(this);
				setIri("objectUri", objectUri);
			}}.asQuery();
			
		} else {
			
			query = new ParameterizedSparqlString() {{
				setCommandText(
						"CONSTRUCT { \n" +
						"	?objectUri a ?object \n" +
						"} WHERE { \n" + 
						"	?objectUri a ?object . \n" +
						"} \n" + 
						"ORDER BY ?object");
				
				DrumbeatOntology.fillParameterizedSparqlString(this);
				setIri("objectUri", objectUri);
			}}.asQuery();
			
		}
		
		Model resultModel = 
				createQueryExecution(query, dataModel)
					.execConstruct();
		
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
		String graphBaseUri = formatObjectResourceBaseUri(collectionId, dataSourceId);
		
		//
		// Read input stream to target model
		//
		
		DataSetUploadOptions options = new DataSetUploadOptions(graphUri, graphBaseUri, dataType, dataFormat,  clearBefore, saveToFiles);		
		File savedRdfFile = new DataSetUploadManager().upload(in, options);
		Model targetModel = DrumbeatApplication.getInstance().getDataModel(graphUri);
		
		
		if (notifyRemote) {
			notifyRemote(targetModel);
		}		
		
		//
		// Update meta data model
		//
		String dataSetUri = formatDataSetResourceUri(collectionId, dataSourceId, dataSetId);
		updateMetaModelAfterUploading(dataSetUri, graphUri, graphBaseUri, targetModel.size(), savedRdfFile);
		
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
