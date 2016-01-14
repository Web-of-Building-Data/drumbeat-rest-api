package fi.aalto.cs.drumbeat.rest.managers;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.NotFoundException;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.Logger;

import fi.aalto.cs.drumbeat.common.config.ComplexProcessorConfiguration;
import fi.aalto.cs.drumbeat.common.config.document.ConfigurationDocument;
import fi.aalto.cs.drumbeat.common.file.FileManager;
import fi.aalto.cs.drumbeat.common.string.StringUtils;
import fi.aalto.cs.drumbeat.ifc.convert.ifc2ld.Ifc2RdfConversionContext;
import fi.aalto.cs.drumbeat.ifc.convert.ifc2ld.Ifc2RdfModelExporter;
import fi.aalto.cs.drumbeat.ifc.convert.ifc2ld.cli.Ifc2RdfExporter;
import fi.aalto.cs.drumbeat.ifc.convert.stff2ifc.IfcModelParser;
import fi.aalto.cs.drumbeat.ifc.convert.stff2ifc.IfcParserException;
import fi.aalto.cs.drumbeat.ifc.data.model.IfcModel;
import fi.aalto.cs.drumbeat.ifc.data.schema.IfcSchemaPool;
import fi.aalto.cs.drumbeat.ifc.processing.IfcModelAnalyser;

import static fi.aalto.cs.drumbeat.rest.common.DrumbeatVocabulary.*;



public class UploadManager {
	
	private static final Logger logger = Logger.getLogger(DataSourceObjectManager.class);	


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
			String graphUri,
			String graphBaseUri,
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
		
		//
		// Save and uncompress input stream
		//
		in = processInputStream(graphUri, dataType, dataFormat, compressionFormat, in, saveToFiles);
		
		//
		// Read input stream to target model
		//
		Model targetModel = internalUpload(graphUri, graphBaseUri, dataType, dataFormat, clearBefore, in);		
		
		return targetModel;
	}
	

	/**
	 * Uncompress input stream and save it to file (if required)
	 * @param graphUri
	 * @param dataType
	 * @param dataFormat
	 * @param compressionFormat
	 * @param clearBefore
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
			boolean clearBefore,
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
			
			if (clearBefore) {
				targetModel.removeAll();
			}
		
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
			synchronized (DataSourceObjectManager.class) {
				if (IfcSchemaPool.size() == 0) {
					ConfigurationDocument.load(DrumbeatApplication.getInstance().getRealServerPath(DrumbeatApplication.ResourcePaths.IFC2LD_CONFIG_FILE_PATH));				
					Ifc2RdfExporter.parseSchemas(DrumbeatApplication.getInstance().getRealServerPath(DrumbeatApplication.ResourcePaths.IFC_SCHEMA_FOLDER_PATH));
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
			throw ErrorFactory.createRdfLangNotFoundException(dataFormat);
		}

		RDFDataMgr.read(targetModel, in, graphBaseUri, lang);
		
		logger.info("Uploading RDF model completed successfully");			
	}	

	private InputStream saveToGzippedFile(String graphUri, String dataType, String dataFormat, boolean inputStreamGzipped, InputStream in) throws IOException {
		
		String baseUri = DrumbeatApplication.getInstance().getBaseUri();
		String graphName = graphUri.startsWith(baseUri) ?
				graphUri.substring(baseUri.length()) : graphUri;
		
		String outputFilePath = String.format("%s/%s/%s/%sfile.gz",
				DrumbeatApplication.getInstance().getRealServerPath(DrumbeatApplication.ResourcePaths.UPLOADS_FOLDER_PATH),
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
	
	

}
