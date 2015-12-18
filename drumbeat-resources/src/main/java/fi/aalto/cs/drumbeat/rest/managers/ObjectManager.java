package fi.aalto.cs.drumbeat.rest.managers;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.update.UpdateAction;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.ontology.LinkedBuildingDataOntology;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.Logger;

import fi.hut.cs.drumbeat.common.DrumbeatException;
import fi.hut.cs.drumbeat.common.config.ComplexProcessorConfiguration;
import fi.hut.cs.drumbeat.common.config.document.ConfigurationDocument;
import fi.hut.cs.drumbeat.common.file.FileManager;
import fi.hut.cs.drumbeat.common.string.StringUtils;
import fi.hut.cs.drumbeat.ifc.convert.ifc2ld.Ifc2RdfConversionContext;
import fi.hut.cs.drumbeat.ifc.convert.ifc2ld.Ifc2RdfModelExporter;
import fi.hut.cs.drumbeat.ifc.convert.ifc2ld.cli.Ifc2RdfExporter;
import fi.hut.cs.drumbeat.ifc.convert.stff2ifc.IfcModelParser;
import fi.hut.cs.drumbeat.ifc.convert.stff2ifc.IfcParserException;
import fi.hut.cs.drumbeat.ifc.data.model.IfcModel;
import fi.hut.cs.drumbeat.ifc.data.schema.IfcSchemaPool;
import fi.hut.cs.drumbeat.ifc.processing.IfcModelAnalyser;
import fi.hut.cs.drumbeat.rdf.modelfactory.JenaProvider;

import static fi.aalto.cs.drumbeat.rest.ontology.LinkedBuildingDataOntology.*;

public class ObjectManager extends DrumbeatManager {
	
	private static final Logger logger = Logger.getLogger(ObjectManager.class);
	
	public static final String DATA_TYPE_IFC = "IFC";
	public static final String DATA_TYPE_RDF = "RDF";
	public static final String DATA_TYPE_CSV = "CSV";
	
	public static final String COMPRESSION_FORMAT_GZIP = "gzip";	
	public static final String COMPRESSION_FORMAT_GZ = "gz";	

	public ObjectManager() throws DrumbeatException {
	}
	
	public ObjectManager(Model metaDataModel, JenaProvider jenaProvider) {
		super(metaDataModel, jenaProvider);
	}	

	public Model getDataModel(String collectionId, String dataSourceId, String dataSetId) throws DrumbeatException {
		String graphName = formatGraphUri(collectionId, dataSourceId, dataSetId);
		return DrumbeatApplication.getInstance().getDataModel(graphName);
	}
	
	/**
	 * Gets all attributes of a specified object 
	 * @param collectionId
	 * @param dataSourceId
	 * @param dataSetId
	 * @return List of statements <<dataSet>> ?predicate ?object
	 * @throws NotFoundException if the dataSet is not found
	 * @throws DrumbeatException 
	 */
	public Model getAll(String collectionId, String dataSourceId, String dataSetId)
		throws NotFoundException, DrumbeatException
	{
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
						"CONSTRUCT { \n" +
						"	?o rdf:type ?type \n" +
						"} \n " +
						"FROM NAMED <datasets/col-1/dso-1-1/dse-1-1-1> \n " +
						"FROM NAMED <owl/ifc2x3> \n " +
						"WHERE { \n " +
						"	GRAPH <datasets/col-1/dso-1-1/dse-1-1-1> { \n " +
						"		?o a ?type . \n " +
						"	} \n " +
						"	GRAPH <owl/ifc2x3> { \n " +
						"   	?type rdfs:subClassOf* ifc:IfcRoot . \n " +
						"	} \n " +
						"} \n "
					);
			
			fillParameterizedSparqlString(this);			
		}}.asQuery();
		
		Model resultModel = 
				createQueryExecution(query, getMetaDataModel())
					.execConstruct();
		
		if (resultModel.isEmpty()) {
			throw ErrorFactory.createObjectNotFoundException(collectionId, dataSourceId, dataSetId, "");
		}
		
		return resultModel;
	}
	
	
	/**
	 * Gets all attributes of a specified object 
	 * @param collectionId
	 * @param dataSourceId
	 * @param dataSetId
	 * @return List of statements <<dataSet>> ?predicate ?object
	 * @throws NotFoundException if the dataSet is not found
	 * @throws DrumbeatException 
	 */
	public Model getById(String collectionId, String dataSourceId, String dataSetId, String objectId)
		throws NotFoundException, DrumbeatException
	{
		Model dataModel = getDataModel(collectionId, dataSourceId, dataSetId);
		
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"CONSTRUCT { \n" +
					"	?objectUri ?predicate ?object \n" +
					"} WHERE { \n" + 
					"	?objectUri ?predicate ?object . \n" +
					"} \n" + 
					"ORDER BY ?predicate ?object");
			
			fillParameterizedSparqlString(this);
			setIri("objectUri", formatObjectResourceUri(collectionId, dataSourceId, dataSetId, objectId));
		}}.asQuery();
		
		Model resultModel = 
				createQueryExecution(query, dataModel)
					.execConstruct();
		
		if (resultModel.isEmpty()) {
			throw ErrorFactory.createObjectNotFoundException(collectionId, dataSourceId, dataSetId, objectId);
		}
		
		return resultModel;
	}
	
	
	/**
	 * Gets type of a specified object 
	 * @param collectionId
	 * @param dataSourceId
	 * @param dataSetId
	 * @return List of statements <<dataSet>> ?predicate ?object
	 * @throws NotFoundException if the dataSet is not found
	 * @throws DrumbeatException 
	 */
	public Model getObjectType(String collectionId, String dataSourceId, String dataSetId, String objectId)
		throws NotFoundException, DrumbeatException
	{
		Model dataModel = getDataModel(collectionId, dataSourceId, dataSetId);
		
		Query query = new ParameterizedSparqlString() {{
			setCommandText(
					"SELECT (?objectUri AS ?subject) (rdf:type AS ?predicate) (?type AS ?object) { \n" + 
					"	?objectUri a ?type . \n" +
					"} \n" + 
					"ORDER BY ?subject ?predicate ?object");
			
			fillParameterizedSparqlString(this);
			setIri("objectUri", formatObjectResourceUri(collectionId, dataSourceId, dataSetId, objectId));
		}}.asQuery();
		
		Model resultModel = 
				createQueryExecution(query, dataModel)
					.execConstruct();
		
		if (resultModel.isEmpty()) {
			throw ErrorFactory.createObjectNotFoundException(collectionId, dataSourceId, dataSetId, objectId);
		}
		
		return resultModel;
	}
	
//	/**
//	 * Gets type of a specified object 
//	 * @param collectionId
//	 * @param dataSourceId
//	 * @param dataSetId
//	 * @return List of statements <<dataSet>> ?predicate ?object
//	 * @throws NotFoundException if the dataSet is not found
//	 * @throws DrumbeatException 
//	 */
//	public Model getObjectProperty(String collectionId, String dataSourceId, String dataSetId, String objectId, String propertyName)
//		throws NotFoundException, DrumbeatException
//	{
//		Model dataModel = getDataModel(collectionId, dataSourceId, dataSetId);
//		
//		Query query = new ParameterizedSparqlString() {{
//			setCommandText(
//					"SELECT ?typeUri { \n" + 
//					"	?objectUri a ?typeUri . \n" +
//					"} \n" + 
//					"ORDER BY ?subject ?predicate ?object");
//			
//			fillParameterizedSparqlString(this);
//			setIri("objectUri", formatObjectResourceUri(collectionId, dataSourceId, dataSetId, objectId));
//		}}.asQuery();
//		
//		Model resultModel = 
//				createQueryExecution
//					.create(query, dataModel)
//					.execConstruct();
//		
//		if (resultModel.isEmpty()) {
//			throw ErrorFactory.createObjectNotFoundException(collectionId, dataSourceId, dataSetId, objectId);
//		}
//		
//		while (resultSet.hasNext()) {
//			Resource typeResource = resultSet.next().get("typeUri").asResource();
//			
//			String ifcModelName = formatGraphName("owl", "ifc", "ifc2x3");
//			Model ifcModel = DrumbeatApplication.getInstance().getDataModel(ifcModelName);
//			
//			
//		}
//		
//		 
//		
//		return resultModel;
//	}
//	
//	private Model internalGetObjectProperty(
//			String collectionId, String dataSourceId, String dataSetId, String objectId, String propertyName, Resource typeResource, Model ifcModel)
//	{
//		Query query = new ParameterizedSparqlString() {{
//			setCommandText(
//					"SELECT ?type { \n" + 
//					"	?objectUri a ?type . \n" +
//					"} \n" + 
//					"ORDER BY ?subject ?predicate ?object");
//			
//			fillParameterizedSparqlString(this);
//			setIri("objectUri", formatObjectResourceUri(collectionId, dataSourceId, dataSetId, objectId));
//		}}.asQuery();
//		
////		Model resultModel = 
////				createQueryExecution(query, dataModel)
////					.execConstruct();
////		
////		if (resultModel.isEmpty()) {
////			return null;
////		}
//		
//		ModelFactory.createOntologyModel(OntModelSpec., base)
//		
//	}
	
	
	
	/**
	 * Imports data set from an input stream
	 * @param collectionId
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
			String collectionId,
			String dataSourceId,
			String dataSetId,
			String dataType,
			String dataFormat,
			String compressionFormat,
			InputStream in,
			boolean saveToFiles)
		throws NotFoundException, IllegalArgumentException, Exception
	{
		//
		// Checking if dataSet exists
		//		
		DataSetManager dataSetManager = new DataSetManager();
		if (!dataSetManager.checkExists(collectionId, dataSourceId, dataSetId)) {
			throw ErrorFactory.createDataSetNotFoundException(collectionId, dataSourceId, dataSetId);
		}
		
		//
		// Format graphUri
		//		
		String dataSetUri = formatDataSetResourceUri(collectionId, dataSourceId, dataSetId);
		String graphUri = formatGraphUri(collectionId, dataSourceId, dataSetId);
		String graphBaseUri = formatObjectResourceUri(collectionId, dataSourceId, dataSetId, "");
		
		//
		// Save and uncompress input stream
		//
		in = processInputStream(graphUri, dataType, dataFormat, compressionFormat, in, saveToFiles);
		
		//
		// Read input stream to target model
		//
		Model targetModel = internalUpload(graphUri, graphBaseUri, dataType, dataFormat, in);
		
		//
		// Update meta data model
		//
		updateMetaModelAfterUploading(dataSetUri, graphUri, graphBaseUri, targetModel.size());		
		
		return dataSetManager.getById(collectionId, dataSourceId, dataSetId);
	}
	
	/**
	 * Uncompress input stream and save it to file (if required)
	 * @param graphUri
	 * @param dataType
	 * @param dataFormat
	 * @param compressionFormat
	 * @param in
	 * @param saveToFiles
	 * @return
	 * @throws IOException
	 */
	private InputStream processInputStream(
			String graphUri,
			String dataType,
			String dataFormat,
			String compressionFormat,
			InputStream in,
			boolean saveToFiles)
			throws IOException
	{
		//
		// Checking compression format
		//
		boolean inputStreamGzipped = false;		
		if (!StringUtils.isEmptyOrNull(compressionFormat)) {
			if (compressionFormat.equalsIgnoreCase(COMPRESSION_FORMAT_GZIP) || compressionFormat.equalsIgnoreCase(COMPRESSION_FORMAT_GZ)) {
				inputStreamGzipped = true;
			} else {
				throw ErrorFactory.createCompressionFormatNotFoundException(compressionFormat);
			}
		}
		
		
		
		//
		// Save to file (if needed)
		//
		if (saveToFiles) {
			in = saveToGzippedFile(graphUri, dataType, dataFormat, inputStreamGzipped, in);
			inputStreamGzipped = true;
		}
		

		//
		// Uncompress input stream (if needed)
		//
		if (inputStreamGzipped) {
			in = new GZIPInputStream(in);
		}
		
		return in;
	}
	
	private Model internalUpload(
			String graphUri,
			String graphBaseUri,
			String dataType,
			String dataFormat,
			InputStream in) throws Exception
	{
		//
		// Open target model and begin transactions (if supported)
		//
		Model targetModel = DrumbeatApplication.getInstance().getDataModel(graphUri);		

		//
		// Upload data to target model
		//		
		try {
			long oldSize = targetModel.size();		
			
			if (targetModel.supportsTransactions()) {
				targetModel.begin();
			}
			
			targetModel.removeAll();
		
			if (dataType.equalsIgnoreCase(DATA_TYPE_IFC)) {
				internalUploadIfc(in, graphBaseUri, targetModel);
			} else if (dataType.equalsIgnoreCase(DATA_TYPE_RDF)) {
				internalUploadRdf(in, dataFormat, graphBaseUri, targetModel);
			} else {
				throw new IllegalArgumentException(String.format("Unknown data type=%s", dataType));
			}
			
			long newSize = targetModel.size();
				
			if (targetModel.supportsTransactions()) {
				targetModel.commit();
			}
			
			logger.info(String.format("Uploaded data to graph '%s': oldSize=%d, newSize=%d", graphUri, oldSize, newSize));
			
		} catch (Exception e) {
			if (targetModel.supportsTransactions()) {
				targetModel.abort();
			}	
			
			logger.error(e);
			throw e;			
		}
		
		return targetModel;
	}
	
	private void internalUploadIfc(
			InputStream in,
			String graphBaseUri,
			Model targetModel) throws Exception 
	{
		logger.info("Uploading IFC model");
		try {			
			// loading schemas and config files
			synchronized (ObjectManager.class) {
				if (IfcSchemaPool.size() == 0) {
					ConfigurationDocument.load(DrumbeatApplication.getInstance().getRealPath(DrumbeatApplication.Paths.IFC2LD_CONFIG_FILE_PATH));				
					Ifc2RdfExporter.parseSchemas(DrumbeatApplication.getInstance().getRealPath(DrumbeatApplication.Paths.IFC_SCHEMA_FOLDER_PATH));
				}			
			}
			
			// parse model
			logger.debug("Parsing model");
			IfcModel ifcModel = IfcModelParser.parse(in);			

			// ground nodes in the model
			logger.debug("Grounding nodes");
			IfcModelAnalyser modelAnalyser = new IfcModelAnalyser(ifcModel);			
			ComplexProcessorConfiguration groundingConfiguration = IfcModelAnalyser.getDefaultGroundingRuleSets();		
			modelAnalyser.groundNodes(groundingConfiguration);

			// export model
			logger.debug("exporting model");
			Ifc2RdfConversionContext conversionContext = DrumbeatApplication.getInstance().getDefaultIfc2RdfConversionContext();
			conversionContext.setModelNamespaceUriFormat(graphBaseUri);
			
			Ifc2RdfModelExporter modelExporter = new Ifc2RdfModelExporter(ifcModel, conversionContext, targetModel);
			targetModel = modelExporter.export();
			logger.info("Uploading IFC model completed successfully");
			
		} catch (IfcParserException e) {
			logger.warn("Parsing IFC model failed", e);
			throw e;			
		} catch (Exception e) {
			logger.error("Uploading IFC model failed", e);
			throw e;			
		}		
	}	
	
	
	private void internalUploadRdf(
			InputStream in,
			String dataFormat,
			String graphBaseUri,
			Model targetModel) throws Exception
	{		
		logger.info("Uploading RDF model");
		
		Lang lang;
		
		try {
			lang = (Lang)Lang.class.getField(dataFormat).get(null);				
		} catch (Exception e) {
			throw ErrorFactory.createLangNotFoundException(dataFormat);
		}

		RDFDataMgr.read(targetModel, in, graphBaseUri, lang);
		
		logger.info("Uploading RDF model completed successfully");			
	}	

	private InputStream saveToGzippedFile(String graphUri, String dataType, String dataFormat, boolean inputStreamGzipped, InputStream in) throws IOException {
		
		String baseUri = DrumbeatApplication.getInstance().getBaseUri();
		String graphName = graphUri.startsWith(baseUri) ?
				graphUri.substring(baseUri.length()) : graphUri;
		
		String outputFilePath = String.format("%s/%s/%s/%sfile.gz",
				DrumbeatApplication.getInstance().getRealPath(DrumbeatApplication.Paths.UPLOADS_FOLDER_PATH),
				graphName,
				dataType.toUpperCase(),
				!StringUtils.isEmptyOrNull(dataFormat) ? dataFormat + "/" : "");
		
		logger.info("Saving data to file: " + outputFilePath);
		
		OutputStream out = FileManager.createFileOutputStream(outputFilePath);
		if (!inputStreamGzipped) {
			out = new GZIPOutputStream(out);
		}
		
		IOUtils.copy(in, out);
		in.close();
		out.close();
		
		return new FileInputStream(outputFilePath);
	}
	
	
	private void updateMetaModelAfterUploading(String dataSetUri, String graphUri, String graphBaseUri, long sizeInTriples) {
		
		Model metaDataModel = getMetaDataModel();

		try {
		
			if (metaDataModel.supportsTransactions()) {
				metaDataModel.begin();
			}		
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?dataSetUri lbdho:graphUri ?o } \n" +
								"WHERE { ?dataSetUri lbdho:graphUri ?o }");
						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
						setIri("dataSetUri", dataSetUri);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"INSERT DATA { ?dataSetUri lbdho:graphUri ?graphUri }");
						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
						setIri("dataSetUri", dataSetUri);
						setLiteral("graphUri", metaDataModel.createLiteral(graphUri));
					}}.asUpdate(),
					metaDataModel);
	
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?dataSetUri lbdho:graphBaseUri ?o } \n" +
								"WHERE { ?dataSetUri lbdho:graphBaseUri ?o }");
						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
						setIri("dataSetUri", dataSetUri);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"INSERT DATA { ?dataSetUri lbdho:graphBaseUri ?graphBaseUri }");
						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
						setIri("dataSetUri", dataSetUri);
						setLiteral("graphBaseUri", metaDataModel.createLiteral(graphBaseUri));
					}}.asUpdate(),
					metaDataModel);
	
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?dataSetUri lbdho:lastModified ?o } \n" +
								"WHERE { ?dataSetUri lbdho:lastModified ?o }");
						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
						setIri("dataSetUri", dataSetUri);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"INSERT DATA { ?dataSetUri lbdho:lastModified ?lastModified }");
						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
						setIri("dataSetUri", dataSetUri);
						setLiteral("lastModified", Calendar.getInstance());
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"DELETE { ?dataSetUri lbdho:sizeInTriples ?o } \n" +
								"WHERE { ?dataSetUri lbdho:sizeInTriples ?o }");
						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
						setIri("dataSetUri", dataSetUri);
					}}.asUpdate(),
					metaDataModel);
			
			UpdateAction.execute(
					new ParameterizedSparqlString() {{
						setCommandText(
								"INSERT DATA { ?dataSetUri lbdho:sizeInTriples ?sizeInTriples }");
						LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
						setIri("dataSetUri", dataSetUri);
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
