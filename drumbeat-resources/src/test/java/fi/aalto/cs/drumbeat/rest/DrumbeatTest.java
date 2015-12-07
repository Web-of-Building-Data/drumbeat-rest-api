package fi.aalto.cs.drumbeat.rest;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.BeforeClass;


import fi.aalto.cs.drumbeat.rest.application.TestApplication;

public class DrumbeatTest extends JerseyTest {
	
	private static TestApplication application;
	
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
		return true;
	}
	
	
	
/////**
////* Get the method name for a depth in call stack. <br />
////* Utility function
////* @param depth depth in the call stack (0 means current method, 1 means call method, ...)
////* @return method name
////*/
////public static String getMethodName(final int methodCallShift)
////{
//// StackTraceElement[] ste = Thread.currentThread().getStackTrace();
//// return ste[methodCallShift + 2].getMethodName();
////}
////
////public static String getTestFilePath(Object object, int methodCallShift, boolean isExpected, String extension) {
////	return String.format("%s%s/%s/%s.%s",
////			TEST_TARGET_RESOURCES_PATH,
////			isExpected ? "expected" : "actual",
////			object.getClass().getSimpleName(),
////			getMethodName(methodCallShift + 1),
////			extension);
////}

}
