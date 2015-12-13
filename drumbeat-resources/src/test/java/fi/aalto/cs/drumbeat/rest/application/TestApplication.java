package fi.aalto.cs.drumbeat.rest.application;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;

public class TestApplication extends DrumbeatApplication {
	
	public static final String TEST_RESOURCES_FOLDER = DrumbeatApplication.Paths.RESOURCES_FOLDER_PATH + "test/";
	public static final String TEST_RDF_META_DATA_FILE_PATH = TEST_RESOURCES_FOLDER + "rest-test-3.ttl";
	
	public static final String TEST_IFC_MODEL_FILE_PATH = TEST_RESOURCES_FOLDER + "sample.ifc";	
	public static final String TEST_IFC_MODEL_WITH_WRONG_CONTENT_FILE_PATH = TEST_RESOURCES_FOLDER + "sample_wrong_content.ifc";
	
	public static final String TEST_RDF_MODEL_FILE_PATH = TEST_RESOURCES_FOLDER + "sample.ttl";	
	public static final String TEST_RDF_MODEL_WITH_WRONG_CONTENT_FILE_PATH = TEST_RESOURCES_FOLDER + "sample_wrong_content.ttl";	

	public TestApplication() {
		super("src/test/java/");
	}

}
