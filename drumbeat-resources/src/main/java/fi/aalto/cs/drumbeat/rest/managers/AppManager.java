package fi.aalto.cs.drumbeat.rest.managers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;

import fi.hut.cs.drumbeat.rdf.modelfactory.AbstractJenaProvider;
import fi.hut.cs.drumbeat.rdf.modelfactory.JenaProviderException;

public class AppManager {
	public static String BASE_URL = "http://localhost:8080/";
	public static final String BASE_URL_TAG = "baseUrl";
	
	public static final String WORKING_DIR = "/";
	public static final String WEB_INF_DIR = WORKING_DIR + "/WEB-INF";
	public static final String CONFIG_DIR = WEB_INF_DIR + "/config";	

	public static final String CONFIG_FILE_PATH = CONFIG_DIR + "/config.properties";
	public static final String LOG4J_CONFIG_FILE_PATH = CONFIG_DIR + "/log4j.xml";
	public static final String IFC2LD_CONFIG_FILE_PATH = CONFIG_DIR + "/ifc2ld.xml";

	public static final String JENA_PROVIDER_PREFIX = "jena.provider.";
	
	public static final String RESOURCES_DIR = WEB_INF_DIR + "/resources";
	public static final String IFC_SCHEMA_DIR = RESOURCES_DIR + "/ifc";	
		
	
	public static AbstractJenaProvider getJenaProvider(ServletContext servletContext) throws JenaProviderException, IOException {
		
		Properties properties = new Properties();
		String configFilePath = servletContext.getRealPath(CONFIG_FILE_PATH);
		FileInputStream in = new FileInputStream(configFilePath);
		properties.load(in);
		
		BASE_URL = properties.getProperty(JENA_PROVIDER_PREFIX + AppManager.BASE_URL_TAG);
		String providerName = properties.getProperty(JENA_PROVIDER_PREFIX + AbstractJenaProvider.ARGUMENT_PROVIDER_NAME);
		String providerClassName = properties.getProperty(JENA_PROVIDER_PREFIX + AbstractJenaProvider.ARGUMENT_PROVIDER_CLASS);
		return AbstractJenaProvider.getFactory(providerName, providerClassName, properties, JENA_PROVIDER_PREFIX);
		
	}	

}
