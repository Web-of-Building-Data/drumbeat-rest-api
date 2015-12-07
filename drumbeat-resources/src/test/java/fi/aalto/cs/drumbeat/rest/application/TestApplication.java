package fi.aalto.cs.drumbeat.rest.application;

public class TestApplication extends DrumbeatApplication {
	
	public static final String TEST_RESOURCES_FOLDER = DrumbeatApplication.Paths.RESOURCES_FOLDER_PATH + "test/";
	public static final String TEST_RDF_DATA_FILE_PATH = TEST_RESOURCES_FOLDER + "rest-test-2.ttl";	
	public static final String TEST_IFC_MODEL_FILE_PATH = TEST_RESOURCES_FOLDER + "sample.ifc";	

	public TestApplication() {
		super("src/test/java/");
	}

}
