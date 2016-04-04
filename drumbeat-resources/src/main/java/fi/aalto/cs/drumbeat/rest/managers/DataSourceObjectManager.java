package fi.aalto.cs.drumbeat.rest.managers;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.NotFoundException;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatVocabulary;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatOntology;
import fi.aalto.cs.drumbeat.rest.common.NameFormatter;

import org.apache.commons.lang3.NotImplementedException;

import fi.aalto.cs.drumbeat.common.DrumbeatException;
import fi.aalto.cs.drumbeat.common.string.StringUtils;
import fi.aalto.cs.drumbeat.rdf.jena.provider.JenaProvider;

public class DataSourceObjectManager extends DrumbeatManager {
	
//	private static Logger logger = Logger.getLogger(DataSourceObjectManager.class);
	
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
	
	
	public Model getAllNonBlank(String collectionId, String dataSourceId) throws DrumbeatException {
		Model metaDataModel = getMetaDataModel();

		DataSetManager dataSetManager = new DataSetManager(metaDataModel, getJenaProvider());
		
		Resource dataSetResource = dataSetManager.getLastDataSetResource(collectionId, dataSourceId);
		if (dataSetResource != null) {
			dataSetResource = dataSetResource.inModel(metaDataModel);
		} else {
			throw ErrorFactory.createObjectNotFoundException(collectionId, dataSourceId);			
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
				resultModel = dataSetObjectManager.getAllNonBlank(collectionId, dataSourceId, dataSetId);
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
		
		
		
		return resultModel;
	}	
	
	
	
//	/**
//	 * Gets all attributes of a specified object 
//	 * @param collectionId
//	 * @param dataSourceId
//	 * @param excludeProperties 
//	 * @param excludeLinks 
//	 * @return List of statements <<dataSet>> ?predicate ?object
//	 * @throws NotFoundException if the dataSet is not found
//	 * @throws DrumbeatException 
//	 */
//	public Model getById(
//			String collectionId,
//			String dataSourceId,
//			String dataSetId,
//			String objectId,
//			boolean excludeProperties,
//			boolean excludeLinks)
//		throws NotFoundException, DrumbeatException
//	{
//		String objectUri = formatObjectResourceUri(collectionId, dataSourceId, dataSetId != null ? dataSetId + "/" + objectId : objectId);
//		return getByUri(collectionId, dataSourceId, dataSetId, objectUri, excludeProperties, excludeLinks);
//	}
	
	
	
	/**
	 * Gets all attributes of a specified object 
	 * @param collectionId
	 * @param dataSourceId
	 * @param excludeProperties 
	 * @param excludeLinks 
	 * @param expandBlanks 
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
			boolean excludeLinks,
			boolean expandBlanks)
		throws NotFoundException, DrumbeatException
	{
		Model metaDataModel = getMetaDataModel();

		DataSetManager dataSetManager = new DataSetManager(metaDataModel, getJenaProvider());
		
		boolean loadFromAllDataSets = dataSetId == null;

		Resource dataSetResource;
		
		if (loadFromAllDataSets) {		
			dataSetResource = dataSetManager.getLastDataSetResource(collectionId, dataSourceId);
			if (dataSetResource != null) {
				dataSetResource = dataSetResource.inModel(metaDataModel);
			} else {
				throw ErrorFactory.createObjectNotFoundException(collectionId, dataSourceId, objectUri);			
			}
		} else {
			String dataSetUri = NameFormatter.formatDataSetGraphUri(collectionId, dataSourceId, dataSetId);
			dataSetResource = ModelFactory.createDefaultModel().createResource(dataSetUri) ;
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
			
			dataSetId = dataSetResource.getLocalName();
			
			try {
				resultModel = dataSetObjectManager.getByUri(collectionId, dataSourceId, dataSetId, objectUri, excludeProperties, expandBlanks);
			} catch (NotFoundException e) {
				resultModel = ModelFactory.createDefaultModel();
			}
			
			if (overwritingMethod.equals(DrumbeatVocabulary.OVERWRITING_METHOD_OVERWRITE_GRAPH) || !loadFromAllDataSets) {
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
			//resultModel = ModelFactory.createDefaultModel();
			throw ErrorFactory.createObjectNotFoundException(collectionId, dataSourceId, objectUri);
		}
		
		
		if (!excludeLinks) {		
		
			//
			// get data from link sources
			//
			LinkSourceManager linkSourceManager = new LinkSourceManager(metaDataModel, getJenaProvider());
			Model linkSources = linkSourceManager.getAllLinkSourcesOfDataSource(collectionId, dataSourceId);
			
			if (linkSources != null) {
				
				ResIterator resIterator = linkSources.listSubjects();
				
				while (resIterator.hasNext()) {
					Resource linkSourceResource = resIterator.next();
					String linkSourceId = linkSourceResource.getLocalName();
					try {
						Model newResultModel = getByUri(collectionId, linkSourceId, null, objectUri, false, true, expandBlanks);
						resultModel.add(newResultModel);
					} catch (NotFoundException e) {					
					}
				}
				
			}
			
			
			//
			// get data from back-linking dataset
			//
			String backLinkSourceUri = NameFormatter.formatBackLinkSourceUri(collectionId, dataSourceId);
			Model backLinkSourceModel = DrumbeatApplication.getInstance().getDataModel(backLinkSourceUri);
			
			if (backLinkSourceModel != null) {
				try {
					Model newResultModel = dataSetObjectManager.getByUri(backLinkSourceModel, objectUri, false, expandBlanks);
					if (newResultModel != null) {
						resultModel.add(newResultModel);
					}
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
	
	
	public boolean onLinkCreated(String subjectUri, String predicateUri, String objectUri) throws DrumbeatException {
		
		Property inversePredicateUri = null;
		
		if (predicateUri.equals(DrumbeatOntology.BLO.implements1.getURI())) {			
			inversePredicateUri = DrumbeatOntology.BLO.isImplementedBy;			
		} else if (predicateUri.equals(DrumbeatOntology.SEO.object.getURI())) {			
			inversePredicateUri = DrumbeatOntology.SEO.event;			
		}  
		
		if (inversePredicateUri != null) {

			String dataSourceUri = NameFormatter.getDataSourceUriFromObjectUri(objectUri);
			
			String backLinkDataSetUri = NameFormatter.formatBackLinkSourceUri(dataSourceUri);
			
			Model targetModel = DrumbeatApplication.getInstance().getDataModel(backLinkDataSetUri);
			
			targetModel.add(
					targetModel.createResource(objectUri),
					inversePredicateUri,
					targetModel.createResource(subjectUri));
			
			return true;
		}
		
		return false;
	}

}
