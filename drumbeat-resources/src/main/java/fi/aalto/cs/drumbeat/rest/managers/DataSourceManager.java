package fi.aalto.cs.drumbeat.rest.managers;

import static fi.aalto.cs.drumbeat.rest.common.NameFormatter.*;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.AlreadyExistsException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.NotFoundException;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.RDF;

import fi.aalto.cs.drumbeat.common.DrumbeatException;
import fi.aalto.cs.drumbeat.rdf.jena.provider.JenaProvider;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatOntology;

public class DataSourceManager extends DrumbeatManager {
	
	public DataSourceManager() throws DrumbeatException {
	}
	
	public DataSourceManager(Model metaDataModel, JenaProvider jenaProvider) {
		super(metaDataModel, jenaProvider);
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
					"CONSTRUCT { \n" +
					"	?dataSourceUri rdf:type lbdho:DataSource \n" +
					"} WHERE { \n" + 
					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?dataSourceUri . \n" +
					"	?dataSourceUri a lbdho:DataSource . \n" +
					"} \n" + 
					"ORDER BY ?dataSourceUri");
			
			DrumbeatOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
		}}.asQuery();
		
		Model resultModel = 
				createQueryExecution(query, getMetaDataModel())
					.execConstruct();
		
		if (resultModel.isEmpty()) {
			CollectionManager collectionManager = new CollectionManager(getMetaDataModel(), getJenaProvider()); 
			if (!collectionManager.checkExists(collectionId)) {
				throw ErrorFactory.createCollectionNotFoundException(collectionId);
			}
		}
		
		return resultModel;
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
					"CONSTRUCT { \n" +
					"	?dataSourceUri ?predicate ?object \n" +
					"} WHERE { \n" + 
					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?dataSourceUri . \n" +
					"	?dataSourceUri a lbdho:DataSource ; ?predicate ?object . \n" +
					"} \n" + 
					"ORDER BY ?predicate ?object");
			
			DrumbeatOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
			setIri("dataSourceUri", formatDataSourceResourceUri(collectionId, dataSourceId));
		}}.asQuery();
		
		Model resultModel = 
				createQueryExecution(query, getMetaDataModel())
					.execConstruct();
		
		if (resultModel.isEmpty()) {
			throw ErrorFactory.createDataSourceNotFoundException(collectionId, dataSourceId);
		}
		
		return resultModel;
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
		CollectionManager collectionManager = new CollectionManager(getMetaDataModel(), getJenaProvider()); 
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
			.addProperty(DrumbeatOntology.LBDHO.hasDataSource, dataSourceResource);
	
		dataSourceResource
			.addProperty(RDF.type, DrumbeatOntology.LBDHO.DataSource)
			.addLiteral(DrumbeatOntology.LBDHO.name, metaDataModel.createTypedLiteral(name))
			.addProperty(DrumbeatOntology.LBDHO.inCollection, collectionResource);		
		
		return ModelFactory
				.createDefaultModel()
				.add(dataSourceResource, RDF.type, DrumbeatOntology.LBDHO.DataSource);
	}
	
	
	/**
	 * Creates a specified dataSource 
	 * @param collectionId
	 * @param dataSourceId
	 * @return the recently created dataSource
	 * @throws NotFoundException if the dataSource is not found
	 * throws DeleteDeniedException if dataSource has children
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
			DrumbeatOntology.fillParameterizedSparqlString(this);
			setIri("dataSourceUri", formatDataSourceResourceUri(collectionId, dataSourceId));			
		}}.asUpdate();
		
		UpdateRequest updateRequest2 = new ParameterizedSparqlString() {{
			setCommandText(
					"DELETE { ?s ?p ?dataSourceUri } \n" +
					"WHERE { ?s ?p ?dataSourceUri }");
			DrumbeatOntology.fillParameterizedSparqlString(this);
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
		
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"ASK { \n" + 
					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?dataSourceUri . \n" +
					"	?dataSourceUri a lbdho:DataSource . \n" + 
					"}");			
			DrumbeatOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
			setIri("dataSourceUri", formatDataSourceResourceUri(collectionId, dataSourceId));
		}}.asQuery();
		
		boolean result = 
				createQueryExecution(query, getMetaDataModel())
					.execAsk();
		
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
					"ASK { \n" + 
					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?dataSourceUri . \n" +
					"	?dataSourceUri a lbdho:DataSource ; lbdho:hasDataSet ?dataSetUri . \n" + 
					"}");			
			DrumbeatOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
			setIri("dataSourceUri", formatDataSourceResourceUri(collectionId, dataSourceId));
		}}.asQuery();
		
		boolean result = 
				createQueryExecution(query, getMetaDataModel())
					.execAsk();
		
		return result;
	}
	
	

}
