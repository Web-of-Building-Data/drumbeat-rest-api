package fi.aalto.cs.drumbeat.rest.api;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.glassfish.jersey.server.ResourceConfig;

import fi.hut.cs.drumbeat.rdf.modelfactory.AbstractJenaProvider;
import fi.hut.cs.drumbeat.rdf.modelfactory.JenaProviderException;

@ApplicationPath("/")
public class ApplicationConfig extends ResourceConfig {
	
	private static final Logger logger = Logger.getLogger(ApplicationConfig.class);
	
	public static class Paths {	
		public static final String WEB_INF_FOLDER_PATH = ApplicationConfig.class.getResource("/").getPath().replaceAll("classes/$", "");
		
		public static final String CONFIG_FOLDER_PATH = WEB_INF_FOLDER_PATH + "config/";
		public static final String COMMON_CONFIG_FILE_PATH = CONFIG_FOLDER_PATH + "config.properties";
		public static final String LOGGER_CONFIG_FILE_PATH = CONFIG_FOLDER_PATH + "log4j.xml";
		public static final String IFC2LD_CONFIG_FILE_PATH = CONFIG_FOLDER_PATH + "ifc2ld.xml";
		
		public static final String RESOURCES_FOLDER_PATH = WEB_INF_FOLDER_PATH + "resources/";
		public static final String IFC_SCHEMA_FOLDER_PATH = RESOURCES_FOLDER_PATH + "ifc/";
	}
	
	public static class Params {
		public static final String WEB_API_BASE_URL = "web.api.baseUrl";		
		public static final String JENA_PROVIDER_PREFIX = "jena.provider.";		
	}

	private static Properties configurationProperties;

	public ApplicationConfig() throws IOException {
		
		DOMConfigurator.configure(Paths.LOGGER_CONFIG_FILE_PATH);				
		logger.info("Starting Web API");

		synchronized (ApplicationConfig.class) {
			if (configurationProperties == null) {				
				configurationProperties = new Properties();
				FileInputStream in = new FileInputStream(Paths.COMMON_CONFIG_FILE_PATH);
				configurationProperties.load(in);
				
			}			
		}		
		
		logger.info("BaseUrl: " + getBaseUrl());
		logger.info("Web API started");
	}
	
	public static Properties getConfigurationProperties() {
		return configurationProperties;
	}
	
	public static String getBaseUrl() {
		return getConfigurationProperties().getProperty(Params.WEB_API_BASE_URL);
	}
	
	public static void setBaseUrl(@Context HttpServletRequest httpRequest) {
		try
		{
		  URL url= new URL(httpRequest.getRequestURI());
		  getConfigurationProperties().setProperty(Params.WEB_API_BASE_URL,url.getProtocol()+url.getAuthority()+"/");
		}
		 catch (Exception e) {
			 // Nothing bad happends
		  ;
		}

		
	}
	
	public static AbstractJenaProvider getJenaProvider() throws JenaProviderException, IOException {
		
		String providerName = getConfigurationProperties().getProperty(Params.JENA_PROVIDER_PREFIX + AbstractJenaProvider.ARGUMENT_PROVIDER_NAME);
		String providerClassName = getConfigurationProperties().getProperty(Params.JENA_PROVIDER_PREFIX + AbstractJenaProvider.ARGUMENT_PROVIDER_CLASS);
		return AbstractJenaProvider.getFactory(providerName, providerClassName, getConfigurationProperties(), Params.JENA_PROVIDER_PREFIX);
		
	}	
	

}
