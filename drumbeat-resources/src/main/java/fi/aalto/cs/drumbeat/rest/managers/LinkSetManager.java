package fi.aalto.cs.drumbeat.rest.managers;

import static fi.aalto.cs.drumbeat.rest.common.NameFormatter.*;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.AlreadyExistsException;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.aalto.cs.drumbeat.common.DrumbeatException;
import fi.aalto.cs.drumbeat.common.string.StringUtils;
import fi.aalto.cs.drumbeat.rdf.jena.provider.JenaProvider;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatOntology;

public class LinkSetManager extends DrumbeatManager {
	
//	private static final Logger logger = Logger.getLogger(LinkSetManager.class);	
	
	public LinkSetManager() throws DrumbeatException {
	}
	
	public LinkSetManager(Model metaDataModel, JenaProvider jenaProvider) {
		super(metaDataModel, jenaProvider);
	}

	/**
	 * Gets all linkSets that belong to the specified collection and datasource.
	 * @param collectionId
	 * @param linkSourceId
	 * @return List of statements <<linkSet>> rdf:type lbdho:LinkSet
	 * @throws NotFoundException if the datasource is not found
	 */
	public Model getAll(String collectionId, String linkSourceId)
		throws NotFoundException
	{
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"CONSTRUCT { \n" +
					"	?linkSetUri rdf:type lbdho:LinkSet \n" +
					"} WHERE { \n" + 
					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?linkSourceUri . \n" +
					"	?linkSourceUri a lbdho:LinkSource ; lbdho:hasDataSet ?linkSetUri . \n" +
					"	?linkSetUri a lbdho:LinkSet . \n" +
					"} \n" + 
					"ORDER BY ?linkSetUri");
			
			DrumbeatOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
			setIri("linkSourceUri", formatDataSourceResourceUri(collectionId, linkSourceId));
		}}.asQuery();
		
		Model result = 
				createQueryExecution(query, getMetaDataModel())
					.execConstruct();
		
		if (result.isEmpty()) {
			LinkSourceManager linkSourceManager = new LinkSourceManager(getMetaDataModel(), getJenaProvider()); 
			if (!linkSourceManager.checkExists(collectionId, linkSourceId)) {
				throw ErrorFactory.createDataSourceNotFoundException(collectionId, linkSourceId);
			}
		}
		
		return result;
	}
	
	
	/**
	 * Gets last created linkSet that belong to the specified collection and datasource.
	 * @param collectionId
	 * @param linkSourceId
	 * @return List of statements <<linkSet>> rdf:type lbdho:LinkSet
//	 * @throws NotFoundException if the datasource is not found
	 */
	public Resource getLastLinkSetResource(String collectionId, String linkSourceId)
//		throws NotFoundException
	{
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"SELECT \n" +
					"	?linkSetUri \n" +
					"WHERE { \n" + 
					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?linkSourceUri . \n" +
					"	?linkSourceUri a lbdho:LinkSource ; lbdho:hasDataSet ?linkSetUri ; lbdho:hasLastDataSet ?linkSetUri . \n" +
					"	?linkSetUri a lbdho:LinkSet . \n" +
					"} \n" + 
					"ORDER BY ?linkSetUri");
			
			DrumbeatOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
			setIri("linkSourceUri", formatDataSourceResourceUri(collectionId, linkSourceId));
		}}.asQuery();
		
		ResultSet result = 
				createQueryExecution(query, getMetaDataModel())
					.execSelect();
		
		if (!result.hasNext()) {
			return null;
//			LinkSourceManager linkSourceManager = new LinkSourceManager(getMetaDataModel(), getJenaProvider()); 
//			if (!linkSourceManager.checkExists(collectionId, linkSourceId)) {
//				throw ErrorFactory.createDataSourceNotFoundException(collectionId, linkSourceId);
//			}
		}
		
		return result.next().getResource("linkSetUri");
	}
	
	
	
	/**
	 * Gets all properties of a specified linkSet 
	 * @param collectionId
	 * @param linkSourceId
	 * @param linkSetId
	 * @return List of statements <<linkSet>> ?predicate ?object
	 * @throws NotFoundException if the linkSet is not found
	 */
	public Model getById(String collectionId, String linkSourceId, String linkSetId)
		throws NotFoundException
	{
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"CONSTRUCT { \n" +
					"	?linkSetUri ?predicate ?object \n" +
					"} WHERE { \n" + 
					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?linkSourceUri . \n" +
					"	?linkSourceUri a lbdho:LinkSource ; lbdho:hasDataSet ?linkSetUri . \n" +
					"	?linkSetUri a lbdho:LinkSet ; ?predicate ?object . \n" +
					"} \n" + 
					"ORDER BY ?predicate ?object");
			
			DrumbeatOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
			setIri("linkSourceUri", formatDataSourceResourceUri(collectionId, linkSourceId));
			setIri("linkSetUri", formatDataSetResourceUri(collectionId, linkSourceId, linkSetId));
		}}.asQuery();
		
		Model result = 
				createQueryExecution(query, getMetaDataModel())
					.execConstruct();
		
		if (result.isEmpty()) {
			throw ErrorFactory.createDataSetNotFoundException(collectionId, linkSourceId, linkSetId);
		}
		
		return result;
	}
	
	
	/**
	 * Creates a specified linkSet 
	 * @param collectionId
	 * @param linkSourceId
	 * @param linkSetId
	 * @param overwritingMethod 
	 * @return the recently created linkSet
	 * @throws AlreadyExistsException if the linkSet already exists
	 * @throws NotFoundException if the datasource is not found
	 */
	public Model create(String collectionId, String linkSourceId, String linkSetId, String name, String overwritingMethod)
		throws AlreadyExistsException, NotFoundException, IllegalArgumentException
	{
		if (!StringUtils.isEmptyOrNull(overwritingMethod)) {
			switch (overwritingMethod) {
			
			}
		}
		
		//
		// check if the parent linkSource exists
		//
		LinkSourceManager linkSourceManager = new LinkSourceManager(getMetaDataModel(), getJenaProvider()); 
		if (!linkSourceManager.checkExists(collectionId, linkSourceId)) {
			throw ErrorFactory.createDataSourceNotFoundException(collectionId, linkSourceId);
		}
		
		//
		// check if there is another dataset in this datasource with the same linkSetId 
		//
		if (checkExists(collectionId, linkSourceId, linkSetId)) {
			throw ErrorFactory.createDataSetAlreadyExistsException(collectionId, linkSourceId, linkSetId);
		}
		
		Model metaDataModel = getMetaDataModel();		

		Resource linkSourceResource = metaDataModel
				.createResource(formatDataSourceResourceUri(collectionId, linkSourceId));
		
		Resource linkSetResource = metaDataModel 
				.createResource(formatDataSetResourceUri(collectionId, linkSourceId, linkSetId));

		//
		// check if there is another dataset in this datasource
		//
		Resource lastLinkSetResource = getLastLinkSetResource(collectionId, linkSourceId);
		
		if (lastLinkSetResource != null) {
			lastLinkSetResource = lastLinkSetResource.inModel(metaDataModel);
			metaDataModel.remove(linkSourceResource, DrumbeatOntology.LBDHO.hasLastDataSet, lastLinkSetResource);
			linkSetResource
				.addProperty(DrumbeatOntology.LBDHO.overwrites, lastLinkSetResource)
				.addProperty(DrumbeatOntology.LBDHO.overwritingMethod, overwritingMethod);
		}
		
		linkSourceResource
			.addProperty(DrumbeatOntology.LBDHO.hasLastDataSet, linkSetResource)
			.addProperty(DrumbeatOntology.LBDHO.hasDataSet, linkSetResource);
	
		linkSetResource
			.addProperty(RDF.type, DrumbeatOntology.LBDHO.DataSet)
			.addProperty(RDF.type, DrumbeatOntology.LBDHO.LinkSet)
			.addLiteral(DrumbeatOntology.LBDHO.name, name)
			.addProperty(DrumbeatOntology.LBDHO.inDataSource, linkSourceResource);
		
		return ModelFactory
				.createDefaultModel()
				.add(linkSetResource, RDF.type, DrumbeatOntology.LBDHO.LinkSet);
	}
	
	
	/**
	 * Creates a specified linkSet 
	 * @param collectionId
	 * @param linkSourceId
	 * @param linkSetId
	 * @return the recently created linkSet
	 * @throws NotFoundException if the linkSet is not found
	 */
	public void delete(String collectionId, String linkSourceId, String linkSetId)
		throws NotFoundException
	{
		if (!checkExists(collectionId, linkSourceId, linkSetId)) {
			throw ErrorFactory.createDataSetNotFoundException(collectionId, linkSourceId, linkSetId);
		}
		
		UpdateRequest updateRequest1 = new ParameterizedSparqlString() {{
			setCommandText(
					"DELETE { ?linkSetUri ?p ?o } \n" +
					"WHERE { ?linkSetUri ?p ?o }");
			DrumbeatOntology.fillParameterizedSparqlString(this);
			setIri("linkSetUri", formatDataSetResourceUri(collectionId, linkSourceId, linkSetId));			
		}}.asUpdate();
		
		UpdateRequest updateRequest2 = new ParameterizedSparqlString() {{
			setCommandText(
					"DELETE { ?s ?p ?linkSetUri } \n" +
					"WHERE { ?s ?p ?linkSetUri }");
			DrumbeatOntology.fillParameterizedSparqlString(this);
			setIri("linkSetUri", formatDataSetResourceUri(collectionId, linkSourceId, linkSetId));			
		}}.asUpdate();

		UpdateAction.execute(updateRequest1, getMetaDataModel());
		UpdateAction.execute(updateRequest2, getMetaDataModel());		
	}
	
	
	/**
	 * Checks if the linkSet exists
	 * @param collectionId
	 * @param linkSourceId
	 * @param linkSetId
	 * @return true if the linkSet exists
	 */
	public boolean checkExists(String collectionId, String linkSourceId, String linkSetId) {
		
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"ASK { \n" + 
					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?linkSourceUri . \n" +
					"	?linkSourceUri a lbdho:LinkSource ; lbdho:hasDataSet ?linkSetUri . \n" +
					"	?linkSetUri a lbdho:LinkSet . \n" + 
					"}");			
			DrumbeatOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
			setIri("linkSourceUri", formatDataSourceResourceUri(collectionId, linkSourceId));
			setIri("linkSetUri", formatDataSetResourceUri(collectionId, linkSourceId, linkSetId));			
		}}.asQuery();
		
		boolean result = createQueryExecution(query, getMetaDataModel())
					.execAsk();
		
		return result;
	}
	
	
	/**
	 * Checks if the linkSource has children linkSets
	 * @param collectionId
	 * @param linkSourceId
	 * @return
	 */
	public boolean checkHasChildren(String collectionId, String linkSourceId, String linkSetId) {
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"ASK { \n" + 
					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?linkSourceUri . \n" +
					"	?linkSourceUri a lbdho:LinkSource ; lbdho:hasDataSet ?linkSetUri . \n" +
					"	?linkSetUri a lbdho:LinkSet . \n" + 
					"}");			
			DrumbeatOntology.fillParameterizedSparqlString(this);
			setIri("collectionUri", formatCollectionResourceUri(collectionId));
			setIri("linkSourceUri", formatDataSourceResourceUri(collectionId, linkSourceId));
			setIri("linkSetUri", formatDataSetResourceUri(collectionId, linkSourceId, linkSetId));			
		}}.asQuery();
		
		boolean result = 
				createQueryExecution(query, getMetaDataModel())
					.execAsk();
		
		return result;
	}

//	public Response createLinkSet(String collectionId, String linkSourceId, String linkSetId, String sourceUrl, String targetUrl) {
//		Query query = new ParameterizedSparqlString() {{
//			setCommandText(
//					"CONSTRUCT { \n" +
//					"	?sourceUri ifc:implements ?targetUri \n" +
//					"} WHERE { \n" + 
//					"	?collectionUri a lbdho:Collection ; lbdho:hasDataSource ?linkSourceUri . \n" +
//					"	?linkSourceUri a lbdho:LinkSource ; lbdho:hasDataSet ?linkSetUri . \n" +
//					"	?linkSetUri a lbdho:LinkSet ; ?predicate ?object . \n" +
//					"} \n" + 
//					"ORDER BY ?predicate ?object");
//			
//			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
//			setIri("collectionUri", formatCollectionResourceUri(collectionId));
//			setIri("linkSourceUri", formatDataSourceResourceUri(collectionId, linkSourceId));
//			setIri("linkSetUri", formatDataSetResourceUri(collectionId, linkSourceId, linkSetId));
//		}}.asQuery();
//		
//		Model result = 
//				createQueryExecution(query, getMetaDataModel())
//					.execConstruct();
//		
//		if (result.isEmpty()) {
//			throw ErrorFactory.createDataSetNotFoundException(collectionId, linkSourceId, linkSetId);
//		}
//		
//		return result;
//	}
//	
	

}
