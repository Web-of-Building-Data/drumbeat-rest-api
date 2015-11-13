package fi.hut.cs.drumbeat.rdf.modelfactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import com.hp.hpl.jena.rdf.model.Model;

//import fi.hut.cs.drumbeat.common.config.ConfigurationItemEx;
//import fi.hut.cs.drumbeat.common.string.StringUtils;


public abstract class AbstractJenaModelFactory {
	
	/****************************************
	 *  STATIC MEMBERS
	 ****************************************/
	
	public static final String ARGUMENT_NAME = "name";
	public static final String ARGUMENT_CLASS = "class";
	public static final String ARGUMENT_SERVER_URL = "serverUrl";
	public static final String ARGUMENT_USER_NAME = "userName";
	public static final String ARGUMENT_PASSWORD = "password";
	public static final String ARGUMENT_MODEL_ID = "modelId";
	
	public static AbstractJenaModelFactory getFactory(String name, Properties properties, String propertyPrefix)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		String className = getProperty(properties, propertyPrefix, ARGUMENT_CLASS, true);
		
		Class<? extends AbstractJenaModelFactory> jenaModelFactoryClass =
				Class.forName(className).asSubclass(AbstractJenaModelFactory.class);
		
		Constructor<? extends AbstractJenaModelFactory> constructor = jenaModelFactoryClass.getConstructor(String.class, Properties.class, String.class);
		AbstractJenaModelFactory modelFactory = constructor.newInstance(name, properties, propertyPrefix);
		return modelFactory;
		
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
	private String modelId;
	
	public AbstractJenaModelFactory(String name, Properties properties, String propertyPrefix) {
		this.name = name;
		if (properties != null) {
			serverUrl = getProperty(properties, propertyPrefix, ARGUMENT_SERVER_URL, true); 
			userName = getProperty(properties, propertyPrefix, ARGUMENT_USER_NAME, true);
			password = getProperty(properties, propertyPrefix, ARGUMENT_PASSWORD, true);
			modelId = getProperty(properties, propertyPrefix, ARGUMENT_MODEL_ID, true);
		}
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

	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}


	private static String getProperty(Properties properties, String namePrefix, String name, boolean isMandatory) {
		if (namePrefix != null) {
			name = namePrefix + name;
		}
		
		String value = properties.getProperty(name);
		if (isMandatory && (value == null || value.isEmpty())) {
			throw new IllegalArgumentException(String.format("Undefined parameter '%s'", name));
		}
		return value;
	}	
	
	public abstract Model createModel() throws Exception;
	
	public abstract Model getModel() throws Exception;
	
	public abstract void release() throws Exception;
	
	
	
}
