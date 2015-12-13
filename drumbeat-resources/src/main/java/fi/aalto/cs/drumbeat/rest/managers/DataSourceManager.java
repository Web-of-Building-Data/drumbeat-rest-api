package fi.aalto.cs.drumbeat.rest.managers;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.AlreadyExistsException;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.ontology.LinkedBuildingDataOntology;
import fi.hut.cs.drumbeat.common.DrumbeatException;

public class DataSourceManager extends MetaDataManager {
	
	public DataSourceManager() throws DrumbeatException {
		this(DrumbeatApplication.getInstance().getMetaDataModel());
	}
	
	public DataSourceManager(Model metaDataModel) {
		super(metaDataModel);
	}
	
	/**
	 * Gets all dataSources that belong to the specified collection
	 * @param collectionId
	 * @return List of statements <<dataSource>> rdf:type lbdho:DataSource
	 * @throws NotFoundException if the collection is not found
	 */
	public Model getAll(String collectionId) {
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"SELECT (?dataSourceUri AS ?subject) (rdf:type AS ?predicate) (?lbdho_DataSource AS ?object) { \n" + 
					"	?collectionUri a ?lbdho_Collection ; ?lbdho_hasDataSource ?dataSourceUri . \n" +
					"	?dataSourceUri a ?lbdho_DataSource . \n" +
					"} \n" + 
					"ORDER BY ?subject");
			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", getCollectionResource(collectionId).getURI());
		}}.asQuery();
		
		ResultSet resultSet = 
				QueryExecutionFactory
					.create(query, getMetaDataModel())
					.execSelect();
		
		if (!resultSet.hasNext()) {
			CollectionManager collectionManager = new CollectionManager(getMetaDataModel()); 
			if (!collectionManager.checkExists(collectionId)) {
				throw new NotFoundException(
						String.format("Collection <%s> not found", getCollectionResource(collectionId)));
			}
		}
		
		return convertResultSetToModel(resultSet);
	}
	
	
	/**
	 * Gets all properties of a specified dataSource 
	 * @param collectionId
	 * @param dataSourceId
	 * @return List of statements <<dataSource>> ?predicate ?object
	 * @throws NotFoundException if the dataset is not found
	 */
	public Model getById(String collectionId, String dataSourceId) {
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"SELECT (?dataSourceUri AS ?subject) ?predicate ?object { \n" + 
					"	?collectionUri a ?lbdho_Collection ; ?lbdho_hasDataSource ?dataSourceUri . \n" +
					"	?dataSourceUri a ?lbdho_DataSource ; ?predicate ?object . \n" +
					"} \n" + 
					"ORDER BY ?subject ?predicate ?object");
			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", getCollectionResource(collectionId).getURI());
			setIri("dataSourceUri", getDataSourceResource(collectionId, dataSourceId).getURI());
		}}.asQuery();
		
		ResultSet resultSet = 
				QueryExecutionFactory
					.create(query, getMetaDataModel())
					.execSelect();
		
		if (!resultSet.hasNext()) {
			throw new NotFoundException(
					String.format(
							"DataSource <%s> not found", getDataSourceResource(collectionId, dataSourceId)));
		}
		
		return convertResultSetToModel(resultSet);
	}
	
	
	/**
	 * Creates a specified dataSource 
	 * @param collectionId
	 * @param dataSourceId
	 * @return the recently created dataSource
	 * @throws AlreadyExistsException if the dataSource already exists
	 * @throws NotFoundException if the collection is not found
	 */
	public Resource create(String collectionId, String dataSourceId, String name) {
		CollectionManager collectionManager = new CollectionManager(getMetaDataModel()); 
		Resource collectionResource = getCollectionResource(collectionId);		
		if (!collectionManager.checkExists(collectionId)) {
			throw new NotFoundException(
					String.format("Collection <%s> not found", collectionResource.getURI()));
		}
		
		Resource dataSourceResource = getDataSourceResource(collectionId, dataSourceId);		
		if (checkExists(collectionId, dataSourceId)) {
			throw new AlreadyExistsException(String.format("DataSource <%s> already exists", dataSourceResource.getURI()));
		}
		
		
		collectionResource
			.inModel(getMetaDataModel())
			.addProperty(LinkedBuildingDataOntology.hasDataSource, dataSourceResource);
	
		dataSourceResource
			.inModel(getMetaDataModel())
			.addProperty(RDF.type, LinkedBuildingDataOntology.DataSource)
			.addLiteral(LinkedBuildingDataOntology.name, name)
			.addProperty(LinkedBuildingDataOntology.inCollection, collectionResource);
		
		return dataSourceResource;
	}
	
	
	/**
	 * Creates a specified dataSource 
	 * @param collectionId
	 * @param dataSourceId
	 * @return the recently created dataSource
	 * @throws NotFoundException if the dataSource is not found
	 */
	public void delete(String collectionId, String dataSourceId) {
		Resource dataSourceResource = getDataSourceResource(collectionId, dataSourceId);		
		if (!checkExists(collectionId, dataSourceId)) {
			throw new NotFoundException(
					String.format("DataSource <%s> not found", getDataSourceResource(collectionId, dataSourceId)));
		}
		
		getMetaDataModel()
			.removeAll(dataSourceResource, null, null)
			.removeAll(null, LinkedBuildingDataOntology.hasDataSource, dataSourceResource);
	}
	
	
	/**
	 * Checks if the dataSource exists
	 * @param collectionId
	 * @param dataSourceId
	 * @return true if the dataSource exists
	 */
	public boolean checkExists(String collectionId, String dataSourceId) {
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"ASK { \n" + 
					"	?collectionUri a ?lbdho_Collection ; ?lbdho_hasDataSource ?dataSourceUri . \n" +
					"	?dataSourceUri a ?lbdho_DataSource . \n" + 
					"}");			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", getCollectionResource(collectionId).getURI());
			setIri("dataSourceUri", getDataSourceResource(collectionId, dataSourceId).getURI());
		}}.asQuery();
		
		boolean result = 
				QueryExecutionFactory
					.create(query, getMetaDataModel())
					.execAsk();
		
		return result;
	}
	
	
	/**
	 * Checks if the dataSource exists
	 * @param collectionId
	 * @param dataSourceId
	 * @return true if the dataSource exists
	 */
	public boolean checkContainsChildren(String collectionId) {
		return getMetaDataModel()
				.contains(
						getCollectionResource(collectionId),
						LinkedBuildingDataOntology.hasDataSet);
	}
	
	

}
