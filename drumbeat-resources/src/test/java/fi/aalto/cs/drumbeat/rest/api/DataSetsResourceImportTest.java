package fi.aalto.cs.drumbeat.rest.api;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;

import fi.aalto.cs.drumbeat.rest.DrumbeatTest;
import fi.aalto.cs.drumbeat.rest.application.TestApplication;
import fi.hut.cs.drumbeat.rdf.RdfUtils;


public class DataSetsResourceImportTest extends DrumbeatTest {	
	
	private static final boolean DO_TEST = true;
	
//	private static final Logger logger = Logger.getLogger(DataSetsResourceImportTest.class);	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DrumbeatTest.setUpBeforeClass();
		Model defaultJenaModel = getApplication().getJenaProvider().openDefaultModel();		
		String testDataFilePath = getApplication().getRealPath(TestApplication.TEST_RDF_DATA_FILE_PATH);
		RdfUtils.importRdfFileToJenaModel(defaultJenaModel, testDataFilePath);
	}
	
	@Test
	public void test_datasets_alive() {
		if (!DO_TEST) {
			return;
		}
		
		Map<String, String> result =
				target("datasets/alive")
					.request(MediaType.APPLICATION_JSON_TYPE)
					.get(new GenericType<Map<String,String>>(){});
		
		String status = result.get("status");
		assertEquals("LIVE", status);		
	}
	
	
	
	@Test
	public void test_importServerFile_correctDataSet_correctFile() {
		if (!DO_TEST) {
			return;
		}
		
		File testModelFilePath = new File(getApplication().getRealPath(TestApplication.TEST_IFC_MODEL_FILE_PATH));
		assertTrue(testModelFilePath.exists());		
		
		Form form = new Form();
		form.param("dataType", DataSetResource.DATA_TYPE_IFC);
		form.param("filePath", testModelFilePath.getAbsolutePath());
		
		Map<String, String> result =
			target("datasets/c1/structural/v1/uploadServerFile")
				.request(MediaType.APPLICATION_JSON)
				.post(
						Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
						new GenericType<Map<String,String>>(){});
		
		assertEquals(0, Integer.parseInt(result.get("oldSize")));
		assertNotEquals(0, Integer.parseInt(result.get("newSize")));
	}
	

}
