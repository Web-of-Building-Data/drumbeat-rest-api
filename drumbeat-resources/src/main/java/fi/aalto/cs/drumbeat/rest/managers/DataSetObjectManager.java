package fi.aalto.cs.drumbeat.rest.managers;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
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

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import fi.hut.cs.drumbeat.common.DrumbeatException;
import fi.hut.cs.drumbeat.common.config.ComplexProcessorConfiguration;
import fi.hut.cs.drumbeat.common.config.document.ConfigurationDocument;
import fi.hut.cs.drumbeat.common.file.FileManager;
import fi.hut.cs.drumbeat.ifc.convert.ifc2ld.Ifc2RdfConversionContext;
import fi.hut.cs.drumbeat.ifc.convert.ifc2ld.Ifc2RdfModelExporter;
import fi.hut.cs.drumbeat.ifc.convert.ifc2ld.cli.Ifc2RdfExporter;
import fi.hut.cs.drumbeat.ifc.convert.stff2ifc.IfcModelParser;
import fi.hut.cs.drumbeat.ifc.convert.stff2ifc.IfcParserException;
import fi.hut.cs.drumbeat.ifc.data.model.IfcModel;
import fi.hut.cs.drumbeat.ifc.data.schema.IfcSchemaPool;
import fi.hut.cs.drumbeat.ifc.processing.IfcModelAnalyser;

import static fi.aalto.cs.drumbeat.rest.ontology.LinkedBuildingDataOntology.*;

public class DataSetObjectManager extends DrumbeatManager {
	
	private static final Logger logger = Logger.getLogger(DataSetObjectManager.class);
	
	public static final String DATA_TYPE_IFC = "IFC";
	public static final String DATA_TYPE_RDF = "RDF";
	public static final String DATA_TYPE_CSV = "CSV";
	

	public DataSetObjectManager() throws DrumbeatException {
		this(DrumbeatApplication.getInstance().getMetaDataModel());
	}

	public DataSetObjectManager(Model metaDataModel) {
		super(metaDataModel);
	}
	
	public Model getDataModel(String collectionId, String dataSourceId, String dataSetId) throws DrumbeatException {
		String graphName = formatGraphName(collectionId, dataSourceId, dataSetId);
		return DrumbeatApplication.getInstance().getDataModel(graphName);
	}
	
	/**
	 * Gets all properties of a specified dataSet 
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
					"SELECT (?objectUri AS ?subject) ?predicate ?object { \n" + 
					"	?objectUri ?predicate ?object . \n" +
					"} \n" + 
					"ORDER BY ?subject ?predicate ?object");
			
			fillParameterizedSparqlString(this);
			setIri("objectUri", formatObjectResourceUri(collectionId, dataSourceId, dataSetId, objectId));
		}}.asQuery();
		
		ResultSet resultSet = 
				QueryExecutionFactory
					.create(query, dataModel)
					.execSelect();
		
		if (!resultSet.hasNext()) {
			throw ErrorFactory.createObjectNotFoundException(collectionId, dataSourceId, dataSetId, objectId);
		}
		
		return convertResultSetToModel(resultSet);
	}
	
	private String getUploadFilePath(String graphName, String dataType, String dataFormat) {
		return String.format("%s/%s/%s.%s",
				DrumbeatApplication.getInstance().getRealPath(DrumbeatApplication.Paths.UPLOADS_FOLDER_PATH),
				dataType,
				graphName,
				dataFormat);
	}
	
	private InputStream saveToFile(String graphName, String dataType, String dataFormat, InputStream inputStream) throws IOException {
		String outputFilePath = getUploadFilePath(graphName, dataType, dataFormat); 
		OutputStream outputStream = FileManager.createFileOutputStream(outputFilePath);
		IOUtils.copy(inputStream, outputStream);
		inputStream.close();
		outputStream.close();
		
		return new FileInputStream(outputFilePath);
	}
	
	/**
	 * Imports data set from an input stream
	 * @param collectionId
	 * @param dataSourceId
	 * @param dataSetId
	 * @param dataType
	 * @param dataFormat
	 * @param inputStream
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
			InputStream inputStream,
			boolean saveToFiles)
		throws NotFoundException, IllegalArgumentException, Exception
	{
		
		DataSetManager dataSetManager = new DataSetManager();
		if (!dataSetManager.checkExists(collectionId, dataSourceId, dataSetId)) {
			throw ErrorFactory.createDataSetNotFoundException(collectionId, dataSourceId, dataSetId);
		}
		
		String graphName = LinkedBuildingDataOntology.formatGraphName(collectionId, dataSourceId, dataSetId);
		if (saveToFiles) {
			inputStream = saveToFile(graphName, dataType, dataFormat, inputStream);
		}

		Model targetDataSetModel = DrumbeatApplication.getInstance().getDataModel(graphName);
		String objectBaseUri = formatObjectResourceUri(collectionId, dataSourceId, dataSetId, "");			

		if (dataType.equalsIgnoreCase(DATA_TYPE_IFC)) {
			internalUploadIfc(inputStream, objectBaseUri, targetDataSetModel);
		} else if (dataType.equalsIgnoreCase(DATA_TYPE_RDF)) {
			internalUploadRdf(inputStream, dataFormat, objectBaseUri, targetDataSetModel);
		} else {
			throw new IllegalArgumentException(String.format("Unknown data type=%s", dataType));
		}
		
		Model metaDataModel = getMetaDataModel();
		
		if (metaDataModel.supportsTransactions()) {
			metaDataModel.begin();
		}		
		
		UpdateAction.execute(
				new ParameterizedSparqlString() {{
					setCommandText(
							"DELETE { ?dataSetUri lbdho:graphName ?o } \n" +
							"WHERE { ?dataSetUri lbdho:graphName ?o }");
					LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
					setIri("dataSetUri", formatDataSetResourceUri(collectionId, dataSourceId, dataSetId));
				}}.asUpdate(),
				metaDataModel);
		
		UpdateAction.execute(
				new ParameterizedSparqlString() {{
					setCommandText(
							"INSERT DATA { ?dataSetUri lbdho:graphName ?graphName }");
					LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
					setIri("dataSetUri", formatDataSetResourceUri(collectionId, dataSourceId, dataSetId));
					setLiteral("graphName", metaDataModel.createLiteral(graphName));
				}}.asUpdate(),
				metaDataModel);

		UpdateAction.execute(
				new ParameterizedSparqlString() {{
					setCommandText(
							"DELETE { ?dataSetUri lbdho:lastModified ?o } \n" +
							"WHERE { ?dataSetUri lbdho:lastModified ?o }");
					LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
					setIri("dataSetUri", formatDataSetResourceUri(collectionId, dataSourceId, dataSetId));
				}}.asUpdate(),
				metaDataModel);
		
		UpdateAction.execute(
				new ParameterizedSparqlString() {{
					setCommandText(
							"INSERT DATA { ?dataSetUri lbdho:lastModified ?lastModified }");
					LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
					setIri("dataSetUri", formatDataSetResourceUri(collectionId, dataSourceId, dataSetId));
					setLiteral("lastModified", Calendar.getInstance());
				}}.asUpdate(),
				metaDataModel);
		
		UpdateAction.execute(
				new ParameterizedSparqlString() {{
					setCommandText(
							"DELETE { ?dataSetUri lbdho:sizeInTriples ?o } \n" +
							"WHERE { ?dataSetUri lbdho:sizeInTriples ?o }");
					LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
					setIri("dataSetUri", formatDataSetResourceUri(collectionId, dataSourceId, dataSetId));
				}}.asUpdate(),
				metaDataModel);
		
		UpdateAction.execute(
				new ParameterizedSparqlString() {{
					setCommandText(
							"INSERT DATA { ?dataSetUri lbdho:sizeInTriples ?sizeInTriples }");
					LinkedBuildingDataOntology.fillParameterizedSparqlString(this);
					setIri("dataSetUri", formatDataSetResourceUri(collectionId, dataSourceId, dataSetId));
					setLiteral("sizeInTriples", targetDataSetModel.size());
				}}.asUpdate(),
				metaDataModel);
		
		if (metaDataModel.supportsTransactions()) {
			metaDataModel.commit();
		}		
		
		
//		new UpdateDeleteInsert().
//		
//		updateRequest1.add(updateRequest1);
//		
////		
//		metaDataModel
//			.removeAll(dataSetResource, LinkedBuildingDataOntology.graphName, null)
//			.removeAll(dataSetResource, LinkedBuildingDataOntology.sizeInTriples, null)
//			.removeAll(dataSetResource, LinkedBuildingDataOntology.lastModified, null)
//			.add(dataSetResource, LinkedBuildingDataOntology.graphName, graphName)
//			.add(dataSetResource, LinkedBuildingDataOntology.sizeInTriples, metaDataModel.createTypedLiteral(targetDataSetModel.size()))
//			.add(dataSetResource, LinkedBuildingDataOntology.lastModified, metaDataModel.createTypedLiteral(Calendar.getInstance()));
		
		return dataSetManager.getById(collectionId, dataSourceId, dataSetId);
	}
	
	private void internalUploadIfc(InputStream inputStream, String objectBaseUri, Model targetModel) throws Exception {
		logger.info("Uploading IFC model");
		try {			
			// loading schemas and config files
			synchronized (DataSetObjectManager.class) {
				if (IfcSchemaPool.size() == 0) {
					ConfigurationDocument.load(DrumbeatApplication.getInstance().getRealPath(DrumbeatApplication.Paths.IFC2LD_CONFIG_FILE_PATH));				
					Ifc2RdfExporter.parseSchemas(DrumbeatApplication.getInstance().getRealPath(DrumbeatApplication.Paths.IFC_SCHEMA_FOLDER_PATH));
				}			
			}
			
			// parse model
			logger.debug("Parsing model");
			IfcModel ifcModel = IfcModelParser.parse(inputStream);			

			// ground nodes in the model
			logger.debug("Grounding nodes");
			IfcModelAnalyser modelAnalyser = new IfcModelAnalyser(ifcModel);			
			ComplexProcessorConfiguration groundingConfiguration = IfcModelAnalyser.getDefaultGroundingRuleSets();		
			modelAnalyser.groundNodes(groundingConfiguration);

			// export model
			logger.debug("exporting model");
			Ifc2RdfConversionContext conversionContext = DrumbeatApplication.getInstance().getDefaultIfc2RdfConversionContext();
			conversionContext.setModelNamespaceUriFormat(objectBaseUri);
			
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
	
	
	private void internalUploadRdf(InputStream inputStream, String dataFormat, String objectBaseUri, Model targetModel) throws Exception
	{		
		logger.info("Uploading RDF model");
		try {			
			// export model
			targetModel.read(inputStream, objectBaseUri, dataFormat);
			
			logger.info("Uploading RDF model completed successfully");
			
		} catch (Exception e) {
			logger.error("Uploading RDF model failed", e);
			throw e;			
		}
	}	

	
	
	
}
