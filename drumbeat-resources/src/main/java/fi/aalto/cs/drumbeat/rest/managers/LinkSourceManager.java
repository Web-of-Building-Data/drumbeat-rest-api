package fi.aalto.cs.drumbeat.rest.managers;

import static fi.aalto.cs.drumbeat.rest.common.LinkedBuildingDataOntology.*;

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

import fi.aalto.cs.drumbeat.common.DrumbeatException;
import fi.aalto.cs.drumbeat.rdf.jena.provider.JenaProvider;
import fi.aalto.cs.drumbeat.rest.common.LinkedBuildingDataOntology;

public class LinkSourceManager extends DrumbeatManager {
	
	public LinkSourceManager() throws DrumbeatException {
	}
	
	public LinkSourceManager(Model metaDataModel, JenaProvider jenaProvider) {
		super(metaDataModel, jenaProvider);
	}	
	
	/**
	 * Gets all linkSources that belong to the specified collection
	 * @param collectionId
	 * @return List of statements <<linkSource>> rdf:type lbdho:LinkSource
	 * @throws NotFoundException if the collection is not found
	 */
	public Model getAll(String collectionId)
		throws NotFoundException
	{
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"CONSTRUCT { \n" +
					"	?linkSourceUri rdf:type lbdho:LinkSource \n" +
					"} WHERE { \n" + 
					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?linkSourceUri . \n" +
					"	?linkSourceUri a lbdho:LinkSource . \n" +
					"} \n" + 
					"ORDER BY ?linkSourceUri");
			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
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
	 * Gets all linkSources that belong to the specified collection
	 * @param collectionId
	 * @return List of statements <<linkSource>> rdf:type lbdho:LinkSource
	 */
	public Model getAllLinkSourcesOfDataSource(String collectionId, String originalDataSetId)
	{
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"CONSTRUCT { \n" +
					"	?linkSourceUri rdf:type lbdho:LinkSource \n" +
					"} WHERE { \n" + 
					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?dataSourceUri ." +
					"	?dataSourceUri a lbdho:DataSource ; lbdho:hasLinkSource ?linkSourceUri . \n" +
					"	?linkSourceUri a lbdho:LinkSource . \n" +
					"} \n" + 
					"ORDER BY ?linkSourceUri");
			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
			setIri("dataSourceUri", formatDataSourceResourceUri(collectionId, originalDataSetId));
		}}.asQuery();
		
		Model resultModel = 
				createQueryExecution(query, getMetaDataModel())
					.execConstruct();
		
		return resultModel;
	}
	
	/**
	 * Gets all properties of a specified linkSource 
	 * @param collectionId
	 * @param linkSourceId
	 * @return List of statements <<linkSource>> ?predicate ?object
	 * @throws NotFoundException if the linkset is not found
	 */
	public Model getById(String collectionId, String linkSourceId)
		throws NotFoundException
	{
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"CONSTRUCT { \n" +
					"	?linkSourceUri ?predicate ?object \n" +
					"} WHERE { \n" + 
					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?linkSourceUri . \n" +
					"	?linkSourceUri a lbdho:LinkSource ; ?predicate ?object . \n" +
					"} \n" + 
					"ORDER BY ?predicate ?object");
			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
			setIri("linkSourceUri", formatDataSourceResourceUri(collectionId, linkSourceId));
		}}.asQuery();
		
		Model resultModel = 
				createQueryExecution(query, getMetaDataModel())
					.execConstruct();
		
		if (resultModel.isEmpty()) {
			throw ErrorFactory.createDataSourceNotFoundException(collectionId, linkSourceId);
		}
		
		return resultModel;
	}
	
	
	/**
	 * Creates a specified linkSource 
	 * @param collectionId
	 * @param linkSourceId
	 * @return the recently created linkSource
	 * @throws AlreadyExistsException if the linkSource already exists
	 * @throws NotFoundException if the collection is not found
	 */
	public Model create(String collectionId, String linkSourceId, String name, String originalDataSourceId)
		throws AlreadyExistsException, NotFoundException
	{
		CollectionManager collectionManager = new CollectionManager(getMetaDataModel(), getJenaProvider()); 
		if (!collectionManager.checkExists(collectionId)) {
			throw ErrorFactory.createCollectionNotFoundException(collectionId);
		}
		
		if (checkExists(collectionId, linkSourceId)) {
			throw ErrorFactory.createDataSourceAlreadyExistsException(collectionId, linkSourceId);
		}
		
		Model metaDataModel = getMetaDataModel();		

		Resource collectionResource = metaDataModel
				.createResource(formatCollectionResourceUri(collectionId));
		
		Resource linkSourceResource = metaDataModel 
				.createResource(formatDataSourceResourceUri(collectionId, linkSourceId));
		
		Resource originalDataSourceResource = metaDataModel 
				.createResource(formatDataSourceResourceUri(collectionId, originalDataSourceId));
		
		collectionResource
			.addProperty(LinkedBuildingDataOntology.hasDataSource, linkSourceResource);
		
		originalDataSourceResource
			.addProperty(LinkedBuildingDataOntology.hasLinkSource, linkSourceResource);
	
		linkSourceResource
			.addProperty(RDF.type, LinkedBuildingDataOntology.DataSource)
			.addProperty(RDF.type, LinkedBuildingDataOntology.LinkSource)			
			.addLiteral(LinkedBuildingDataOntology.name, metaDataModel.createTypedLiteral(name))
			.addProperty(LinkedBuildingDataOntology.inCollection, collectionResource)
			.addProperty(LinkedBuildingDataOntology.hasOriginalDataSource, originalDataSourceResource);		
		
		return ModelFactory
				.createDefaultModel()
				.add(linkSourceResource, RDF.type, LinkedBuildingDataOntology.LinkSource);
	}
	
	
	/**
	 * Creates a specified linkSource 
	 * @param collectionId
	 * @param linkSourceId
	 * @return the recently created linkSource
	 * @throws NotFoundException if the linkSource is not found
	 * throws DeleteDeniedException if linkSource has children
	 */
	public void delete(String collectionId, String linkSourceId)
		throws NotFoundException, DeleteDeniedException
	{
		if (!checkExists(collectionId, linkSourceId)) {
			throw ErrorFactory.createDataSourceNotFoundException(collectionId, linkSourceId);
		}
		
		if (checkHasChildren(collectionId, linkSourceId)) {
			throw ErrorFactory.createDataSourceHasChildrenException(collectionId, linkSourceId);
		}
		
		UpdateRequest updateRequest1 = new ParameterizedSparqlString() {{
			setCommandText(
					"DELETE { ?linkSourceUri ?p ?o } \n" +
					"WHERE { ?linkSourceUri ?p ?o }");
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("linkSourceUri", formatDataSourceResourceUri(collectionId, linkSourceId));			
		}}.asUpdate();
		
		UpdateRequest updateRequest2 = new ParameterizedSparqlString() {{
			setCommandText(
					"DELETE { ?s ?p ?linkSourceUri } \n" +
					"WHERE { ?s ?p ?linkSourceUri }");
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("linkSourceUri", formatDataSourceResourceUri(collectionId, linkSourceId));			
		}}.asUpdate();

		UpdateAction.execute(updateRequest1, getMetaDataModel());
		UpdateAction.execute(updateRequest2, getMetaDataModel());		
	}
	
	
	/**
	 * Checks if the linkSource exists
	 * @param collectionId
	 * @param linkSourceId
	 * @return true if the linkSource exists
	 */
	public boolean checkExists(String collectionId, String linkSourceId) {
		
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"ASK { \n" + 
					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?linkSourceUri . \n" +
					"	?linkSourceUri a lbdho:LinkSource . \n" + 
					"}");			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
			setIri("linkSourceUri", formatDataSourceResourceUri(collectionId, linkSourceId));
		}}.asQuery();
		
		boolean result = 
				createQueryExecution(query, getMetaDataModel())
					.execAsk();
		
		return result;
	}
	
	
	/**
	 * Checks if the linkSource has children linkSets
	 * @param collectionId
	 * @param linkSourceId
	 * @return
	 */
	public boolean checkHasChildren(String collectionId, String linkSourceId) {
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"ASK { \n" + 
					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?linkSourceUri . \n" +
					"	?linkSourceUri a lbdho:LinkSource ; lbdho:hasDataSet ?linkSetUri . \n" + 
					"}");			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
			setIri("linkSourceUri", formatDataSourceResourceUri(collectionId, linkSourceId));
		}}.asQuery();
		
		boolean result = 
				createQueryExecution(query, getMetaDataModel())
					.execAsk();
		
		return result;
	}
	
	

}
