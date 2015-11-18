package fi.hut.cs.drumbeat.resources.managers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;




import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.apache.log4j.xml.DOMConfigurator;

import fi.hut.cs.drumbeat.rdf.modelfactory.AbstractJenaProvider;
import fi.hut.cs.drumbeat.rdf.modelfactory.JenaProviderException;

public class AppManager {
	
	//public static String WORKING_DIR = "C:/DRUM/!github/drumbeat/drumbeat-ldp/drumbeat-ldp.resources/WebContent";
	public static String WORKING_DIR = ".";
	public static String WEB_INF_DIR = WORKING_DIR + "/WEB-INF";
	public static String CONFIG_DIR = WEB_INF_DIR + "/config";	

	public static String CONFIG_FILE_PATH = CONFIG_DIR + "/config.properties";
	public static String LOG4J_CONFIG_FILE_PATH = CONFIG_DIR + "/log4j.xml";

	public static String JENA_PROVIDER_PREFIX = "modelfactory.";
	
	
	@Context
	private ServletContext context;

	@PostConstruct
	public void init() {
		System.out.println("Hello world");
		DOMConfigurator.configure(LOG4J_CONFIG_FILE_PATH);
	}
	
	
	public static AbstractJenaProvider getJenaProvider() throws JenaProviderException, IOException {
		Properties properties = new Properties();
		FileInputStream in = new FileInputStream(CONFIG_FILE_PATH);
		properties.load(in);
		String providerName = properties.getProperty(JENA_PROVIDER_PREFIX + AbstractJenaProvider.ARGUMENT_PROVIDER_NAME);
		String providerClassName = properties.getProperty(JENA_PROVIDER_PREFIX + AbstractJenaProvider.ARGUMENT_PROVIDER_CLASS);
		return AbstractJenaProvider.getFactory(providerName, providerClassName, properties, JENA_PROVIDER_PREFIX);
	}	

}
