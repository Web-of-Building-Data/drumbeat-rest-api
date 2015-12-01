package fi.hut.cs.drumbeat.rdf.modelfactory;

import java.lang.reflect.Constructor;
import java.util.Properties;

import com.hp.hpl.jena.rdf.model.Model;

public abstract class AbstractJenaProvider {
	
	/****************************************
	 *  STATIC MEMBERS
	 ****************************************/
	
	public static final String ARGUMENT_PROVIDER_NAME = "providerName";
	public static final String ARGUMENT_PROVIDER_CLASS = "providerClass";
	public static final String ARGUMENT_SERVER_URL = "serverUrl";
	public static final String ARGUMENT_USER_NAME = "userName";
	public static final String ARGUMENT_PASSWORD = "password";
	public static final String ARGUMENT_DEFAULT_GRAPH_NAME = "defaultGraphName";
	
	public static AbstractJenaProvider getFactory(String name, String className, Properties properties, String propertyPrefix) throws JenaProviderException
	{
		try {
			if (className == null) {
				throw new IllegalArgumentException("Jena provider class name is undefined");				
			}
			
			Class<?> class1 = Class.forName(className); 
			
			Class<? extends AbstractJenaProvider> jenaModelFactoryClass =
					class1.asSubclass(AbstractJenaProvider.class);
			
			Constructor<? extends AbstractJenaProvider> constructor = jenaModelFactoryClass.getConstructor(String.class, Properties.class, String.class);
			AbstractJenaProvider modelFactory = constructor.newInstance(name, properties, propertyPrefix);
			return modelFactory;
		} catch (Exception e) {
			throw new JenaProviderException(e);
		}		
	}
	
	
	
//	public static JenaModelFactoryBase getFactory(ConfigurationItemEx configuration) throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//		String name = configuration.getName();
//		String className = configuration.getType();
//		Properties properties = configuration.getProperties();		
//		
//		Class<? extends JenaModelFactoryBase> jenaModelFactoryClass = Class.forName(className).asSubclass(JenaModelFactoryBase.class);
//		Constructor<? extends JenaModelFactoryBase> constructor = jenaModelFactoryClass.getConstructor(String.class, Properties.class);
//		JenaModelFactoryBase modelFactory = constructor.newInstance(name, properties);
//		return modelFactory;
//	}
	
	private String name;
	private String serverUrl;
	private String userName;
	private String password;
	private String defaultGraphName;
	private Properties properties;
	private String propertyPrefix;
	
	public AbstractJenaProvider(String providerName, Properties properties, String propertyPrefix) {
		this.name = providerName;
		this.properties = properties;
		this.propertyPrefix = propertyPrefix;
		
		if (properties != null) {
			this.serverUrl = getProperty(ARGUMENT_SERVER_URL, false); 
			this.userName = getProperty(ARGUMENT_USER_NAME, false);
			this.password = getProperty(ARGUMENT_PASSWORD, false);
			this.defaultGraphName = getProperty(ARGUMENT_DEFAULT_GRAPH_NAME, false);
		}
	}
	
	public AbstractJenaProvider(
			String providerName,
			String serverUrl,
			String userName,
			String password,
			String defaultGraphName,
			Properties properties,
			String propertyPrefix)
	{
		this.name = providerName;
		this.serverUrl = serverUrl;
		this.userName = userName;
		this.password = password;
		this.defaultGraphName=defaultGraphName;
		this.properties = properties;
		this.propertyPrefix = propertyPrefix;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getServerUrl() {
		return serverUrl;
	}
	
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	protected String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDefaultGraphName() {
		return defaultGraphName;
	}

	public void setDefaultGraphName(String graphName) {
		this.defaultGraphName = graphName;
	}
	
	protected Properties getProperties() {
		return properties;
	}
	
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	private String getProperty(String name, boolean isMandatory) {
		if (propertyPrefix != null) {
			name = propertyPrefix + name;
		}
		
		String value = properties.getProperty(name);
		if (isMandatory && (value == null || value.isEmpty())) {
			throw new IllegalArgumentException(String.format("Undefined parameter '%s'", name));
		}
		return value;
	}
	
	/**
	 * Initialises resources 
	 */	
	public void init() throws JenaProviderException {		
	}
	
	/**
	 * Releases resources (connections, cached data and so on) 
	 */	
	public void release() throws JenaProviderException {		
	}

	/**
	 * Returns a Jena model instance for the default graph
	 * @param graphName
	 * @return
	 */
	public Model openDefaultModel() throws JenaProviderException {
		return openModel(getDefaultGraphName());
	}	
	
	
	/**
	 * Returns a Jena model instance for the given graph name. If the model already exists, it is simply opened.
	 * @param graphName
	 * @return
	 */
	public abstract Model openModel(String graphName) throws JenaProviderException;
	
	
}
