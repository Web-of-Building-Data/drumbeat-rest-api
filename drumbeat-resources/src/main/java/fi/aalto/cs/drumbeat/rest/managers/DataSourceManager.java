package fi.aalto.cs.drumbeat.rest.managers;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.AlreadyExistsException;
import com.hp.hpl.jena.shared.DeleteDeniedException;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.ontology.LinkedBuildingDataOntology;
import static fi.aalto.cs.drumbeat.rest.ontology.LinkedBuildingDataOntology.*;
import fi.hut.cs.drumbeat.common.DrumbeatException;

public class DataSourceManager extends DrumbeatManager {
	
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
	public Model getAll(String collectionId)
		throws NotFoundException
	{
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"SELECT (?dataSourceUri AS ?subject) (rdf:type AS ?predicate) (lbdho:DataSource AS ?object) { \n" + 
					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?dataSourceUri . \n" +
					"	?dataSourceUri a lbdho:DataSource . \n" +
					"} \n" + 
					"ORDER BY ?subject");
			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
		}}.asQuery();
		
		ResultSet resultSet = 
				QueryExecutionFactory
					.create(query, getMetaDataModel())
					.execSelect();
		
		if (!resultSet.hasNext()) {
			CollectionManager collectionManager = new CollectionManager(getMetaDataModel()); 
			if (!collectionManager.checkExists(collectionId)) {
				throw ErrorFactory.createCollectionNotFoundException(collectionId);
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
	public Model getById(String collectionId, String dataSourceId)
		throws NotFoundException
	{
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"SELECT (?dataSourceUri AS ?subject) ?predicate ?object { \n" + 
					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?dataSourceUri . \n" +
					"	?dataSourceUri a lbdho:DataSource ; ?predicate ?object . \n" +
					"} \n" + 
					"ORDER BY ?subject ?predicate ?object");
			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
			setIri("dataSourceUri", formatDataSourceResourceUri(collectionId, dataSourceId));
		}}.asQuery();
		
		ResultSet resultSet = 
				QueryExecutionFactory
					.create(query, getMetaDataModel())
					.execSelect();
		
		if (!resultSet.hasNext()) {
			throw ErrorFactory.createDataSourceNotFoundException(collectionId, dataSourceId);
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
	public Model create(String collectionId, String dataSourceId, String name)
		throws AlreadyExistsException, NotFoundException
	{
		CollectionManager collectionManager = new CollectionManager(getMetaDataModel()); 
		if (!collectionManager.checkExists(collectionId)) {
			throw ErrorFactory.createCollectionNotFoundException(collectionId);
		}
		
		if (checkExists(collectionId, dataSourceId)) {
			throw ErrorFactory.createDataSourceAlreadyExistsException(collectionId, dataSourceId);
		}
		
		Model metaDataModel = getMetaDataModel();		

		Resource collectionResource = metaDataModel
				.createResource(formatCollectionResourceUri(collectionId));
		
		Resource dataSourceResource = metaDataModel 
				.createResource(formatDataSourceResourceUri(collectionId, dataSourceId));
		
		collectionResource
			.addProperty(LinkedBuildingDataOntology.hasDataSource, dataSourceResource);
	
		dataSourceResource
			.addProperty(RDF.type, LinkedBuildingDataOntology.DataSource)
			.addLiteral(LinkedBuildingDataOntology.name, metaDataModel.createTypedLiteral(name))
			.addProperty(LinkedBuildingDataOntology.inCollection, collectionResource);		
		
		return ModelFactory
				.createDefaultModel()
				.add(dataSourceResource, RDF.type, LinkedBuildingDataOntology.DataSource);
	}
	
	
	/**
	 * Creates a specified dataSource 
	 * @param collectionId
	 * @param dataSourceId
	 * @return the recently created dataSource
	 * @throws NotFoundException if the dataSource is not found
	 */
	public void delete(String collectionId, String dataSourceId)
		throws NotFoundException, DeleteDeniedException
	{
		if (!checkExists(collectionId, dataSourceId)) {
			throw ErrorFactory.createDataSourceNotFoundException(collectionId, dataSourceId);
		}
		
		if (checkHasChildren(collectionId, dataSourceId)) {
			throw ErrorFactory.createDataSourceHasChildrenException(collectionId, dataSourceId);
		}
		
		UpdateRequest updateRequest1 = new ParameterizedSparqlString() {{
			setCommandText(
					"DELETE { ?dataSourceUri ?p ?o } \n" +
					"WHERE { ?dataSourceUri ?p ?o }");
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("dataSourceUri", formatDataSourceResourceUri(collectionId, dataSourceId));			
		}}.asUpdate();
		
		UpdateRequest updateRequest2 = new ParameterizedSparqlString() {{
			setCommandText(
					"DELETE { ?s ?p ?dataSourceUri } \n" +
					"WHERE { ?s ?p ?dataSourceUri }");
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("dataSourceUri", formatDataSourceResourceUri(collectionId, dataSourceId));			
		}}.asUpdate();

		UpdateAction.execute(updateRequest1, getMetaDataModel());
		UpdateAction.execute(updateRequest2, getMetaDataModel());		
	}
	
	
	/**
	 * Checks if the dataSource exists
	 * @param collectionId
	 * @param dataSourceId
	 * @return true if the dataSource exists
	 */
	public boolean checkExists(String collectionId, String dataSourceId) {
		
		// execAsk() in Virtuoso always returns false 
		
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
//					"ASK { \n" + 
					"SELECT (1 AS ?exists) { \n" + 
					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?dataSourceUri . \n" +
					"	?dataSourceUri a lbdho:DataSource . \n" + 
					"}");			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
			setIri("dataSourceUri", formatDataSourceResourceUri(collectionId, dataSourceId));
		}}.asQuery();
		
		boolean result = 
				QueryExecutionFactory
					.create(query, getMetaDataModel())
//					.execAsk();
					.execSelect()
					.hasNext();
		
		return result;
	}
	
	
	/**
	 * Checks if the dataSource has children dataSets
	 * @param collectionId
	 * @param dataSourceId
	 * @return
	 */
	public boolean checkHasChildren(String collectionId, String dataSourceId) {
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
//					"ASK { \n" + 
					"SELECT (1 AS ?exists) { \n" + 
					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?dataSourceUri . \n" +
					"	?dataSourceUri a lbdho:DataSource ; lbdho:hasDataSet ?dataSetUri . \n" + 
					"}");			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
			setIri("dataSourceUri", formatDataSourceResourceUri(collectionId, dataSourceId));
		}}.asQuery();
		
		boolean result = 
				QueryExecutionFactory
					.create(query, getMetaDataModel())
//					.execAsk();
					.execSelect()
					.hasNext();
		
		return result;
	}
	
	

}
