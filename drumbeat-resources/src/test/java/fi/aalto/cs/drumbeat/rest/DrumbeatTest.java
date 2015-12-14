package fi.aalto.cs.drumbeat.rest;

import javax.ws.rs.core.Application;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.BeforeClass;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import fi.aalto.cs.drumbeat.rest.application.TestApplication;

public class DrumbeatTest extends JerseyTest {

	private static TestApplication application;
	private final boolean doTest;
	
	public DrumbeatTest(boolean doTest) {
		this.doTest = doTest;
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		application = new TestApplication();
	}

	@Override
	protected Application configure() {
		return application;
	}

	protected static TestApplication getApplication() {
		return application;
	}

	protected boolean doTest() {
		if (!doTest) {
			getLogger().warn(
					String.format(
							"%s.%s(): test skipped",
							getClass().getName(),
							getMethodName(1)));
		}
		return doTest;
	}
	
	protected Logger getLogger() {
		return Logger.getLogger(getClass());
	}

	/**
	 * Get the method name for a depth in call stack. <br />
	 * Utility function
	 * 
	 * @param depth
	 *            depth in the call stack (0 means current method, 1 means call
	 *            method, ...)
	 * @return method name
	 */
	public static String getMethodName(final int methodCallShift) {
		StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		return ste[methodCallShift + 2].getMethodName();
	}

//	public static String getTestFilePath(Object object, int methodCallShift, boolean isExpected, String extension) {
//		return String.format("%s%s/%s/%s.%s", TEST_TARGET_RESOURCES_PATH, isExpected ? "expected" : "actual",
//				object.getClass().getSimpleName(), getMethodName(methodCallShift + 1), extension);
//	}
	
	
	public void logModel(Level level, Model model) {
		
		Logger logger = getLogger(); 
		
		StmtIterator stmtIterator = model.listStatements();
		while (stmtIterator.hasNext()) {
			logger.log(level, stmtIterator.next().asTriple().toString());
		}
		
	}
	

}
