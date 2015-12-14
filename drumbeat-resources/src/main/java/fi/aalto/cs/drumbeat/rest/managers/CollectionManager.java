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
import fi.hut.cs.drumbeat.common.DrumbeatException;

public class CollectionManager extends MetaDataManager {
	
	public CollectionManager() throws DrumbeatException {
		this(DrumbeatApplication.getInstance().getMetaDataModel());
	}
	
	public CollectionManager(Model metaDataModel) {
		super(metaDataModel);
	}
	

	/**
	 * Gets all collections that belong to the specified collection
	 * @return List of statements <<collection>> rdf:type lbdho:Collection
	 * @throws NotFoundException if the collection is not found
	 */
	public Model getAll()
		throws NotFoundException
	{
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"SELECT (?collectionUri AS ?subject) (rdf:type AS ?predicate) (?lbdho_Collection AS ?object) { \n" + 
					"	?collectionUri a ?lbdho_Collection . \n" +
					"} \n" + 
					"ORDER BY ?subject");
			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
		}}.asQuery();
		
		ResultSet resultSet = 
				QueryExecutionFactory
					.create(query, getMetaDataModel())
					.execSelect();
		
		return convertResultSetToModel(resultSet);
	}
	
	
	/**
	 * Gets all properties of a specified collection 
	 * @param collectionId
	 * @return List of statements <<collection>> ?predicate ?object
	 * @throws NotFoundException if the collection is not found
	 */
	public Model getById(String collectionId)
		throws NotFoundException
	{
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"SELECT (?collectionUri AS ?subject) ?predicate ?object { \n" + 
					"	?collectionUri a ?lbdho_Collection ; ?predicate ?object . \n" +
					"} \n" + 
					"ORDER BY ?subject ?predicate ?object");
			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", getCollectionResource(collectionId).getURI());
		}}.asQuery();
		
		ResultSet resultSet = 
				QueryExecutionFactory
					.create(query, getMetaDataModel())
					.execSelect();
		
		if (!resultSet.hasNext()) {
			throw ErrorFactory.createCollectionNotFoundException(getCollectionResource(collectionId));
		}
		
		return convertResultSetToModel(resultSet);
	}
	
	
	/**
	 * Creates a specified collection 
	 * @param collectionId
	 * @param collectionId
	 * @return the recently created collection
	 * @throws AlreadyExistsException if the collection already exists
	 */
	public Model create(String collectionId, String name)
		throws AlreadyExistsException
	{
		Resource collectionResource = getCollectionResource(collectionId);		
		if (checkExists(collectionId)) {
			throw ErrorFactory.createCollectionAlreadyExistsException(collectionResource);
		}
		
		collectionResource
			.inModel(getMetaDataModel())
			.addProperty(RDF.type, LinkedBuildingDataOntology.Collection)
			.addLiteral(LinkedBuildingDataOntology.name, name);
		
		return ModelFactory
				.createDefaultModel()
				.add(collectionResource, RDF.type, LinkedBuildingDataOntology.Collection);
	}
	
	
	/**
	 * Creates a specified collection 
	 * @param collectionId
	 * @param collectionId
	 * @return the recently created collection
	 * @throws NotFoundException if the collection is not found
	 */
	public void delete(String collectionId)
		throws NotFoundException, DeleteDeniedException
	{
		Resource collectionResource = getCollectionResource(collectionId);		
		if (!checkExists(collectionId)) {
			throw ErrorFactory.createCollectionNotFoundException(collectionResource);
		}
		
		if (checkHasChildren(collectionId)) {
			throw ErrorFactory.createCollectionHasChildren(collectionResource);
		}		
		
		UpdateRequest updateRequest1 = new ParameterizedSparqlString() {{
			setCommandText(
					"DELETE { ?collectionUri ?p ?o } \n" +
					"WHERE { ?collectionUri ?p ?o }");
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", getCollectionResource(collectionId).getURI());			
		}}.asUpdate();
		
		UpdateAction.execute(updateRequest1, getMetaDataModel());
	}
	
	
	/**
	 * Checks if the collection exists
	 * @param collectionId
	 * @param collectionId
	 * @return true if the collection exists
	 */
	public boolean checkExists(String collectionId) {
		// execAsk() in Virtuoso always returns false 

		Query query = new ParameterizedSparqlString() {{
			setCommandText(
//					"ASK { \n" + 
					"SELECT (1 AS ?exists) { \n" + 
					"	?collectionUri a ?lbdho_Collection . \n" + 
					"}");			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", getCollectionResource(collectionId).getURI());
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
	 * Checks if the collection exists
	 * @param collectionId
	 * @param collectionId
	 * @return true if the collection exists
	 */
	public boolean checkHasChildren(String collectionId) {
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"SELECT (1 AS ?exists) { \n" + 
					"	?collectionUri a ?lbdho_Collection ; ?lbdho_hasDataSource ?dataSourceUri . \n" + 
					"}");			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", getCollectionResource(collectionId).getURI());
		}}.asQuery();
		
		boolean result = 
				QueryExecutionFactory
					.create(query, getMetaDataModel())
					.execSelect()
					.hasNext();
		
		return result;
	}

}
