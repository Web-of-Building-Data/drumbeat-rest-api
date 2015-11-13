package fi.hut.cs.drumbeat.ldp.resources.managers;

import java.io.FileInputStream;
import java.util.Properties;


import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.apache.log4j.xml.DOMConfigurator;

import fi.hut.cs.drumbeat.rdf.modelfactory.AbstractJenaModelFactory;

public class AppManager {
	
	public static String WORKING_DIR = System.getProperty("user.dir");
	public static String WEB_INF_DIR = WORKING_DIR + "/WEB-INF";
	public static String CONFIG_DIR = WEB_INF_DIR + "/config";	

	public static String CONFIG_FILE_PATH = CONFIG_DIR + "/config.properties";
	public static String LOG4J_CONFIG_FILE_PATH = CONFIG_DIR + "/log4j.xml";

	public static String MODEL_FACTORY_PREFIX = "modelfactory.";
	
	
	@Context
	private ServletContext context;

	@PostConstruct
	public void init() {
		System.out.println("Hello world");
		DOMConfigurator.configure(LOG4J_CONFIG_FILE_PATH);
	}
	
	
	public static AbstractJenaModelFactory getModelFactory() throws Exception {
		Properties properties = new Properties();
		FileInputStream in = new FileInputStream(CONFIG_FILE_PATH);
		properties.load(in);
		String modelFactoryName = properties.getProperty(MODEL_FACTORY_PREFIX + AbstractJenaModelFactory.ARGUMENT_NAME);
		return AbstractJenaModelFactory.getFactory(modelFactoryName, properties, MODEL_FACTORY_PREFIX);
	}	

}
