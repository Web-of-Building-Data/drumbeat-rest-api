package fi.aalto.cs.drumbeat.rest.managers;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.AlreadyExistsException;
import com.hp.hpl.jena.shared.NotFoundException;
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
	public Model getAll(String collectionId, String dataSourceId) {
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
				throw new NotFoundException(
						String.format("DataSource <%s> not found", getDataSourceResource(collectionId, dataSourceId)));
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
	public Model getById(String collectionId, String dataSourceId, String dataSetId) {
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
			throw new NotFoundException(
					String.format(
							"DataSet <%s> not found", getDataSetResource(collectionId, dataSourceId, dataSetId)));
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
	public Resource create(String collectionId, String dataSourceId, String dataSetId, String name) {
		DataSourceManager dataSourceManager = new DataSourceManager(getMetaDataModel()); 
		Resource dataSourceResource = getDataSourceResource(collectionId, dataSourceId);		
		if (!dataSourceManager.checkExists(collectionId, dataSourceId)) {
			throw new NotFoundException(
					String.format("DataSource <%s> not found", dataSourceResource.getURI()));
		}
		
		Resource dataSetResource = getDataSetResource(collectionId, dataSourceId, dataSetId);		
		if (checkExists(collectionId, dataSourceId, dataSetId)) {
			throw new AlreadyExistsException(String.format("DataSet <%s> already exists", dataSetResource.getURI()));
		}
		
		
		dataSourceResource
			.inModel(getMetaDataModel())
			.addProperty(LinkedBuildingDataOntology.hasDataSet, dataSetResource);
	
		dataSetResource
			.inModel(getMetaDataModel())
			.addProperty(RDF.type, LinkedBuildingDataOntology.DataSet)
			.addLiteral(LinkedBuildingDataOntology.name, name)
			.addProperty(LinkedBuildingDataOntology.inDataSource, dataSourceResource);
		
		return dataSetResource;
	}
	
	
	/**
	 * Creates a specified dataSet 
	 * @param collectionId
	 * @param dataSourceId
	 * @param dataSetId
	 * @return the recently created dataSet
	 * @throws NotFoundException if the dataSet is not found
	 */
	public void delete(String collectionId, String dataSourceId, String dataSetId) {
		Resource dataSetResource = getDataSetResource(collectionId, dataSourceId, dataSetId);		
		if (!checkExists(collectionId, dataSourceId, dataSetId)) {
			throw new NotFoundException(
					String.format("DataSet <%s> not found", getDataSetResource(collectionId, dataSourceId, dataSetId)));
		}
		
		getMetaDataModel()
			.removeAll(dataSetResource, null, null)
			.removeAll(null, LinkedBuildingDataOntology.hasDataSet, dataSetResource);
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
					.execAsk();		
	}

}
