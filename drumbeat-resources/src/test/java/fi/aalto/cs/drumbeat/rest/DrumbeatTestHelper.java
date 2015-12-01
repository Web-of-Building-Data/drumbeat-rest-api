package fi.aalto.cs.drumbeat.rest;


import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.xml.DOMConfigurator;

public class DrumbeatTestHelper {
	
	public static final String TEST_SCHEMA_VERSION = "IFC4_ADD1";
	
	public static final String TEST_RESOURCES_PATH ="src/test/java/resources/"; 
	
	public static final double DOUBLE_DELTA = 1e-15;
	
	public static final String CONFIG_FILE_PATH = TEST_RESOURCES_PATH + "config.properties";
	public static final String LOGGER_CONFIG_FILE_PATH = TEST_RESOURCES_PATH + "log4j.xml";
	public static final String TEST_MODEL_FILE_PATH = TEST_RESOURCES_PATH + "sample.ifc";
	
	private static boolean initialized = false;
	private static Properties configProperties;
	
	public static void init() throws Exception {
		if (!initialized) {
			initialized = true;
			DOMConfigurator.configure(LOGGER_CONFIG_FILE_PATH);
			configProperties = new Properties();
			configProperties.load(new FileInputStream(CONFIG_FILE_PATH));
		}
	}
	
	public static Properties getConfigProperties() {
		return configProperties;
	}
	
	public static String getWebBaseUrl() {
		return configProperties.getProperty("web.baseUrl");
	}
	
	
	
//	/**
//	 * Get the method name for a depth in call stack. <br />
//	 * Utility function
//	 * @param depth depth in the call stack (0 means current method, 1 means call method, ...)
//	 * @return method name
//	 */
//	public static String getMethodName(final int methodCallShift)
//	{
//	  StackTraceElement[] ste = Thread.currentThread().getStackTrace();
//	  return ste[methodCallShift + 2].getMethodName();
//	}
//	
//	public static String getTestFilePath(Object object, int methodCallShift, boolean isExpected, String extension) {
//		return String.format("%s%s/%s/%s.%s",
//				TEST_TARGET_RESOURCES_PATH,
//				isExpected ? "expected" : "actual",
//				object.getClass().getSimpleName(),
//				getMethodName(methodCallShift + 1),
//				extension);
//	}
	
}
