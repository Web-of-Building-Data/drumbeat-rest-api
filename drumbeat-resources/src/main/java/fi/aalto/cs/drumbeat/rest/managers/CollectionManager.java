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
	public Model getAll() {
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
	public Model getById(String collectionId) {
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
			throw new NotFoundException(
					String.format(
							"Collection <%s> not found", getCollectionResource(collectionId)));
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
	public Resource create(String collectionId, String name) {
		Resource collectionResource = getCollectionResource(collectionId);		
		if (checkExists(collectionId)) {
			throw new AlreadyExistsException(String.format("Collection <%s> already exists", collectionResource.getURI()));
		}
		
		collectionResource
			.inModel(getMetaDataModel())
			.addProperty(RDF.type, LinkedBuildingDataOntology.Collection)
			.addLiteral(LinkedBuildingDataOntology.name, name);
		
		return collectionResource;
	}
	
	
	/**
	 * Creates a specified collection 
	 * @param collectionId
	 * @param collectionId
	 * @return the recently created collection
	 * @throws NotFoundException if the collection is not found
	 */
	public void delete(String collectionId) {
		Resource collectionResource = getCollectionResource(collectionId);		
		if (!checkExists(collectionId)) {
			throw new NotFoundException(
					String.format("Collection <%s> not found", getCollectionResource(collectionId)));
		}
		
		getMetaDataModel()
			.removeAll(collectionResource, null, null);
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
					"	?collectionUri a ?lbdho_Collection . \n" + 
					"}");			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", getCollectionResource(collectionId).getURI());
		}}.asQuery();
		
		boolean result = 
				QueryExecutionFactory
					.create(query, getMetaDataModel())
					.execAsk();
		
		return result;
	}
	
	
	/**
	 * Checks if the collection exists
	 * @param collectionId
	 * @param collectionId
	 * @return true if the collection exists
	 */
	public boolean checkContainsChildren(String collectionId) {
		return getMetaDataModel()
				.contains(
						getCollectionResource(collectionId),
						LinkedBuildingDataOntology.hasDataSource);
	}

}
