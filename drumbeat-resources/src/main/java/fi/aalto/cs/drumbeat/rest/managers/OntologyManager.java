package fi.aalto.cs.drumbeat.rest.managers;

import static fi.aalto.cs.drumbeat.rest.common.NameFormatter.*;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.AlreadyExistsException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.NotFoundException;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.RDF;

import java.io.File;
import java.io.InputStream;
import java.util.Calendar;

import fi.aalto.cs.drumbeat.common.DrumbeatException;
import fi.aalto.cs.drumbeat.rdf.jena.provider.JenaProvider;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatOntology;
import fi.aalto.cs.drumbeat.rest.managers.upload.DataSetUploadManager;
import fi.aalto.cs.drumbeat.rest.managers.upload.DataSetUploadOptions;

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
			
			DrumbeatOntology.fillParameterizedSparqlString(this);
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
			
			DrumbeatOntology.fillParameterizedSparqlString(this);
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
				.addProperty(RDF.type, DrumbeatOntology.LBDHO.Ontology)
				.addLiteral(DrumbeatOntology.LBDHO.name, name);
		
		return ModelFactory
				.createDefaultModel()
				.add(ontologyResource, RDF.type, DrumbeatOntology.LBDHO.Ontology);
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
			DrumbeatOntology.fillParameterizedSparqlString(this);
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
			DrumbeatOntology.fillParameterizedSparqlString(this);
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
		
		deleteCachedRdfFile(ontologyId);
		
		//
		// Read input stream to target model
		//
		DataSetUploadOptions options = new DataSetUploadOptions(
				ontologyId,
				null,
				null,
				graphUri,
				graphUri,
				graphBaseUri,
				dataType,
				dataFormat,
				clearBefore,
				saveToFiles);		
		File savedRdfFile = new DataSetUploadManager().upload(in, options);
		Model targetModel = DrumbeatApplication.getInstance().getDataModel(graphUri);
		
		//
		// Update meta data model
		//
		updateMetaModelAfterUploading(graphUri, graphUri, graphBaseUri, targetModel.size(), savedRdfFile);		
		
		return getById(ontologyId);
	}
	
	
	private void updateMetaModelAfterUploading(String ontologyUri, String graphUri, String graphBaseUri, long sizeInTriples, File savedRdfFile) {
		
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
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("ontologyUri", ontologyUri);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"INSERT DATA { ?ontologyUri lbdho:graphUri ?graphUri }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("ontologyUri", ontologyUri);
						setLiteral("graphUri", metaDataModel.createLiteral(graphUri));
					}}.asUpdate(),
					metaDataModel);
	
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?ontologyUri lbdho:graphBaseUri ?o } \n" +
								"WHERE { ?ontologyUri lbdho:graphBaseUri ?o }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("ontologyUri", ontologyUri);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"INSERT DATA { ?ontologyUri lbdho:graphBaseUri ?graphBaseUri }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("ontologyUri", ontologyUri);
						setLiteral("graphBaseUri", metaDataModel.createLiteral(graphBaseUri));
					}}.asUpdate(),
					metaDataModel);
	
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?ontologyUri lbdho:lastModified ?o } \n" +
								"WHERE { ?ontologyUri lbdho:lastModified ?o }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("ontologyUri", ontologyUri);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"INSERT DATA { ?ontologyUri lbdho:lastModified ?lastModified }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("ontologyUri", ontologyUri);
						setLiteral("lastModified", Calendar.getInstance());
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?ontologyUri lbdho:sizeInTriples ?o } \n" +
								"WHERE { ?ontologyUri lbdho:sizeInTriples ?o }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("ontologyUri", ontologyUri);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"INSERT DATA { ?ontologyUri lbdho:sizeInTriples ?sizeInTriples }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("ontologyUri", ontologyUri);
						setLiteral("sizeInTriples", sizeInTriples);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?ontologyUri lbdho:cachedInRdfFile ?o } \n" +
								"WHERE { ?ontologyUri lbdho:cachedInRdfFile ?o }");
						DrumbeatOntology.fillParameterizedSparqlString(this);
						setIri("ontologyUri", ontologyUri);
					}}.asUpdate(),
					metaDataModel);
			
			if (savedRdfFile != null) {
				UpdateAction.execute(
						new ParameterizedSparqlString() {{
							setCommandText(
									"INSERT DATA { ?ontologyUri lbdho:cachedInRdfFile ?cachedInRdfFile }");
							DrumbeatOntology.fillParameterizedSparqlString(this);
							setIri("ontologyUri", ontologyUri);
							setLiteral("cachedInRdfFile", savedRdfFile.getName());
						}}.asUpdate(),
						metaDataModel);
			}
			
			
			if (metaDataModel.supportsTransactions()) {
				metaDataModel.commit();
			}
			
		} catch (Exception exception) {
			if (metaDataModel.supportsTransactions()) {
				metaDataModel.abort();
			}						
		}
		
	}
	
	
	public Model getCachedRdfFile(String ontologyId) {
		
		String ontologyUri = formatLocalOntologyUri(ontologyId);		
		
		Query query =
				new ParameterizedSparqlString() {{
					setCommandText(
							"CONSTRUCT { \n" +
									"	?ontologyUri lbdho:cachedInRdfFile ?cachedInRdfFile \n" +
									"} WHERE { \n" + 
									"	?ontologyUri lbdho:cachedInRdfFile ?cachedInRdfFile . \n" +
									"} \n");							
					DrumbeatOntology.fillParameterizedSparqlString(this);
					setIri("ontologyUri", ontologyUri);
				}}.asQuery();
		
		Model result =
				createQueryExecution(query, getMetaDataModel())
					.execConstruct();
		
		return result;
	}
	
	
	public void deleteCachedRdfFile(String ontologyId) {
		Model cachedRdfFile = getCachedRdfFile(ontologyId);
		NodeIterator it = cachedRdfFile.listObjects();
		while (it.hasNext()) {
			String fileName = it.next().asLiteral().getString();
			new DataSetUploadManager().deleteCachedRdfFile(fileName);
		}
		
		String ontologyUri = formatLocalOntologyUri(ontologyId);		
		UpdateAction.execute(
				new ParameterizedSparqlString() {{
					setCommandText(
							"DELETE { ?ontologyUri lbdho:cachedInRdfFile ?o } \n" +
							"WHERE { ?ontologyUri lbdho:cachedInRdfFile ?o }");
					DrumbeatOntology.fillParameterizedSparqlString(this);
					setIri("ontologyUri", ontologyUri);
				}}.asUpdate(),
				getMetaDataModel());
		
	}
	
	
	
}
