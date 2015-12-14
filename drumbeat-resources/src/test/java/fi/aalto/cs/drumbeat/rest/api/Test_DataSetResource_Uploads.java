package fi.aalto.cs.drumbeat.rest.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;

import fi.aalto.cs.drumbeat.rest.DrumbeatTest;
import fi.aalto.cs.drumbeat.rest.application.TestApplication;
import fi.hut.cs.drumbeat.common.DrumbeatException;
import fi.hut.cs.drumbeat.rdf.RdfUtils;


public class Test_DataSetResource_Uploads extends DrumbeatTest {	
	
	private static final boolean DO_TEST = false;
	
	public Test_DataSetResource_Uploads() {
		super(DO_TEST);
	}
	
	private static final String DATA_SET_NAME_CORRECT_1 = "c1/structural/v1";
	private static final String DATA_SET_NAME_CORRECT_2 = "c1/structural/v2";
	private static final String DATA_SET_NAME_WRONG = "c1/structural/v123";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DrumbeatTest.setUpBeforeClass();
		Model metaDataModel = getApplication().getMetaDataModel();		
		String testDataFilePath = getApplication().getRealPath(TestApplication.TEST_RDF_META_DATA_FILE_PATH);
		RdfUtils.importRdfFileToJenaModel(metaDataModel, testDataFilePath);
	}
	
	
	@Test
	public void test_uploadServerFile_ifc_correctDataSet_correctFile() throws DrumbeatException {
		if (!doTest()) {
			return;
		}
		
		File testModelFilePath = new File(getApplication().getRealPath(TestApplication.TEST_IFC_MODEL_FILE_PATH));
		assertTrue(testModelFilePath.exists());		
		
		Form form = new Form();
		form.param("dataType", DataSetResource.DATA_TYPE_IFC);
		form.param("filePath", testModelFilePath.getAbsolutePath());
		
		Model metaDataModel = getApplication().getMetaDataModel();
		assertNotEquals(0L, metaDataModel.size());
		
		Map<String, String> result = null;
		
		try {		
			result =
				target("datasets/" + DATA_SET_NAME_CORRECT_1 + "/uploadServerFile")
					.request(MediaType.APPLICATION_JSON)
					.post(
							Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
							new GenericType<Map<String,String>>(){});
		} catch (WebApplicationException e) {
			Response response = e.getResponse();
			getLogger().error(e.getMessage());
			assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		}
		
		assertEquals(0L, Long.parseLong((String)result.get("oldSize")));
		assertNotEquals(0L, Long.parseLong(result.get("newSize")));
	}
	
	
	@Test
	public void test_uploadServerFile_ifc_correctDataSet_wrongFile() {
		if (!doTest()) {
			return;
		}
		
		File testModelFilePath = new File(getApplication().getRealPath(TestApplication.TEST_IFC_MODEL_FILE_PATH + "111"));
		assertFalse(testModelFilePath.exists());		
		
		Form form = new Form();
		form.param("dataType", DataSetResource.DATA_TYPE_IFC);
		form.param("filePath", testModelFilePath.getAbsolutePath());
		
		int statusCode;
		
		try {		
			target("datasets/" + DATA_SET_NAME_CORRECT_1 + "/uploadServerFile")
				.request(MediaType.APPLICATION_JSON)
				.post(
						Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
						new GenericType<Map<String,String>>(){});
			
			statusCode = Response.Status.OK.getStatusCode();
		} catch (WebApplicationException e) {
			statusCode = e.getResponse().getStatus();
		}
		
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), statusCode);
	}	
	

	@Test
	public void test_uploadServerFile_ifc_correctDataSet_wrongFileContent() {
		if (!doTest()) {
			return;
		}
		
		File testModelFilePath = new File(getApplication().getRealPath(TestApplication.TEST_IFC_MODEL_WITH_WRONG_CONTENT_FILE_PATH));
		assertTrue(testModelFilePath.exists());		
		
		Form form = new Form();
		form.param("dataType", DataSetResource.DATA_TYPE_IFC);
		form.param("filePath", testModelFilePath.getAbsolutePath());
		
		int statusCode;
		
		try {		
			target("datasets/" + DATA_SET_NAME_CORRECT_1 + "/uploadServerFile")
				.request(MediaType.APPLICATION_JSON)
				.post(
						Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
						new GenericType<Map<String,String>>(){});
			
			statusCode = Response.Status.OK.getStatusCode();
		} catch (WebApplicationException e) {
			statusCode = e.getResponse().getStatus();
		}
		
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), statusCode);
	}	

	@Test
	public void test_uploadServerFile_ifc_wrongDataSet_correctFile() {
		if (!doTest()) {
			return;
		}
		
		File testModelFilePath = new File(getApplication().getRealPath(TestApplication.TEST_IFC_MODEL_FILE_PATH));
		assertTrue(testModelFilePath.exists());		
		
		Form form = new Form();
		form.param("dataType", DataSetResource.DATA_TYPE_IFC);
		form.param("filePath", testModelFilePath.getAbsolutePath());
		
		int statusCode;
		
		try {		
			target("datasets/" + DATA_SET_NAME_WRONG + "/uploadServerFile")
				.request(MediaType.APPLICATION_JSON)
				.post(
						Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
						new GenericType<Map<String,String>>(){});
			
			statusCode = Response.Status.OK.getStatusCode();
		} catch (WebApplicationException e) {
			statusCode = e.getResponse().getStatus();
		}
		
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), statusCode);
	}
	
	
	@Test
	public void test_uploadServerFile_rdf_correctDataSet_correctFile() throws DrumbeatException {
		if (!doTest()) {
			return;
		}
		
		File testModelFilePath = new File(getApplication().getRealPath(TestApplication.TEST_RDF_MODEL_FILE_PATH));
		assertTrue(testModelFilePath.exists());		
		
		Form form = new Form();
		form.param("dataType", DataSetResource.DATA_TYPE_RDF);
		form.param("dataFormat", "turtle");
		form.param("filePath", testModelFilePath.getAbsolutePath());
		
		Model metaDataModel = getApplication().getMetaDataModel();
		assertNotEquals(0L, metaDataModel.size());
		
		Map<String, String> result = null;
		
		try {		
			result =
				target("datasets/" + DATA_SET_NAME_CORRECT_2 + "/uploadServerFile")
					.request(MediaType.APPLICATION_JSON)
					.post(
							Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
							new GenericType<Map<String,String>>(){});
		} catch (WebApplicationException e) {
			Response response = e.getResponse();
			getLogger().error(e.getMessage(), e);
			assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		}
		
		assertEquals(0L, Long.parseLong((String)result.get("oldSize")));
		assertNotEquals(0L, Long.parseLong(result.get("newSize")));
	}
	
	
	@Test
	public void test_uploadServerFile_rdf_correctDataSet_wrongFile() {
		if (!doTest()) {
			return;
		}
		
		File testModelFilePath = new File(getApplication().getRealPath(TestApplication.TEST_RDF_MODEL_FILE_PATH + "111"));
		assertFalse(testModelFilePath.exists());		
		
		Form form = new Form();
		form.param("dataType", DataSetResource.DATA_TYPE_RDF);
		form.param("dataFormat", "turtle");
		form.param("filePath", testModelFilePath.getAbsolutePath());
		
		int statusCode;
		
		try {		
			target("datasets/" + DATA_SET_NAME_CORRECT_2 + "/uploadServerFile")
				.request(MediaType.APPLICATION_JSON)
				.post(
						Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
						new GenericType<Map<String,String>>(){});
			
			statusCode = Response.Status.OK.getStatusCode();
		} catch (WebApplicationException e) {
			statusCode = e.getResponse().getStatus();
		}
		
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), statusCode);
	}	
	

	@Test
	public void test_uploadServerFile_rdf_correctDataSet_wrongFileContent() {
		if (!doTest()) {
			return;
		}
		
		File testModelFilePath = new File(getApplication().getRealPath(TestApplication.TEST_RDF_MODEL_WITH_WRONG_CONTENT_FILE_PATH));
		assertTrue(testModelFilePath.exists());		
		
		Form form = new Form();
		form.param("dataType", DataSetResource.DATA_TYPE_RDF);
		form.param("filePath", testModelFilePath.getAbsolutePath());
		
		int statusCode;
		
		try {		
			target("datasets/" + DATA_SET_NAME_CORRECT_1 + "/uploadServerFile")
				.request(MediaType.APPLICATION_JSON)
				.post(
						Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
						new GenericType<Map<String,String>>(){});
			
			statusCode = Response.Status.OK.getStatusCode();
		} catch (WebApplicationException e) {
			e.getResponse().getStatusInfo();
			String message = e.getResponse().getEntity().toString(); 
			getLogger().debug(message);
			statusCode = e.getResponse().getStatus();
		}
		
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), statusCode);
	}	

	@Test
	public void test_uploadServerFile_rdf_wrongDataSet_correctFile() {
		if (!doTest()) {
			return;
		}
		
		File testModelFilePath = new File(getApplication().getRealPath(TestApplication.TEST_RDF_MODEL_FILE_PATH));
		assertTrue(testModelFilePath.exists());		
		
		Form form = new Form();
		form.param("dataType", DataSetResource.DATA_TYPE_RDF);
		form.param("filePath", testModelFilePath.getAbsolutePath());
		
		int statusCode;
		
		try {		
			target("datasets/" + DATA_SET_NAME_WRONG + "/uploadServerFile")
				.request(MediaType.APPLICATION_JSON)
				.post(
						Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
						new GenericType<Map<String,String>>(){});
			
			statusCode = Response.Status.OK.getStatusCode();
		} catch (WebApplicationException e) {
			statusCode = e.getResponse().getStatus();
		}
		
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), statusCode);
	}
		
	
}
