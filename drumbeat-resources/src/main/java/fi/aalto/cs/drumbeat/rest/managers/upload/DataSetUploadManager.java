package fi.aalto.cs.drumbeat.rest.managers.upload;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.NotFoundException;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.log4j.Logger;

import fi.aalto.cs.drumbeat.common.config.ComplexProcessorConfiguration;
import fi.aalto.cs.drumbeat.common.config.document.ConfigurationDocument;
import fi.aalto.cs.drumbeat.common.file.FileManager;
import fi.aalto.cs.drumbeat.common.io.SerializedInputStream;
import fi.aalto.cs.drumbeat.common.string.StringUtils;
import fi.aalto.cs.drumbeat.ifc.convert.ifc2ld.Ifc2RdfConversionContext;
import fi.aalto.cs.drumbeat.ifc.convert.ifc2ld.Ifc2RdfModelExporter;
import fi.aalto.cs.drumbeat.ifc.convert.ifc2ld.cli.Ifc2RdfExporter;
import fi.aalto.cs.drumbeat.ifc.convert.stff2ifc.IfcModelParser;
import fi.aalto.cs.drumbeat.ifc.convert.stff2ifc.IfcParserException;
import fi.aalto.cs.drumbeat.ifc.data.model.IfcModel;
import fi.aalto.cs.drumbeat.ifc.data.schema.IfcSchemaPool;
import fi.aalto.cs.drumbeat.ifc.processing.IfcModelAnalyser;
import fi.aalto.cs.drumbeat.rdf.jena.provider.JenaProvider;

import static fi.aalto.cs.drumbeat.rest.common.DrumbeatVocabulary.*;



public class DataSetUploadManager {
	
	private static final Logger logger = Logger.getLogger(DataSetUploadManager.class);
	private static final Lang DEFAULT_RDF_LANG = Lang.NTRIPLES;
	
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
	public File upload(
			InputStream in,
			DataSetUploadOptions options)
	
		throws NotFoundException, IllegalArgumentException, Exception
	{
		if (StringUtils.isEmptyOrNull(options.getDataType())) {
			throw new IllegalAccessException("Undefined param 'dataType'");
		}
		
		File savedRdfFile;
		
		//
		// Open target model and begin transactions (if supported)
		//
		Model targetModel = DrumbeatApplication.getInstance().getDataModel(options.getDataSetGraphUri());		

		//
		// Upload data to target model
		//		
		try {
			long oldSize = targetModel.size();		
			
			if (options.isClearBefore()) {
				DrumbeatApplication.getInstance().getJenaProvider().deleteModel(options.getDataSetGraphUri());
//				targetModel.removeAll();
			}
		
			if (options.getDataType().equalsIgnoreCase(DATA_TYPE_IFC)) {
				savedRdfFile = internalUploadIfc(in, options);
			} else if (options.getDataType().equalsIgnoreCase(DATA_TYPE_RDF)) {
				savedRdfFile = internalUploadRdf(in, options, null);
			} else {
				throw new IllegalArgumentException(String.format("Unknown data type=%s", options.getDataType()));
			}
			
			long newSize = targetModel.size();
				
			logger.info(String.format("Uploaded data to graph '%s': oldSize=%d, newSize=%d", options.getDataSetGraphUri(), oldSize, newSize));
			
		} catch (Exception e) {
			logger.error(e);
			throw e;			
		}
		
		return savedRdfFile;
		
	}
	


	private File internalUploadIfc(InputStream in, DataSetUploadOptions options) throws Exception 
	{
		logger.info("Uploading IFC model");
		try {			
			// loading schemas and config files
			synchronized (DataSetUploadManager.class) {
				if (IfcSchemaPool.size() == 0) {
					ConfigurationDocument.load(DrumbeatApplication.getInstance().getRealServerPath(DrumbeatApplication.ResourcePaths.IFC2LD_CONFIG_FILE_PATH));				
					Ifc2RdfExporter.parseSchemas(DrumbeatApplication.getInstance().getRealServerPath(DrumbeatApplication.ResourcePaths.IFC_SCHEMA_FOLDER_PATH));
				}			
			}
			
			// parse model
			logger.debug("Parsing model");
			IfcModel ifcModel = IfcModelParser.parse(new SerializedInputStream(in, options.getDataFormat()));			

			// ground nodes in the model
			logger.debug("Grounding nodes");
			IfcModelAnalyser modelAnalyser = new IfcModelAnalyser(ifcModel);			
			ComplexProcessorConfiguration groundingConfiguration = IfcModelAnalyser.getDefaultGroundingRuleSets();		
			modelAnalyser.groundNodes(groundingConfiguration);

			// export model
			logger.debug("exporting model");
			Ifc2RdfConversionContext conversionContext = DrumbeatApplication.getInstance().getDefaultIfc2RdfConversionContext();
			conversionContext.setModelNamespaceUriFormat(options.getDataSourceObjectBaseUri());
			
			conversionContext.setModelBlankNodeNamespaceUriFormat(options.getDataSetBlankObjectBaseUri());
			
			Model targetModel = ModelFactory.createDefaultModel();
			Ifc2RdfModelExporter modelExporter = new Ifc2RdfModelExporter(ifcModel, conversionContext, targetModel);			
			targetModel = modelExporter.export();
			
			File savedRdfFile = internalUploadJenaModel(targetModel, options);
			
			logger.info("Uploading IFC model completed successfully");
			
			return savedRdfFile;
			
		} catch (IfcParserException e) {
			logger.warn("Parsing IFC model failed", e);
			throw e;			
		} catch (Exception e) {
			logger.error("Uploading IFC model failed", e);
			throw e;			
		} finally {
			in.close();
		}		
	}
	
	public File internalUploadJenaModel(Model model, DataSetUploadOptions options) throws Exception {
		
		File outputFile = saveModelToGzippedFile(model);
		options.setDataType(DATA_TYPE_RDF);
		String outputFileName = Paths.get(outputFile.getAbsolutePath()).getFileName().toString();
		options.setDataFormat(outputFileName);
		
		return internalUploadRdf(null, options, outputFile);
		
	}
	
	private File internalUploadRdf(InputStream in, DataSetUploadOptions options, File rdfCacheFile) throws Exception
	{		
		JenaProvider jenaProvider = DrumbeatApplication.getInstance().getJenaProvider();	
		boolean useBulkLoading = jenaProvider.supportsBulkLoading() && DrumbeatApplication.getInstance().isRdfBulkUploadEnabled();
		
		Lang rdfLang;

		try {
			
			if (rdfCacheFile == null) {
				
				logger.info("Caching RDF file");
				
				SerializedInputStream sis = SerializedInputStream.getUncompressedInputStream(in, options.getDataFormat());
				in = sis.getInputStream();
				String fileExtension = sis.getSerializationInfo();
				if (!fileExtension.contains(".")) {
					fileExtension = "." + fileExtension;
				}
				rdfLang = RDFLanguages.filenameToLang(fileExtension); 
				
				if (rdfLang == null) {
					throw new JenaException("Error retrieving RDF lang from file name: " + sis.getSerializationInfo());
				}

				if (options.isSaveToFiles() || useBulkLoading) {
					rdfCacheFile = saveInputStreamToGzippedFile(sis.getInputStream(), rdfLang);
					in = new GZIPInputStream(new FileInputStream(rdfCacheFile));
				}
				
			} else {
				
				in = new GZIPInputStream(new FileInputStream(rdfCacheFile));
				rdfLang = DEFAULT_RDF_LANG;				
				
			}
			
			if (!(in instanceof BufferedInputStream)) {
				in = new BufferedInputStream(in);
			}
		

			logger.info("Uploading RDF model");
			
			if (useBulkLoading) {
				
				boolean loaded = jenaProvider.bulkLoadFile(rdfCacheFile.getCanonicalPath(), options.getDataSetGraphUri());
				if (!loaded) {
					throw new JenaException("File is not loaded: " + rdfCacheFile.getCanonicalPath());
				}
				
			} else {
	
				Model targetModel = DrumbeatApplication.getInstance().getDataModel(options.getDataSetGraphUri());
				
				RDFDataMgr.read(targetModel, in, options.getDataSourceObjectBaseUri(), rdfLang);
		
			}
			
			logger.info("Uploading RDF model completed successfully");			
			
			
		} finally {
			in.close();
		}
		
		if (rdfCacheFile != null && !options.isSaveToFiles()) {
			rdfCacheFile.delete();
			return null;
		}
		
		return rdfCacheFile;
		
	}	
	

	private File saveInputStreamToGzippedFile(InputStream in, Lang lang) throws IOException {
		
		String fileName = UUID.randomUUID().toString();		
		String outputFilePath = String.format("%s/%s.%s.gz",
				DrumbeatApplication.getInstance().getUploadsDirPath(),
				fileName,
				lang.getFileExtensions().get(0));

		logger.info("Saving data to file: " + outputFilePath);
		
		File outputFile = FileManager.createFile(outputFilePath);		
		OutputStream out = new FileOutputStream(outputFile);
		out = new GZIPOutputStream(out);
		
		IOUtils.copy(in, out);
		in.close();
		out.close();
		
		return outputFile;
	}
	
	
	private File saveModelToGzippedFile(Model model) throws IOException {
		
		Lang lang = DEFAULT_RDF_LANG;

		String fileName = UUID.randomUUID().toString();		
		String outputFilePath = String.format("%s/%s.%s.gz",
				DrumbeatApplication.getInstance().getUploadsDirPath(),
				fileName,
				lang.getFileExtensions().get(0));

		logger.info("Saving data to file: " + outputFilePath);
		
		File outputFile = FileManager.createFile(outputFilePath);		
		OutputStream out = new FileOutputStream(outputFile);
		out = new GZIPOutputStream(out);		
		
		RDFDataMgr.write(out, model, lang);
		
		out.close();
		
		return outputFile;
	}
	
	
	public boolean deleteCachedRdfFile(String fileName) {
		File file = new File(DrumbeatApplication.getInstance().getUploadsDirPath(), fileName);
		return file.exists() && file.delete();
	}
	
	

}
