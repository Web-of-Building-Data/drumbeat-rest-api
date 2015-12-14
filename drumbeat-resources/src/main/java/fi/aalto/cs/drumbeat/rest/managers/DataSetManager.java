package fi.aalto.cs.drumbeat.rest.managers;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.AlreadyExistsException;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.ontology.LinkedBuildingDataOntology;
import fi.hut.cs.drumbeat.common.DrumbeatException;

public class DataSetManager extends MetaDataManager {
	
//	private static final Logger logger = Logger.getLogger(DataSetManager.class);	
	
	public DataSetManager() throws DrumbeatException {
		this(DrumbeatApplication.getInstance().getMetaDataModel());
	}
	
	public DataSetManager(Model metaDataModel) {
		super(metaDataModel);
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
					"SELECT (?dataSetUri AS ?subject) (rdf:type AS ?predicate) (?lbdho_DataSet AS ?object) { \n" + 
					"	?collectionUri a ?lbdho_Collection ; ?lbdho_hasDataSource ?dataSourceUri . \n" +
					"	?dataSourceUri a ?lbdho_DataSource ; ?lbdho_hasDataSet ?dataSetUri . \n" +
					"	?dataSetUri a ?lbdho_DataSet . \n" +
					"} \n" + 
					"ORDER BY ?subject");
			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", getCollectionResource(collectionId).getURI());
			setIri("dataSourceUri", getDataSourceResource(collectionId, dataSourceId).getURI());
		}}.asQuery();
		
		ResultSet resultSet = 
				QueryExecutionFactory
					.create(query, getMetaDataModel())
					.execSelect();
		
		if (!resultSet.hasNext()) {
			DataSourceManager dataSourceManager = new DataSourceManager(getMetaDataModel()); 
			if (!dataSourceManager.checkExists(collectionId, dataSourceId)) {
				Resource dataSourceResouce = getDataSourceResource(collectionId, dataSourceId);
				throw ErrorFactory.createDataSourceNotFoundException(dataSourceResouce);
			}
		}
		
		return convertResultSetToModel(resultSet);
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
					"SELECT (?dataSetUri AS ?subject) ?predicate ?object { \n" + 
					"	?collectionUri a ?lbdho_Collection ; ?lbdho_hasDataSource ?dataSourceUri . \n" +
					"	?dataSourceUri a ?lbdho_DataSource ; ?lbdho_hasDataSet ?dataSetUri . \n" +
					"	?dataSetUri a ?lbdho_DataSet ; ?predicate ?object . \n" +
					"} \n" + 
					"ORDER BY ?subject ?predicate ?object");
			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", getCollectionResource(collectionId).getURI());
			setIri("dataSourceUri", getDataSourceResource(collectionId, dataSourceId).getURI());
			setIri("dataSetUri", getDataSetResource(collectionId, dataSourceId, dataSetId).getURI());
		}}.asQuery();
		
		ResultSet resultSet = 
				QueryExecutionFactory
					.create(query, getMetaDataModel())
					.execSelect();
		
		if (!resultSet.hasNext()) {
			Resource dataSetResouce = getDataSetResource(collectionId, dataSourceId, dataSetId);
			throw ErrorFactory.createDataSetNotFoundException(dataSetResouce);
		}
		
		return convertResultSetToModel(resultSet);
	}
	
	
	/**
	 * Creates a specified dataSet 
	 * @param collectionId
	 * @param dataSourceId
	 * @param dataSetId
	 * @return the recently created dataSet
	 * @throws AlreadyExistsException if the dataSet already exists
	 * @throws NotFoundException if the datasource is not found
	 */
	public Model create(String collectionId, String dataSourceId, String dataSetId, String name)
		throws AlreadyExistsException, NotFoundException
	{
		DataSourceManager dataSourceManager = new DataSourceManager(getMetaDataModel()); 
		Resource dataSourceResource = getDataSourceResource(collectionId, dataSourceId);		
		if (!dataSourceManager.checkExists(collectionId, dataSourceId)) {
			Resource dataSourceResouce = getDataSourceResource(collectionId, dataSourceId);
			throw ErrorFactory.createDataSourceNotFoundException(dataSourceResouce);
		}
		
		Resource dataSetResource = getDataSetResource(collectionId, dataSourceId, dataSetId);		
		if (checkExists(collectionId, dataSourceId, dataSetId)) {
			throw ErrorFactory.createDataSetAlreadyExistsException(dataSetResource);
		}
		
		
		dataSourceResource
			.inModel(getMetaDataModel())
			.addProperty(LinkedBuildingDataOntology.hasDataSet, dataSetResource);
	
		dataSetResource
			.inModel(getMetaDataModel())
			.addProperty(RDF.type, LinkedBuildingDataOntology.DataSet)
			.addLiteral(LinkedBuildingDataOntology.name, name)
			.addProperty(LinkedBuildingDataOntology.inDataSource, dataSourceResource);
		
		return ModelFactory
				.createDefaultModel()
				.add(dataSetResource, RDF.type, LinkedBuildingDataOntology.DataSet);
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
		Resource dataSetResource = getDataSetResource(collectionId, dataSourceId, dataSetId);		
		if (!checkExists(collectionId, dataSourceId, dataSetId)) {
			throw ErrorFactory.createDataSetNotFoundException(dataSetResource);
		}
		
		UpdateRequest updateRequest1 = new ParameterizedSparqlString() {{
			setCommandText(
					"DELETE { ?dataSetUri ?p ?o } \n" +
					"WHERE { ?dataSetUri ?p ?o }");
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("dataSetUri", getDataSetResource(collectionId, dataSourceId, dataSetId).getURI());			
		}}.asUpdate();
		
		UpdateRequest updateRequest2 = new ParameterizedSparqlString() {{
			setCommandText(
					"DELETE { ?s ?p ?dataSetUri } \n" +
					"WHERE { ?s ?p ?dataSetUri }");
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("dataSetUri", getDataSetResource(collectionId, dataSourceId, dataSetId).getURI());			
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
		
		// execAsk() in Virtuoso always returns false 

		Query query = new ParameterizedSparqlString() {{
			setCommandText(
//					"ASK { \n" + 
					"SELECT (1 AS ?exists) { \n" + 
					"	?collectionUri a ?lbdho_Collection ; ?lbdho_hasDataSource ?dataSourceUri . \n" +
					"	?dataSourceUri a ?lbdho_DataSource ; ?lbdho_hasDataSet ?dataSetUri . \n" +
					"	?dataSetUri a ?lbdho_DataSet . \n" + 
					"}");			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", getCollectionResource(collectionId).getURI());
			setIri("dataSourceUri", getDataSourceResource(collectionId, dataSourceId).getURI());
			setIri("dataSetUri", getDataSetResource(collectionId, dataSourceId, dataSetId).getURI());			
		}}.asQuery();
		
		return QueryExecutionFactory
					.create(query, getMetaDataModel())
//					.execAsk();		
					.execSelect()
					.hasNext();
	}

}
