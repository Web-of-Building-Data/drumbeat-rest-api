package fi.aalto.cs.drumbeat.rest.managers;

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

import static fi.aalto.cs.drumbeat.rest.common.NameFormatter.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

public class LinkSetObjectManager extends DrumbeatManager {
	
	private static final Logger logger = Logger.getLogger(LinkSetObjectManager.class);	

	public LinkSetObjectManager() throws DrumbeatException {
	}
	
	public LinkSetObjectManager(Model metaDataModel, JenaProvider jenaProvider) {
		super(metaDataModel, jenaProvider);
	}	

	public Model getDataModel(String collectionId, String linkSourceId, String linkSetId) throws DrumbeatException {
		String graphName = formatLinkSetGraphUri(collectionId, linkSourceId, linkSetId);
		return DrumbeatApplication.getInstance().getDataModel(graphName);
	}
	
	/**
	 * Gets all attributes of a specified object 
	 * @param collectionId
	 * @param linkSourceId
	 * @param linkSetId
	 * @return List of statements <<linkSet>> ?predicate ?object
	 * @throws NotFoundException if the linkSet is not found
	 * @throws DrumbeatException 
	 */
	public Model getAll(String collectionId, String linkSourceId, String linkSetId)
		throws NotFoundException, DrumbeatException
	{
		Model dataModel = getDataModel(collectionId, linkSourceId, linkSetId);

		Query query = new ParameterizedSparqlString() {{
			setCommandText(
						"CONSTRUCT { \n" +
						"	?o rdf:type ?type \n" +
						"} \n " +
//						"FROM NAMED ?linkSetUri \n " +
//						"FROM NAMED ?ifcOwlUri \n " +
						"WHERE { \n " +
						"	GRAPH ?linkSetUri { \n " +
						"		?o a ?type ; ifc:globalId_IfcRoot _:globalId . \n " +
						"	} \n " +
//						"	GRAPH ?ifcOwlUri { \n " +
//						"   	?type rdfs:subClassOf* ifc:IfcRoot . \n " +
//						"	} \n " +
						"} \n "
					);
			
			DrumbeatOntology.fillParameterizedSparqlString(this);
			setIri("linkSetUri", formatLinkSetResourceUri(collectionId, linkSourceId, linkSetId));
			setIri("ifcOwlUri", DrumbeatOntology.formatDrumbeatOntologyBaseUri("ifc2x3"));
		}}.asQuery();
		
		Model resultModel = 
				createQueryExecution(query, dataModel)
					.execConstruct();
		
		if (resultModel.isEmpty()) {
			throw ErrorFactory.createObjectNotFoundException(collectionId, linkSourceId, "");
		}
		
		return resultModel;
	}
	
	
	/**
	 * Gets all attributes of a specified object 
	 * @param collectionId
	 * @param linkSourceId
	 * @param linkSetId
	 * @return List of statements <<linkSet>> ?predicate ?object
	 * @throws NotFoundException if the linkSet is not found
	 * @throws DrumbeatException 
	 */
	public Model getById(String collectionId, String linkSourceId, String linkSetId, String objectId)
		throws NotFoundException, DrumbeatException
	{
		String objectUri = formatObjectResourceUri(collectionId, linkSourceId, objectId);
		return getByUri(collectionId, linkSourceId, linkSetId, objectUri);		
	}
	
	/**
	 * Gets all attributes of a specified object 
	 * @param collectionId
	 * @param linkSourceId
	 * @param linkSetId
	 * @return List of statements <<linkSet>> ?predicate ?object
	 * @throws NotFoundException if the linkSet is not found
	 * @throws DrumbeatException 
	 */
	public Model getByUri(String collectionId, String linkSourceId, String linkSetId, String objectUri)
		throws NotFoundException, DrumbeatException
	{
		Model dataModel = getDataModel(collectionId, linkSourceId, linkSetId);
		
		Query query = new ParameterizedSparqlString() {{
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
		
		Model resultModel = 
				createQueryExecution(query, dataModel)
					.execConstruct();
		
		if (resultModel.isEmpty()) {
			throw ErrorFactory.createObjectNotFoundException(collectionId, linkSourceId, objectUri);
		}
		
		return resultModel;
	}
	
	
	/**
	 * Gets type of a specified object 
	 * @param collectionId
	 * @param linkSourceId
	 * @param linkSetId
	 * @return List of statements <<linkSet>> ?predicate ?object
	 * @throws NotFoundException if the linkSet is not found
	 * @throws DrumbeatException 
	 */
	public Model getObjectType(String collectionId, String linkSourceId, String linkSetId, String objectId)
		throws NotFoundException, DrumbeatException
	{
		Model dataModel = getDataModel(collectionId, linkSourceId, linkSetId);
		
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"SELECT (?objectUri AS ?subject) (rdf:type AS ?predicate) (?type AS ?object) { \n" + 
					"	?objectUri a ?type . \n" +
					"} \n" + 
					"ORDER BY ?subject ?predicate ?object");
			
			DrumbeatOntology.fillParameterizedSparqlString(this);
			setIri("objectUri", formatObjectResourceUri(collectionId, linkSourceId, objectId));
		}}.asQuery();
		
		Model resultModel = 
				createQueryExecution(query, dataModel)
					.execConstruct();
		
		if (resultModel.isEmpty()) {
			throw ErrorFactory.createObjectNotFoundException(collectionId, linkSourceId, objectId);
		}
		
		return resultModel;
	}
	
//	/**
//	 * Gets type of a specified object 
//	 * @param collectionId
//	 * @param linkSourceId
//	 * @param linkSetId
//	 * @return List of statements <<linkSet>> ?predicate ?object
//	 * @throws NotFoundException if the linkSet is not found
//	 * @throws DrumbeatException 
//	 */
//	public Model getObjectProperty(String collectionId, String linkSourceId, String linkSetId, String objectId, String propertyName)
//		throws NotFoundException, DrumbeatException
//	{
//		Model dataModel = getDataModel(collectionId, linkSourceId, linkSetId);
//		
//		Query query = new ParameterizedSparqlString() {{
//			setCommandText(
//					"SELECT ?typeUri { \n" + 
//					"	?objectUri a ?typeUri . \n" +
//					"} \n" + 
//					"ORDER BY ?subject ?predicate ?object");
//			
//			fillParameterizedSparqlString(this);
//			setIri("objectUri", formatObjectResourceUri(collectionId, linkSourceId, linkSetId, objectId));
//		}}.asQuery();
//		
//		Model resultModel = 
//				createQueryExecution
//					.create(query, dataModel)
//					.execConstruct();
//		
//		if (resultModel.isEmpty()) {
//			throw ErrorFactory.createObjectNotFoundException(collectionId, linkSourceId, linkSetId, objectId);
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
//			String collectionId, String linkSourceId, String linkSetId, String objectId, String propertyName, Resource typeResource, Model ifcModel)
//	{
//		Query query = new ParameterizedSparqlString() {{
//			setCommandText(
//					"SELECT ?type { \n" + 
//					"	?objectUri a ?type . \n" +
//					"} \n" + 
//					"ORDER BY ?subject ?predicate ?object");
//			
//			fillParameterizedSparqlString(this);
//			setIri("objectUri", formatObjectResourceUri(collectionId, linkSourceId, linkSetId, objectId));
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
	 * @param linkSourceId
	 * @param linkSetId
	 * @param dataType
	 * @param dataFormat
	 * @param clearBefore
	 * @param in
	 * @param saveFiles
	 * @return
	 * @throws NotFoundException
	 */
	public Model upload(
			String collectionId,
			String linkSourceId,
			String linkSetId,
			String dataType,
			String dataFormat,
			boolean clearBefore,
			boolean notifyRemote,
			InputStream in,
			boolean saveToFiles)
		throws NotFoundException, IllegalArgumentException, Exception
	{
		//
		// Checking if linkSet exists
		//		
		LinkSetManager linkSetManager = new LinkSetManager();
		if (!linkSetManager.checkExists(collectionId, linkSourceId, linkSetId)) {
			throw ErrorFactory.createLinkSetNotFoundException(collectionId, linkSourceId, linkSetId);
		}
		
		deleteCachedRdfFile(collectionId, linkSourceId, linkSetId);
		
		//
		// Format graphUri
		//		
		String graphUri = formatLinkSetGraphUri(collectionId, linkSourceId, linkSetId);
		String graphBaseUri = formatObjectResourceBaseUri(collectionId, linkSourceId);
		
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
		String linkSetUri = formatLinkSetResourceUri(collectionId, linkSourceId, linkSetId);
		updateMetaModelAfterUploading(linkSetUri, graphUri, graphBaseUri, targetModel.size(), savedRdfFile);		
		
		return linkSetManager.getById(collectionId, linkSourceId, linkSetId);
	}
	
	
	public Model generateLinks( 
			String collectionId,
			String linkSourceId,
			String linkSetId,
			boolean clearBefore,
			String linkType,
			String localDataSourceUri,
			String remoteDataSourceUri,
			boolean notifyRemote)
					throws Exception
	{
		//
		// Checking if linkSet exists
		//		
		LinkSetManager linkSetManager = new LinkSetManager();
		if (!linkSetManager.checkExists(collectionId, linkSourceId, linkSetId)) {
			throw ErrorFactory.createLinkSetNotFoundException(collectionId, linkSourceId, linkSetId);
		}
		
		String sparqlTemplate;
		
		FileReader reader = null;
		
		try {
			String sparqlTemplateFileName = linkType + ".sparql";
			String sparqlTemplateFilePath = DrumbeatApplication.getInstance().getRealServerPath(DrumbeatApplication.ResourcePaths.LINKS_FOLDER_PATH + sparqlTemplateFileName); 

			reader = new FileReader(sparqlTemplateFilePath);
			StringBuilder sb = new StringBuilder();
			char[] buf = new char[1024];
			int length;
			while ((length = reader.read(buf, 0, 1024)) > 0) {
				sb.append(buf, 0, length);
			}
			sparqlTemplate = sb.toString();
			
		} catch (IOException e) {
			throw ErrorFactory.createInvalidLinkType(linkType);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
			
			
		
		
		//
		// Format graphUri
		//		
		String graphUri = formatLinkSetGraphUri(collectionId, linkSourceId, linkSetId);
		String graphBaseUri = formatObjectResourceBaseUri(collectionId, linkSourceId);
		
		//
		// Open target model and begin transactions (if supported)
		//
		Model targetModel = DrumbeatApplication.getInstance().getDataModel(graphUri);		
		String linkSetUri = formatLinkSetResourceUri(collectionId, linkSourceId, linkSetId);

		//
		// Upload data to target model
		//		
		try {
			long oldSize = targetModel.size();		
			
			if (targetModel.supportsTransactions()) {
				targetModel.begin();
			}
			
			if (clearBefore) {
				DrumbeatApplication.getInstance().getJenaProvider().deleteModel(graphUri);
//				targetModel.removeAll();
			}
			
//			UpdateAction.execute(
//					new ParameterizedSparqlString() {{
//						setCommandText(sparqlCommand);
//						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
//						setIri("linkSetUri", linkSetUri);
//					}}.asUpdate(),
//					targetModel);
			
			Query query = new ParameterizedSparqlString() {{
								setCommandText(sparqlTemplate);
								DrumbeatOntology.fillParameterizedSparqlString(this);								
								setIri("linkSetUri", linkSetUri);
								setIri("localDataSourceUri", localDataSourceUri);
								setIri("remoteDataSourceUri", remoteDataSourceUri);
							}}.asQuery();
							
			Model modelWithLinks = createQueryExecution(query, targetModel)
				.execConstruct();
			
//			targetModel.add(modelWithLinks);
			
			
			DataSetUploadOptions options = new DataSetUploadOptions(linkSetUri, linkSetUri, "RDF", null, clearBefore, false);
			new DataSetUploadManager().internalUploadJenaModel(modelWithLinks, options );
			
			if (notifyRemote) {
				notifyRemote(modelWithLinks);
			}			
			
			if (targetModel.supportsTransactions()) {
				targetModel.commit();
			}
			
			long newSize = targetModel.size();
				
			logger.info(String.format("Uploaded data to graph '%s': oldSize=%d, newSize=%d", graphUri, oldSize, newSize));
			
		} catch (Exception e) {
			if (targetModel.supportsTransactions()) {
				targetModel.abort();
			}	
			
			logger.error(e);
			throw e;			
		}
		
		//
		// Update meta data model
		//
		updateMetaModelAfterUploading(linkSetUri, graphUri, graphBaseUri, targetModel.size(), null);		
		
		return linkSetManager.getById(collectionId, linkSourceId, linkSetId);
		
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
	
	
	
	
	private void updateMetaModelAfterUploading(String linkSetUri, String graphUri, String graphBaseUri, long sizeInTriples, File savedRdfFile) {
		
		Model metaDataModel = getMetaDataModel();

		try {
		
			if (metaDataModel.supportsTransactions()) {
				metaDataModel.begin();
			}		
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?linkSetUri lbdho:graphUri ?o } \n" +
								"WHERE { ?linkSetUri lbdho:graphUri ?o }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("linkSetUri", linkSetUri);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"INSERT DATA { ?linkSetUri lbdho:graphUri ?graphUri }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("linkSetUri", linkSetUri);
						setLiteral("graphUri", metaDataModel.createLiteral(graphUri));
					}}.asUpdate(),
					metaDataModel);
	
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?linkSetUri lbdho:graphBaseUri ?o } \n" +
								"WHERE { ?linkSetUri lbdho:graphBaseUri ?o }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("linkSetUri", linkSetUri);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"INSERT DATA { ?linkSetUri lbdho:graphBaseUri ?graphBaseUri }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("linkSetUri", linkSetUri);
						setLiteral("graphBaseUri", metaDataModel.createLiteral(graphBaseUri));
					}}.asUpdate(),
					metaDataModel);
	
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?linkSetUri lbdho:lastModified ?o } \n" +
								"WHERE { ?linkSetUri lbdho:lastModified ?o }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("linkSetUri", linkSetUri);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"INSERT DATA { ?linkSetUri lbdho:lastModified ?lastModified }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("linkSetUri", linkSetUri);
						setLiteral("lastModified", Calendar.getInstance());
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?linkSetUri lbdho:sizeInTriples ?o } \n" +
								"WHERE { ?linkSetUri lbdho:sizeInTriples ?o }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("linkSetUri", linkSetUri);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"INSERT DATA { ?linkSetUri lbdho:sizeInTriples ?sizeInTriples }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("linkSetUri", linkSetUri);
						setLiteral("sizeInTriples", sizeInTriples);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?linkSetUri lbdho:cachedInRdfFile ?o } \n" +
								"WHERE { ?linkSetUri lbdho:cachedInRdfFile ?o }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("linkSetUri", linkSetUri);
					}}.asUpdate(),
					metaDataModel);
			
			if (savedRdfFile != null) {
				UpdateAction.execute(
						new ParameterizedSparqlString() {{
							setCommandText(
									"INSERT DATA { ?linkSetUri lbdho:cachedInRdfFile ?cachedInRdfFile }");
							DrumbeatOntology.fillParameterizedSparqlString(this);
							setIri("linkSetUri", linkSetUri);
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
