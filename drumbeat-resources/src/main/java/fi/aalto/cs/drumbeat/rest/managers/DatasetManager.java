package fi.aalto.cs.drumbeat.rest.managers;

import static fi.aalto.cs.drumbeat.rest.managers.AppManager.IFC2LD_CONFIG_FILE_PATH;
import static fi.aalto.cs.drumbeat.rest.managers.AppManager.IFC_SCHEMA_DIR;
import static fi.aalto.cs.drumbeat.rest.managers.AppManager.LOG4J_CONFIG_FILE_PATH;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import fi.aalto.cs.drumbeat.rest.ontology.BuildingDataOntology;
import fi.hut.cs.drumbeat.common.config.ComplexProcessorConfiguration;
import fi.hut.cs.drumbeat.common.config.document.ConfigurationParserException;
import fi.hut.cs.drumbeat.ifc.common.IfcException;
import fi.hut.cs.drumbeat.ifc.convert.ifc2ld.cli.Ifc2RdfExporter;
import fi.hut.cs.drumbeat.ifc.convert.ifc2ld.util.Ifc2RdfExportUtil;
import fi.hut.cs.drumbeat.ifc.convert.stff2ifc.IfcModelParser;
import fi.hut.cs.drumbeat.ifc.convert.stff2ifc.IfcParserException;
import fi.hut.cs.drumbeat.ifc.data.model.IfcModel;
import fi.hut.cs.drumbeat.ifc.processing.IfcModelAnalyser;

public class DataSetManager {
	
	private final Model model;	

	private static boolean ifcSchemaLoaded; 
	
	public DataSetManager(Model model) {
		this.model = model;
	}
	
	
	public ResultSet listAll() {
		
		final QueryExecution queryExecution = 
				QueryExecutionFactory.create(
						QueryFactory.create(
								String.format("SELECT ?dataset ?name WHERE { ?dataset a <%s>; <%s> ?name . } ",
										BuildingDataOntology.Collections.Collection,
										BuildingDataOntology.Collections.name)),
						model);
		
		return queryExecution.execSelect();
		
	}
	
	public Resource get(String name) {
		return null;
	}
	
	
	public Resource create(String name) {
		
		
		return null;
		
	}
	
	
	public void importData(ServletContext servletContext, InputStream inputStream, Model jenaModel) throws Exception
	{		
		synchronized (DataSetManager.class) {
			if (!ifcSchemaLoaded) {				
				Ifc2RdfExporter.init(servletContext.getRealPath(LOG4J_CONFIG_FILE_PATH), servletContext.getRealPath(IFC2LD_CONFIG_FILE_PATH));				
				String schemaDirPath = servletContext.getRealPath(IFC_SCHEMA_DIR);
				Ifc2RdfExporter.parseSchemas(schemaDirPath);
			}			
		}
		
		IfcModel ifcModel = IfcModelParser.parse(inputStream);

		ComplexProcessorConfiguration groundingConfiguration = IfcModelAnalyser.getDefaultGroundingRuleSets();
		
		// ground nodes in the model
		IfcModelAnalyser modelAnalyser = new IfcModelAnalyser(ifcModel);			
		modelAnalyser.groundNodes(groundingConfiguration);
		
		Ifc2RdfExportUtil.exportModelToJenaModel(jenaModel, ifcModel);
	}	
	
	

}
