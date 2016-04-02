package fi.aalto.cs.drumbeat.rest.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import fi.aalto.cs.drumbeat.rest.api.DataSetResource;
import fi.aalto.cs.drumbeat.common.DrumbeatException;
import fi.aalto.cs.drumbeat.common.config.document.ConfigurationParserException;
import fi.aalto.cs.drumbeat.common.params.BooleanParam;
import fi.aalto.cs.drumbeat.ifc.convert.ifc2ld.Ifc2RdfConversionContext;
import fi.aalto.cs.drumbeat.ifc.convert.ifc2ld.config.Ifc2RdfConversionContextLoader;
import fi.aalto.cs.drumbeat.rdf.jena.provider.AbstractJenaProvider;
import fi.aalto.cs.drumbeat.rdf.jena.provider.JenaProvider;
import fi.aalto.cs.drumbeat.rdf.jena.provider.JenaProviderException;

public abstract class DrumbeatApplication extends ResourceConfig {
	
	public static class RequestParams {
		public static final String NONE = "NONE";
	}
	
	public static class SystemEnvironment {
		public static final String DRUMBEAT_SHARE_FOLDER = "DRUMBEAT_SHARE_FOLDER";
	}
	
	public static class ResourcePaths {	
		public static final String CONFIG_FOLDER_PATH = "config/";
		public static final String COMMON_CONFIG_FILE_PATH = CONFIG_FOLDER_PATH + "config.properties";
		public static final String LOGGER_CONFIG_FILE_PATH = CONFIG_FOLDER_PATH + "log4j.xml";
		public static final String IFC2LD_CONFIG_FILE_PATH = CONFIG_FOLDER_PATH + "ifc2ld.xml";
		
		public static final String RESOURCES_FOLDER_PATH = "resources/";
		public static final String IFC_SCHEMA_FOLDER_PATH = RESOURCES_FOLDER_PATH + "ifc/";
		public static final String LINKS_FOLDER_PATH = RESOURCES_FOLDER_PATH + "links/";
		public static final String UPLOADS_FOLDER_PATH = "uploads/";
	}
	
	public static class ConfigParams {
		public static final String WEB_BASE_URI = "web.baseUri";		
		public static final String WEB_BASE_URI_FIXED = "web.baseUri.fixed";		
		
		public static final String JENA_PROVIDER_PREFIX = "jena.provider.";

		public static final String UPLOADS_SAVE_ENALBED = "uploads.save.enabled";
		public static final String UPLOADS_RDF_BULK_ENALBED = "uploads.rdf.bulk.enabled";
		public static final String UPLOADS_DIR_PATH = "uploads.dir.path";
	}
	
	public static class Resources {
		public static final String PACKAGE_RESOURCES = DataSetResource.class.getPackage().getName();
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
	private String baseUri;
	private String realBaseUri;
	private Boolean isBaseUriFixed;
	private Boolean isSavingUploadEnabled;
	private Boolean isRdfBulkUploadEnabled;
	private String uploadsDirPath;
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
				DOMConfigurator.configure(getRealServerPath(ResourcePaths.LOGGER_CONFIG_FILE_PATH));
				logger.info("Starting Web API");
			}
			
			if (configurationProperties == null) {
				configurationProperties = new Properties();
				try {
					String configFilePath = getRealServerPath(ResourcePaths.COMMON_CONFIG_FILE_PATH);
					logger.info("Config file: " + configFilePath);
					FileInputStream in = new FileInputStream(configFilePath);
					configurationProperties.load(in);
				} catch (IOException e) {
					throw new RuntimeException("Loading config file failed: " + e.getMessage(), e);
				}
				
			}			
		}		
		
		logger.info("ApplicationId: " + applicationId);
		logger.info("BaseUri: " + getBaseUri());
		logger.info("Web API started");
	}
	
	public int getApplicationId() {
		return applicationId;
	}
	
	public Properties getConfigurationProperties() {
		return configurationProperties;
	}
	
	public String getBaseUri() {
		if (baseUri == null) {
			baseUri = getConfigurationProperties().getProperty(ConfigParams.WEB_BASE_URI).trim(); 
		}
		return baseUri;
	}
	
	public boolean isBaseUriFixed() {
		if (isBaseUriFixed == null) {
			String value = getConfigurationProperties().getProperty(ConfigParams.WEB_BASE_URI_FIXED, "true").trim();
			BooleanParam param = new BooleanParam();
			param.setStringValue(value);
			isBaseUriFixed = param.getValue();
		}
		return isBaseUriFixed;		
	}

	public String getRealUri(String uri, boolean isRelative) {
		String baseUri = getBaseUri();
		if (!isRelative) {
			if (!uri.startsWith(baseUri)) {
				return uri;
			}
			uri = uri.substring(baseUri.length());
			isRelative = true; 
		}
		
		// uri is relative
		if (realBaseUri != null) {
			return realBaseUri + uri;
		} else {
			return getBaseUri() + uri;
		}
	}
	
	public void notifyRequest(UriInfo uriInfo) {		
		realBaseUri = uriInfo.getBaseUri().toString();
		if (!isBaseUriFixed() && !realBaseUri.equals(baseUri)) {
			logger.info("New BaseUri: " + realBaseUri);
			this.baseUri = realBaseUri;
		}
	}
		
	public String getUploadsDirPath() {
		if (uploadsDirPath == null) {
			uploadsDirPath = getConfigurationProperties().getProperty(ConfigParams.UPLOADS_DIR_PATH);
			if (uploadsDirPath != null) {				
				uploadsDirPath = getRealServerPath(uploadsDirPath);
			} else {
				uploadsDirPath = System.getenv(SystemEnvironment.DRUMBEAT_SHARE_FOLDER);
			}
			
			if (uploadsDirPath == null) {
				throw new RuntimeException(
						"The upload folder is not specified neither in config.properties file, nor as system environment variable " + 
								SystemEnvironment.DRUMBEAT_SHARE_FOLDER);
			}
			
			uploadsDirPath = uploadsDirPath.trim();
		}
		return uploadsDirPath;
	}
	
	/**
	 * Gets value indicating whether uploaded files must be saved
	 * @return
	 */
	public boolean isSavingUploadEnabled() {
		if (isSavingUploadEnabled == null) {
			String value = getConfigurationProperties().getProperty(ConfigParams.UPLOADS_SAVE_ENALBED, "false").trim();
			BooleanParam param = new BooleanParam();
			param.setStringValue(value);
			isSavingUploadEnabled = param.getValue();
		}
		return isSavingUploadEnabled;
	}
	
	/**
	 * Gets value indicating whether bulk upload is allowed
	 * @return
	 */
	public boolean isRdfBulkUploadEnabled() {
		if (isRdfBulkUploadEnabled == null) {
			String value = getConfigurationProperties().getProperty(ConfigParams.UPLOADS_RDF_BULK_ENALBED, "false").trim();
			BooleanParam param = new BooleanParam();
			param.setStringValue(value);
			isRdfBulkUploadEnabled = param.getValue();
		}
		return isRdfBulkUploadEnabled;
	}
	
	
	public String getBaseUri(String path) {
		return getConfigurationProperties().getProperty(ConfigParams.WEB_BASE_URI) + path;
	}
	
	public JenaProvider getJenaProvider() throws DrumbeatException {
		
		if (jenaProvider == null) {		
			String providerName = getConfigurationProperties().getProperty(ConfigParams.JENA_PROVIDER_PREFIX + AbstractJenaProvider.ARGUMENT_PROVIDER_NAME).trim();
			String providerClassName = getConfigurationProperties().getProperty(ConfigParams.JENA_PROVIDER_PREFIX + AbstractJenaProvider.ARGUMENT_PROVIDER_CLASS).trim();
			try {
				jenaProvider = AbstractJenaProvider.getFactory(providerName, providerClassName, getConfigurationProperties(), ConfigParams.JENA_PROVIDER_PREFIX);
			} catch (JenaProviderException e) {
				String message = "Error getting Jena provider: " + e.getMessage();
				logger.error(e.getMessage(), e);
				throw new DrumbeatException(message, e);
			}
		}
		return jenaProvider;		
	}
	
	/**
	 * Gets the Jena model which contains meta data
	 * @return
	 * @throws JenaProviderException
	 * @throws IOException
	 */
	public Model getMetaDataModel() throws DrumbeatException {
		try {
			return getJenaProvider().openDefaultModel();
		} catch (JenaProviderException e) {
			String message = "Error opening default Jena model: " + e.getMessage();
			logger.error(e.getMessage(), e);
			throw new DrumbeatException(message, e);			
		}
	}
	
	public Model getDataModel(String name) throws DrumbeatException {
		try {
			return getJenaProvider().openModel(name);
		} catch (JenaProviderException e) {
			String message = "Error opening Jena model: " + e.getMessage();
			logger.error(e.getMessage(), e);
			throw new DrumbeatException(message, e);			
		}		
	}
	
	public Model getOwlModel(String name, boolean ontModel) throws DrumbeatException {
		try {
			Model ifcModel = getJenaProvider().openModel(DrumbeatOntology.GRAPH_NAME_IFC);
			if (ontModel) {
				return ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, ifcModel);
			}
			return ifcModel;
		} catch (JenaProviderException e) {
			String message = "Error opening Jena model: " + e.getMessage();
			logger.error(e.getMessage(), e);
			throw new DrumbeatException(message, e);			
		}	
		
	}
	
	public String getRealServerPath(String path) {
		File file = new File(path);
		if (file.isAbsolute()) {
			return path;
		}
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
