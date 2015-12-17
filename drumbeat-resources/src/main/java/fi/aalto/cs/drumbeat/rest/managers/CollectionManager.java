package fi.aalto.cs.drumbeat.rest.managers;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.AlreadyExistsException;
import com.hp.hpl.jena.shared.DeleteDeniedException;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.aalto.cs.drumbeat.rest.ontology.LinkedBuildingDataOntology;
import static fi.aalto.cs.drumbeat.rest.ontology.LinkedBuildingDataOntology.*;
import fi.hut.cs.drumbeat.common.DrumbeatException;
import fi.hut.cs.drumbeat.rdf.modelfactory.JenaProvider;

public class CollectionManager extends DrumbeatManager {
	
	public CollectionManager() throws DrumbeatException {
	}

	public CollectionManager(Model metaDataModel, JenaProvider jenaProvider) {
		super(metaDataModel, jenaProvider);
	}

	/**
	 * Gets all collections that belong to the specified collection
	 * @return List of statements <<collection>> rdf:type lbdho:Collection
	 * @throws NotFoundException if the collection is not found
	 */
	public Model getAll()
	{
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"CONSTRUCT { \n" +
					"	?collectionUri rdf:type lbdho:Collection \n" +
					"} WHERE { \n" + 
					"	?collectionUri a lbdho:Collection . \n" +
					"} \n" + 
					"ORDER BY ?collectionUri");
			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
		}}.asQuery();
	
		Model result = 
				createQueryExecution(query, getMetaDataModel())
					.execConstruct();
		
		return result;
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
					"CONSTRUCT { \n" +
					"	?collectionUri ?predicate ?object \n" +
					"} WHERE { \n" + 
					"	?collectionUri a lbdho:Collection ; ?predicate ?object . \n" +
					"} \n" + 
					"ORDER BY ?subject ?predicate ?object");
			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
		}}.asQuery();
		
		Model result =
				createQueryExecution(query, getMetaDataModel())
					.execConstruct();
		
		if (result.isEmpty()) {
			throw ErrorFactory.createCollectionNotFoundException(collectionId);
		}
		
		return result;
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
		if (checkExists(collectionId)) {
			throw ErrorFactory.createCollectionAlreadyExistsException(collectionId);
		}		
		
		Model metaDataModel = getMetaDataModel();		
		
		Resource collectionResource = metaDataModel
				.createResource(formatCollectionResourceUri(collectionId))
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
		if (!checkExists(collectionId)) {
			throw ErrorFactory.createCollectionNotFoundException(collectionId);
		}
		
		if (checkHasChildren(collectionId)) {
			throw ErrorFactory.createCollectionHasChildrenException(collectionId);
		}		
		
		UpdateRequest updateRequest1 = new ParameterizedSparqlString() {{
			setCommandText(
					"DELETE { ?collectionUri ?p ?o } \n" +
					"WHERE { ?collectionUri ?p ?o }");
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));			
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
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"ASK { \n" + 
					"	?collectionUri a lbdho:Collection . \n" + 
					"}");			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
		}}.asQuery();
		
		boolean result = 
				createQueryExecution(query, getMetaDataModel())
					.execAsk();
		
		return result;
	}
	
	
	/**
	 * Checks if the collection has children dataSources
	 * @param collectionId
	 * @param collectionId
	 * @return
	 */
	public boolean checkHasChildren(String collectionId) {
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"ASK { \n" + 
					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?dataSourceUri . \n" + 
					"}");			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
		}}.asQuery();
		
		boolean result = 
				createQueryExecution(query, getMetaDataModel())
					.execAsk();
		
		return result;
	}

}
