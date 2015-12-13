package fi.aalto.cs.drumbeat.rest.managers;

import java.io.InputStream;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.hut.cs.drumbeat.common.config.ComplexProcessorConfiguration;
import fi.hut.cs.drumbeat.common.config.document.ConfigurationDocument;
import fi.hut.cs.drumbeat.ifc.convert.ifc2ld.Ifc2RdfConversionContext;
import fi.hut.cs.drumbeat.ifc.convert.ifc2ld.Ifc2RdfModelExporter;
import fi.hut.cs.drumbeat.ifc.convert.ifc2ld.cli.Ifc2RdfExporter;
import fi.hut.cs.drumbeat.ifc.convert.stff2ifc.IfcModelParser;
import fi.hut.cs.drumbeat.ifc.convert.stff2ifc.IfcParserException;
import fi.hut.cs.drumbeat.ifc.data.model.IfcModel;
import fi.hut.cs.drumbeat.ifc.processing.IfcModelAnalyser;

public class DataSetContentManager {
	
	private static final Logger logger = Logger.getLogger(DataSetContentManager.class);
	
	private static boolean ifcSchemaLoaded;
	
	public DataSetContentManager() {
	}

	
	public Model uploadIfcData(InputStream inputStream, Model jenaModel) throws Exception
	{		
		logger.info("Uploading IFC model");
		try {			
			// loading schemas and config files
			synchronized (DataSetContentManager.class) {
				if (!ifcSchemaLoaded) {
					ConfigurationDocument.load(DrumbeatApplication.getInstance().getRealPath(DrumbeatApplication.Paths.IFC2LD_CONFIG_FILE_PATH));				
					Ifc2RdfExporter.parseSchemas(DrumbeatApplication.getInstance().getRealPath(DrumbeatApplication.Paths.IFC_SCHEMA_FOLDER_PATH));
					ifcSchemaLoaded = true;
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
			conversionContext.setModelNamespaceUriFormat(DrumbeatApplication.getInstance().getBaseUri());
			
			Ifc2RdfModelExporter modelExporter = new Ifc2RdfModelExporter(ifcModel, conversionContext, jenaModel);
			jenaModel = modelExporter.export();
			logger.info("Uploading IFC model completed successfully");
			return jenaModel;
			
		} catch (IfcParserException e) {
			logger.warn("Parsing IFC model failed", e);
			throw e;			
		} catch (Exception e) {
			logger.error("Uploading IFC model failed", e);
			throw e;			
		}
	}
	
	
	public Model uploadRdfData(InputStream inputStream, Lang rdfLang, Model jenaModel) throws Exception
	{		
		logger.info("Uploading RDF model");
		try {			
			// export model
			logger.debug("exporting model");
			RDFDataMgr.read(jenaModel, inputStream, rdfLang);
			
			logger.info("Uploading RDF model completed successfully");
			return jenaModel;
			
		} catch (Exception e) {
			logger.error("Uploading RDF model failed", e);
			throw e;			
		}
	}	



}
