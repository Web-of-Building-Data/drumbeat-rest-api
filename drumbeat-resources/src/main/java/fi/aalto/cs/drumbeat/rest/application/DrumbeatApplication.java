package fi.aalto.cs.drumbeat.rest.application;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.hp.hpl.jena.rdf.model.Model;

import fi.aalto.cs.drumbeat.rest.api.CollectionResource;
import fi.hut.cs.drumbeat.common.config.document.ConfigurationParserException;
import fi.hut.cs.drumbeat.common.params.BooleanParam;
import fi.hut.cs.drumbeat.ifc.convert.ifc2ld.Ifc2RdfConversionContext;
import fi.hut.cs.drumbeat.ifc.convert.ifc2ld.config.Ifc2RdfConversionContextLoader;
import fi.hut.cs.drumbeat.rdf.modelfactory.AbstractJenaProvider;
import fi.hut.cs.drumbeat.rdf.modelfactory.JenaProviderException;

@ApplicationPath("/")
public abstract class DrumbeatApplication extends ResourceConfig {
	
	public static class Paths {	
		public static final String CONFIG_FOLDER_PATH = "config/";
		public static final String COMMON_CONFIG_FILE_PATH = CONFIG_FOLDER_PATH + "config.properties";
		public static final String LOGGER_CONFIG_FILE_PATH = CONFIG_FOLDER_PATH + "log4j.xml";
		public static final String IFC2LD_CONFIG_FILE_PATH = CONFIG_FOLDER_PATH + "ifc2ld.xml";
		
		public static final String RESOURCES_FOLDER_PATH = "resources/";
		public static final String IFC_SCHEMA_FOLDER_PATH = RESOURCES_FOLDER_PATH + "ifc/";
		public static final String UPLOADS_FOLDER_PATH = "uploads/";
	}
	
	public static class Params {
		public static final String WEB_BASE_URI = "web.baseUri";		
		public static final String JENA_PROVIDER_PREFIX = "jena.provider.";
		public static final String UPLOADS_SAVE = "uploads.save";
	}
	
	public static class Resources {
		public static final String PACKAGE_RESOURCES = CollectionResource.class.getPackage().getName();
	}
	
	
	private static DrumbeatApplication instance;

	public static DrumbeatApplication getInstance() {
		return instance;
	}	
	
	private static Logger logger;
	private static AbstractJenaProvider jenaProvider;
	private static int nextApplicationId = 0;
	
	private final int applicationId;
	private Properties configurationProperties;
	private Boolean saveUploads;
	private Ifc2RdfConversionContext defaultConversionContext;
	private String workingFolderPath;

	protected DrumbeatApplication(String workingFolderPath) {
		
		applicationId = nextApplicationId++;
		
		instance = this;
		
		packages(Resources.PACKAGE_RESOURCES);
		register(MultiPartFeature.class);
		
		this.workingFolderPath = workingFolderPath;
		
		synchronized (DrumbeatApplication.class) {
			if (logger == null) {
				logger = Logger.getRootLogger();
				DOMConfigurator.configure(getRealPath(Paths.LOGGER_CONFIG_FILE_PATH));
				logger.info("Starting Web API");
			}
			
			if (configurationProperties == null) {
				configurationProperties = new Properties();
				try {
					String configFilePath = getRealPath(Paths.COMMON_CONFIG_FILE_PATH);
					logger.info("Config file: " + configFilePath);
					FileInputStream in = new FileInputStream(configFilePath);
					configurationProperties.load(in);
				} catch (IOException e) {
					throw new RuntimeException("Loading config file failed: " + e.getMessage(), e);
				}
				
			}			
		}		
		
		logger.info("ApplicationId: " + applicationId);
		logger.info("BaseUrl: " + getBaseUri());
		logger.info("Web API started");
	}
	
	public int getApplicationId() {
		return applicationId;
	}
	
	public Properties getConfigurationProperties() {
		return configurationProperties;
	}
	
	public String getBaseUri() {
		return getConfigurationProperties().getProperty(Params.WEB_BASE_URI);
	}
	
	public void setBaseUrl(@Context HttpServletRequest httpRequest) {
		try
		{
		  String url = "http://" + httpRequest.getLocalAddr() + ":" + httpRequest.getLocalPort() + "/";
		  getConfigurationProperties().setProperty(
				  Params.WEB_BASE_URI,
				  url);
		}
		 catch (Exception e) {
			 // Nothing bad happends
		}		
	}
	
	/**
	 * Gets value indicating whether uploaded files must be saved
	 * @return
	 */
	public boolean getSaveUploads() {
		if (saveUploads == null) {
			BooleanParam param = new BooleanParam();
			param.setStringValue(getConfigurationProperties().getProperty(Params.UPLOADS_SAVE, "false"));
			saveUploads = param.getValue();
		}
		return saveUploads;
	}
	
	
	
	public String getBaseUri(String path) {
		return getConfigurationProperties().getProperty(Params.WEB_BASE_URI) + path;
	}
	
	public AbstractJenaProvider getJenaProvider() throws JenaProviderException, IOException {
		
		if (jenaProvider == null) {		
			String providerName = getConfigurationProperties().getProperty(Params.JENA_PROVIDER_PREFIX + AbstractJenaProvider.ARGUMENT_PROVIDER_NAME);
			String providerClassName = getConfigurationProperties().getProperty(Params.JENA_PROVIDER_PREFIX + AbstractJenaProvider.ARGUMENT_PROVIDER_CLASS);
			jenaProvider = AbstractJenaProvider.getFactory(providerName, providerClassName, getConfigurationProperties(), Params.JENA_PROVIDER_PREFIX);
		}
		return jenaProvider;
		
	}
	
	public Model getMainModel() throws JenaProviderException, IOException {
		return getJenaProvider().openDefaultModel();
	}
	
	public String getRealPath(String path) {
		return workingFolderPath + path;
	}
	
	/**
	 * Gets the default IFC-to-RDF conversion context loaded from the configuration file.
	 * 
	 * @return the default {@link Ifc2RdfConversionContext} object
	 *  
	 * @throws ConfigurationParserException
	 */
	public Ifc2RdfConversionContext getDefaultIfc2RdfConversionContext() throws ConfigurationParserException {
		if (defaultConversionContext == null) {
			defaultConversionContext = Ifc2RdfConversionContextLoader.loadFromDefaultConfigurationFile(null); 
		}
		return defaultConversionContext;
	}
	

}
