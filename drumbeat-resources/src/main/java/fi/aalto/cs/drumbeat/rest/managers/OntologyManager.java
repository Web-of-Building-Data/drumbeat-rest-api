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

import java.io.InputStream;
import java.util.Calendar;

import fi.aalto.cs.drumbeat.common.DrumbeatException;
import fi.aalto.cs.drumbeat.rdf.jena.provider.JenaProvider;

public class OntologyManager extends DrumbeatManager {
	
	public OntologyManager() throws DrumbeatException {
	}

	public OntologyManager(Model metaDataModel, JenaProvider jenaProvider) {
		super(metaDataModel, jenaProvider);
	}

	/**
	 * Gets all ontologys that belong to the specified ontology
	 * @return List of statements <<ontology>> rdf:type lbdho:Ontology
	 * @throws NotFoundException if the ontology is not found
	 */
	public Model getAll()
	{
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"CONSTRUCT { \n" +
					"	?ontologyUri rdf:type lbdho:Ontology \n" +
					"} WHERE { \n" + 
					"	?ontologyUri a lbdho:Ontology . \n" +
					"} \n" + 
					"ORDER BY ?ontologyUri");
			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
		}}.asQuery();
	
		Model result = 
				createQueryExecution(query, getMetaDataModel())
					.execConstruct();
		
		return result;
	}
	
	
	/**
	 * Gets all properties of a specified ontology 
	 * @param ontologyId
	 * @return List of statements <<ontology>> ?predicate ?object
	 * @throws NotFoundException if the ontology is not found
	 */
	public Model getById(String ontologyId)
		throws NotFoundException
	{
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"CONSTRUCT { \n" +
					"	?ontologyUri ?predicate ?object \n" +
					"} WHERE { \n" + 
					"	?ontologyUri a lbdho:Ontology ; ?predicate ?object . \n" +
					"} \n" + 
					"ORDER BY ?predicate ?object");
			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("ontologyUri", formatLocalOntologyUri(ontologyId));
		}}.asQuery();
		
		Model result =
				createQueryExecution(query, getMetaDataModel())
					.execConstruct();
		
		if (result.isEmpty()) {
			throw ErrorFactory.createOntologyNotFoundException(ontologyId);
		}
		
		return result;
	}
	
	
	/**
	 * Creates a specified ontology 
	 * @param ontologyId
	 * @param ontologyId
	 * @return the recently created ontology
	 * @throws AlreadyExistsException if the ontology already exists
	 */
	public Model create(String ontologyId, String name)
		throws AlreadyExistsException
	{
		if (checkExists(ontologyId)) {
			throw ErrorFactory.createOntologyAlreadyExistsException(ontologyId);
		}		
		
		Model metaDataModel = getMetaDataModel();		
		
		Resource ontologyResource = metaDataModel
				.createResource(formatLocalOntologyUri(ontologyId))
				.addProperty(RDF.type, LinkedBuildingDataOntology.Ontology)
				.addLiteral(LinkedBuildingDataOntology.name, name);
		
		return ModelFactory
				.createDefaultModel()
				.add(ontologyResource, RDF.type, LinkedBuildingDataOntology.Ontology);
	}
	
	
	/**
	 * Creates a specified ontology 
	 * @param ontologyId
	 * @param ontologyId
	 * @return the recently created ontology
	 * @throws NotFoundException if the ontology is not found
	 */
	public void delete(String ontologyId)
		throws NotFoundException, DeleteDeniedException
	{
		if (!checkExists(ontologyId)) {
			throw ErrorFactory.createOntologyNotFoundException(ontologyId);
		}
		
		UpdateRequest updateRequest1 = new ParameterizedSparqlString() {{
			setCommandText(
					"DELETE { ?ontologyUri ?p ?o } \n" +
					"WHERE { ?ontologyUri ?p ?o }");
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("ontologyUri", formatLocalOntologyUri(ontologyId));			
		}}.asUpdate();
		
		UpdateAction.execute(updateRequest1, getMetaDataModel());
	}
	
	
	/**
	 * Checks if the ontology exists
	 * @param ontologyId
	 * @param ontologyId
	 * @return true if the ontology exists
	 */
	public boolean checkExists(String ontologyId) {
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"ASK { \n" + 
					"	?ontologyUri a lbdho:Ontology . \n" + 
					"}");			
			LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
			setIri("ontologyUri", formatLocalOntologyUri(ontologyId));
		}}.asQuery();
		
		boolean result = 
				createQueryExecution(query, getMetaDataModel())
					.execAsk();
		
		return result;
	}
	
	
	/**
	 * Imports data set from an input stream
	 * @param ontologyId
	 * @param dataSourceId
	 * @param dataSetId
	 * @param dataType
	 * @param dataFormat
	 * @param in
	 * @param saveFiles
	 * @return
	 * @throws NotFoundException
	 */
	public Model upload(
			String ontologyId,
			String dataType,
			String dataFormat,
			String compressionFormat,
			boolean clearBefore,
			InputStream in,
			boolean saveToFiles)
		throws NotFoundException, IllegalArgumentException, Exception
	{
		//
		// Format graphUri
		//		
		String ontologyUri = formatLocalOntologyUri(ontologyId);
		String graphUri = ontologyUri;
		String graphBaseUri = formatLocalOntologyBaseUri(ontologyId);
		
		//
		// Read input stream to target model
		//
		Model targetModel = new UploadManager().upload(graphUri, graphBaseUri, dataType, dataFormat, compressionFormat, clearBefore, in, saveToFiles);
		
		//
		// Update meta data model
		//
		updateMetaModelAfterUploading(graphUri, graphUri, graphBaseUri, targetModel.size());		
		
		return getById(ontologyId);
	}
	
	
	private void updateMetaModelAfterUploading(String ontologyUri, String graphUri, String graphBaseUri, long sizeInTriples) {
		
		Model metaDataModel = getMetaDataModel();

		try {
		
			if (metaDataModel.supportsTransactions()) {
				metaDataModel.begin();
			}		
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?ontologyUri lbdho:graphUri ?o } \n" +
								"WHERE { ?ontologyUri lbdho:graphUri ?o }");
						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
						setIri("ontologyUri", ontologyUri);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"INSERT DATA { ?ontologyUri lbdho:graphUri ?graphUri }");
						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
						setIri("ontologyUri", ontologyUri);
						setLiteral("graphUri", metaDataModel.createLiteral(graphUri));
					}}.asUpdate(),
					metaDataModel);
	
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?ontologyUri lbdho:graphBaseUri ?o } \n" +
								"WHERE { ?ontologyUri lbdho:graphBaseUri ?o }");
						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
						setIri("ontologyUri", ontologyUri);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"INSERT DATA { ?ontologyUri lbdho:graphBaseUri ?graphBaseUri }");
						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
						setIri("ontologyUri", ontologyUri);
						setLiteral("graphBaseUri", metaDataModel.createLiteral(graphBaseUri));
					}}.asUpdate(),
					metaDataModel);
	
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?ontologyUri lbdho:lastModified ?o } \n" +
								"WHERE { ?ontologyUri lbdho:lastModified ?o }");
						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
						setIri("ontologyUri", ontologyUri);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"INSERT DATA { ?ontologyUri lbdho:lastModified ?lastModified }");
						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
						setIri("ontologyUri", ontologyUri);
						setLiteral("lastModified", Calendar.getInstance());
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?ontologyUri lbdho:sizeInTriples ?o } \n" +
								"WHERE { ?ontologyUri lbdho:sizeInTriples ?o }");
						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
						setIri("ontologyUri", ontologyUri);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"INSERT DATA { ?ontologyUri lbdho:sizeInTriples ?sizeInTriples }");
						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
						setIri("ontologyUri", ontologyUri);
						setLiteral("sizeInTriples", sizeInTriples);
					}}.asUpdate(),
					metaDataModel);
			
			if (metaDataModel.supportsTransactions()) {
				metaDataModel.commit();
			}
			
		} catch (Exception exception) {
			if (metaDataModel.supportsTransactions()) {
				metaDataModel.abort();
			}						
		}
		
	}
	
	
	
}
