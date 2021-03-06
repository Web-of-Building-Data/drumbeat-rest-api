package fi.aalto.cs.drumbeat.rest.managers;

import static fi.aalto.cs.drumbeat.rest.common.NameFormatter.*;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.AlreadyExistsException;
import org.apache.jena.shared.NotFoundException;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.RDF;
import org.apache.log4j.Logger;

import fi.aalto.cs.drumbeat.common.DrumbeatException;
import fi.aalto.cs.drumbeat.common.string.StringUtils;
import fi.aalto.cs.drumbeat.rdf.jena.provider.JenaProvider;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatOntology;

public class DataSetManager extends DrumbeatManager {	
	
	private static final Logger logger = Logger.getLogger(DataSetManager.class);	
	
	public DataSetManager() throws DrumbeatException {
	}
	
	public DataSetManager(Model metaDataModel, JenaProvider jenaProvider) {
		super(metaDataModel, jenaProvider);
	}

	/**
	 * Gets all dataSets that belong to the specified collection and datasource.
	 * @param collectionId
	 * @param dataSourceId
	 * @return List of statements <<dataSet>> rdf:type lbdho:DataSet
	 * @throws NotFoundException if the datasource is not found
	 */
	public Model getAll(String collectionId, String dataSourceId)
		throws NotFoundException
	{
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"CONSTRUCT { \n" +
					"	?dataSetUri rdf:type lbdho:DataSet \n" +
					"} WHERE { \n" + 
					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?dataSourceUri . \n" +
					"	?dataSourceUri a lbdho:DataSource ; lbdho:hasDataSet ?dataSetUri . \n" +
					"	?dataSetUri a lbdho:DataSet . \n" +
					"} \n" + 
					"ORDER BY ?dataSetUri");
			
			DrumbeatOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
			setIri("dataSourceUri", formatDataSourceResourceUri(collectionId, dataSourceId));
		}}.asQuery();
		
		logger.debug("Running query \n" + query);

		Model result = 
				createQueryExecution(query, getMetaDataModel())
					.execConstruct();
		
		if (result.isEmpty()) {
			DataSourceManager dataSourceManager = new DataSourceManager(getMetaDataModel(), getJenaProvider()); 
			if (!dataSourceManager.checkExists(collectionId, dataSourceId)) {
				throw ErrorFactory.createDataSourceNotFoundException(collectionId, dataSourceId);
			}
		}
		
		return result;
	}
	
	
	/**
	 * Gets last created dataSet that belong to the specified collection and datasource.
	 * @param collectionId
	 * @param dataSourceId
	 * @return List of statements <<dataSet>> rdf:type lbdho:DataSet
//	 * @throws NotFoundException if the datasource is not found
	 */
	public Resource getLastDataSetResource(String collectionId, String dataSourceId)
//		throws NotFoundException
	{
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"SELECT \n" +
					"	?dataSetUri \n" +
					"WHERE { \n" + 
					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?dataSourceUri . \n" +
					"	?dataSourceUri a lbdho:DataSource ; lbdho:hasDataSet ?dataSetUri ; lbdho:hasLastDataSet ?dataSetUri . \n" +
					"	?dataSetUri a lbdho:DataSet . \n" +
					"} \n" + 
					"ORDER BY ?dataSetUri");
			
			DrumbeatOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
			setIri("dataSourceUri", formatDataSourceResourceUri(collectionId, dataSourceId));
		}}.asQuery();
		
		logger.debug("Running query \n" + query);
		
		ResultSet result = 
				createQueryExecution(query, getMetaDataModel())
					.execSelect();
		
		if (!result.hasNext()) {
			return null;
//			DataSourceManager dataSourceManager = new DataSourceManager(getMetaDataModel(), getJenaProvider()); 
//			if (!dataSourceManager.checkExists(collectionId, dataSourceId)) {
//				throw ErrorFactory.createDataSourceNotFoundException(collectionId, dataSourceId);
//			}
		}
		
		return result.next().getResource("dataSetUri");
	}
	
	
	
	/**
	 * Gets all properties of a specified dataSet 
	 * @param collectionId
	 * @param dataSourceId
	 * @param dataSetId
	 * @return List of statements <<dataSet>> ?predicate ?object
	 * @throws NotFoundException if the dataSet is not found
	 */
	public Model getById(String collectionId, String dataSourceId, String dataSetId)
		throws NotFoundException
	{
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"CONSTRUCT { \n" +
					"	?dataSetUri ?predicate ?object \n" +
					"} WHERE { \n" + 
					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?dataSourceUri . \n" +
					"	?dataSourceUri a lbdho:DataSource ; lbdho:hasDataSet ?dataSetUri . \n" +
					"	?dataSetUri a lbdho:DataSet ; ?predicate ?object . \n" +
					"} \n" + 
					"ORDER BY ?predicate ?object");			

			DrumbeatOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
			setIri("dataSourceUri", formatDataSourceResourceUri(collectionId, dataSourceId));
			setIri("dataSetUri", formatDataSetResourceUri(collectionId, dataSourceId, dataSetId));
		}}.asQuery();
		
		logger.debug("Running query \n" + query);

		Model result = 
				createQueryExecution(query, getMetaDataModel())
					.execConstruct();
		
		if (result.isEmpty()) {
			throw ErrorFactory.createDataSetNotFoundException(collectionId, dataSourceId, dataSetId);
		}
		
		return result;
	}
	
	
	/**
	 * Creates a specified dataSet 
	 * @param collectionId
	 * @param dataSourceId
	 * @param dataSetId
	 * @param overwritingMethod 
	 * @return the recently created dataSet
	 * @throws AlreadyExistsException if the dataSet already exists
	 * @throws NotFoundException if the datasource is not found
	 */
	public Model create(String collectionId, String dataSourceId, String dataSetId, String name, String overwritingMethod)
		throws AlreadyExistsException, NotFoundException, IllegalArgumentException
	{
		if (!StringUtils.isEmptyOrNull(overwritingMethod)) {
			switch (overwritingMethod) {
			
			}
		}
		
		//
		// check if the parent dataSource exists
		//
		DataSourceManager dataSourceManager = new DataSourceManager(getMetaDataModel(), getJenaProvider()); 
		if (!dataSourceManager.checkExists(collectionId, dataSourceId)) {
			throw ErrorFactory.createDataSourceNotFoundException(collectionId, dataSourceId);
		}
		
		//
		// check if there is another dataset in this datasource with the same dataSetId 
		//
		if (checkExists(collectionId, dataSourceId, dataSetId)) {
			throw ErrorFactory.createDataSetAlreadyExistsException(collectionId, dataSourceId, dataSetId);
		}
		
		Model metaDataModel = getMetaDataModel();		

		Resource dataSourceResource = metaDataModel
				.createResource(formatDataSourceResourceUri(collectionId, dataSourceId));
		
		Resource dataSetResource = metaDataModel 
				.createResource(formatDataSetResourceUri(collectionId, dataSourceId, dataSetId));

		//
		// check if there is another dataset in this datasource
		//
		Resource lastDataSetResource = getLastDataSetResource(collectionId, dataSourceId);
		
		if (lastDataSetResource != null) {
			lastDataSetResource = lastDataSetResource.inModel(metaDataModel);
			metaDataModel.remove(dataSourceResource, DrumbeatOntology.LBDHO.hasLastDataSet, lastDataSetResource);
			dataSetResource
				.addProperty(DrumbeatOntology.LBDHO.overwrites, lastDataSetResource)
				.addProperty(DrumbeatOntology.LBDHO.overwritingMethod, overwritingMethod);
		}
		
		dataSourceResource
			.addProperty(DrumbeatOntology.LBDHO.hasLastDataSet, dataSetResource)
			.addProperty(DrumbeatOntology.LBDHO.hasDataSet, dataSetResource);
	
		dataSetResource
			.addProperty(RDF.type, DrumbeatOntology.LBDHO.DataSet)
			.addLiteral(DrumbeatOntology.LBDHO.name, name)
			.addProperty(DrumbeatOntology.LBDHO.inDataSource, dataSourceResource);
		
		return ModelFactory
				.createDefaultModel()
				.add(dataSetResource, RDF.type, DrumbeatOntology.LBDHO.DataSet);
	}
	
	
	/**
	 * Creates a specified dataSet 
	 * @param collectionId
	 * @param dataSourceId
	 * @param dataSetId
	 * @return the recently created dataSet
	 * @throws NotFoundException if the dataSet is not found
	 */
	public void delete(String collectionId, String dataSourceId, String dataSetId)
		throws NotFoundException
	{
		if (!checkExists(collectionId, dataSourceId, dataSetId)) {
			throw ErrorFactory.createDataSetNotFoundException(collectionId, dataSourceId, dataSetId);
		}
		
		UpdateRequest updateRequest1 = new ParameterizedSparqlString() {{
			setCommandText(
					"DELETE { ?dataSetUri ?p ?o } \n" +
					"WHERE { ?dataSetUri ?p ?o }");
			DrumbeatOntology.fillParameterizedSparqlString(this);
			setIri("dataSetUri", formatDataSetResourceUri(collectionId, dataSourceId, dataSetId));			
		}}.asUpdate();
		
		UpdateRequest updateRequest2 = new ParameterizedSparqlString() {{
			setCommandText(
					"DELETE { ?s ?p ?dataSetUri } \n" +
					"WHERE { ?s ?p ?dataSetUri }");
			DrumbeatOntology.fillParameterizedSparqlString(this);
			setIri("dataSetUri", formatDataSetResourceUri(collectionId, dataSourceId, dataSetId));			
		}}.asUpdate();

		UpdateAction.execute(updateRequest1, getMetaDataModel());
		UpdateAction.execute(updateRequest2, getMetaDataModel());		
	}
	
	
	/**
	 * Checks if the dataSet exists
	 * @param collectionId
	 * @param dataSourceId
	 * @param dataSetId
	 * @return true if the dataSet exists
	 */
	public boolean checkExists(String collectionId, String dataSourceId, String dataSetId) {
		
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"ASK { \n" + 
					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?dataSourceUri . \n" +
					"	?dataSourceUri a lbdho:DataSource ; lbdho:hasDataSet ?dataSetUri . \n" +
					"	?dataSetUri a lbdho:DataSet . \n" + 
					"}");			
			DrumbeatOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
			setIri("dataSourceUri", formatDataSourceResourceUri(collectionId, dataSourceId));
			setIri("dataSetUri", formatDataSetResourceUri(collectionId, dataSourceId, dataSetId));			
		}}.asQuery();
		
		logger.debug("Running query \n" + query);

		boolean result = createQueryExecution(query, getMetaDataModel())
					.execAsk();
		
		return result;
	}
	
	
	/**
	 * Checks if the dataSource has children dataSets
	 * @param collectionId
	 * @param dataSourceId
	 * @return
	 */
	public boolean checkHasChildren(String collectionId, String dataSourceId, String dataSetId) {
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"ASK { \n" + 
					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?dataSourceUri . \n" +
					"	?dataSourceUri a lbdho:DataSource ; lbdho:hasDataSet ?dataSetUri . \n" +
					"	?dataSetUri a lbdho:DataSet . \n" + 
					"}");			
			DrumbeatOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
			setIri("dataSourceUri", formatDataSourceResourceUri(collectionId, dataSourceId));
			setIri("dataSetUri", formatDataSetResourceUri(collectionId, dataSourceId, dataSetId));			
		}}.asQuery();
		
		logger.debug("Running query \n" + query);

		boolean result = 
				createQueryExecution(query, getMetaDataModel())
					.execAsk();
		
		return result;
	}

//	public Response createLinkSet(String collectionId, String dataSourceId, String dataSetId, String sourceUrl, String targetUrl) {
//		Query query = new ParameterizedSparqlString() {{
//			setCommandText(
//					"CONSTRUCT { \n" +
//					"	?sourceUri ifc:implements ?targetUri \n" +
//					"} WHERE { \n" + 
//					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?dataSourceUri . \n" +
//					"	?dataSourceUri a lbdho:DataSource ; lbdho:hasDataSet ?dataSetUri . \n" +
//					"	?dataSetUri a lbdho:DataSet ; ?predicate ?object . \n" +
//					"} \n" + 
//					"ORDER BY ?predicate ?object");
//			
//			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
//			setIri("collectionUri", formatCollectionResourceUri(collectionId));
//			setIri("dataSourceUri", formatDataSourceResourceUri(collectionId, dataSourceId));
//			setIri("dataSetUri", formatDataSetResourceUri(collectionId, dataSourceId, dataSetId));
//		}}.asQuery();
//		
//		Model result = 
//				createQueryExecution(query, getMetaDataModel())
//					.execConstruct();
//		
//		if (result.isEmpty()) {
//			throw ErrorFactory.createDataSetNotFoundException(collectionId, dataSourceId, dataSetId);
//		}
//		
//		return result;
//	}
//	
	

}
