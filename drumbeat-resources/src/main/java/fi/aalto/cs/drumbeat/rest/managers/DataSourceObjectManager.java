package fi.aalto.cs.drumbeat.rest.managers;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.update.UpdateAction;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatVocabulary;
import fi.aalto.cs.drumbeat.rest.ontology.LinkedBuildingDataOntology;

import java.io.InputStream;
import java.util.Calendar;

import javax.sound.midi.MetaMessage;

import org.apache.commons.lang3.NotImplementedException;

import fi.aalto.cs.drumbeat.common.DrumbeatException;
import fi.aalto.cs.drumbeat.common.string.StringUtils;
import fi.aalto.cs.drumbeat.rdf.jena.provider.JenaProvider;

import static fi.aalto.cs.drumbeat.rest.ontology.LinkedBuildingDataOntology.*;

public class DataSourceObjectManager extends DrumbeatManager {
	
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
		Model metaDataModel = getMetaDataModel();

		DataSetManager dataSetManager = new DataSetManager(metaDataModel, getJenaProvider());
		
		Resource dataSetResource = dataSetManager.getLastDataSetResource(collectionId, dataSourceId).inModel(metaDataModel);
		if (dataSetResource != null) {
			throw ErrorFactory.createObjectNotFoundException(collectionId, dataSourceId, objectId);
		}		
		
		DataSetObjectManager dataSetObjectManager = new DataSetObjectManager(metaDataModel, getJenaProvider());
		
		for (;;) {
			
			String overwritingMethod = null;
			String overwrittenDataSetUri = null;
			
			if (dataSetResource.hasProperty(LinkedBuildingDataOntology.overwritingMethod)) {
				overwritingMethod = dataSetResource
					.getProperty(LinkedBuildingDataOntology.overwritingMethod)
					.getObject()
					.asLiteral()
					.getString();
				
				overwrittenDataSetUri = dataSetResource
						.getProperty(LinkedBuildingDataOntology.overwrites)
						.getObject()
						.asLiteral()
						.getString();
			}
			
			if (StringUtils.isEmptyOrNull(overwritingMethod)) {
				overwritingMethod = DrumbeatVocabulary.OVERWRITING_METHOD_OVERWRITE_GRAPH;
			}
			
			String dataSetId = dataSetResource.getLocalName();
			
			Model resultModel;
			
			try {
				resultModel = dataSetObjectManager.getById(collectionId, dataSourceId, dataSetId, objectId);
			} catch (NotFoundException e) {
				resultModel = ModelFactory.createDefaultModel();
			}
			
			switch (overwritingMethod) {
			case DrumbeatVocabulary.OVERWRITING_METHOD_OVERWRITE_GRAPH:
				return resultModel;

			case DrumbeatVocabulary.OVERWRITING_METHOD_OVERWRITE_OBJECTS:
				if (!resultModel.isEmpty()) {
					return resultModel;
				}
				break;

			case DrumbeatVocabulary.OVERWRITING_METHOD_OVERWRITE_TRIPLES:
			default:
				throw new NotImplementedException(DrumbeatVocabulary.OVERWRITING_METHOD_OVERWRITE_TRIPLES);
			}
			
			dataSetResource = metaDataModel.getResource(overwrittenDataSetUri);	
		}
		
		
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
		String graphUri = formatGraphUri(collectionId, dataSourceId, dataSetId);
		String graphBaseUri = formatObjectResourceBaseUri(collectionId, dataSourceId);
		
		//
		// Read input stream to target model
		//
		Model targetModel = new UploadManager().upload(graphUri, graphBaseUri, dataType, dataFormat, compressionFormat, in, saveToFiles);
		
		//
		// Update meta data model
		//
		String dataSetUri = formatDataSetResourceUri(collectionId, dataSourceId, dataSetId);
		updateMetaModelAfterUploading(dataSetUri, graphUri, graphBaseUri, targetModel.size());		
		
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
						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
						setIri("dataSetUri", dataSetUri);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"INSERT DATA { ?dataSetUri lbdho:graphUri ?graphUri }");
						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
						setIri("dataSetUri", dataSetUri);
						setLiteral("graphUri", metaDataModel.createLiteral(graphUri));
					}}.asUpdate(),
					metaDataModel);
	
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?dataSetUri lbdho:graphBaseUri ?o } \n" +
								"WHERE { ?dataSetUri lbdho:graphBaseUri ?o }");
						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
						setIri("dataSetUri", dataSetUri);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"INSERT DATA { ?dataSetUri lbdho:graphBaseUri ?graphBaseUri }");
						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
						setIri("dataSetUri", dataSetUri);
						setLiteral("graphBaseUri", metaDataModel.createLiteral(graphBaseUri));
					}}.asUpdate(),
					metaDataModel);
	
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?dataSetUri lbdho:lastModified ?o } \n" +
								"WHERE { ?dataSetUri lbdho:lastModified ?o }");
						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
						setIri("dataSetUri", dataSetUri);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"INSERT DATA { ?dataSetUri lbdho:lastModified ?lastModified }");
						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
						setIri("dataSetUri", dataSetUri);
						setLiteral("lastModified", Calendar.getInstance());
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?dataSetUri lbdho:sizeInTriples ?o } \n" +
								"WHERE { ?dataSetUri lbdho:sizeInTriples ?o }");
						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
						setIri("dataSetUri", dataSetUri);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"INSERT DATA { ?dataSetUri lbdho:sizeInTriples ?sizeInTriples }");
						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
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
	
	
	
}
