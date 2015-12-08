package fi.aalto.cs.drumbeat.rest.api;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.jena.riot.Lang;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;

import fi.aalto.cs.drumbeat.rest.DrumbeatTest;
import fi.aalto.cs.drumbeat.rest.application.TestApplication;
import fi.hut.cs.drumbeat.rdf.RdfUtils;
import fi.hut.cs.drumbeat.rdf.modelfactory.JenaProviderException;


public class DataSetResourceImportTest extends DrumbeatTest {	
	
	private static final boolean DO_TEST = true;
	
	private static final String DATA_SET_NAME_CORRECT_1 = "c1/structural/v1";
	private static final String DATA_SET_NAME_CORRECT_2 = "c1/structural/v2";
	private static final String DATA_SET_NAME_CORRECT_3 = "c1/structural/v3";
	private static final String DATA_SET_NAME_WRONG = "c1/structural/v123";
	
	@Override
	protected boolean doTest() {
		return DO_TEST && super.doTest();
	}
	
	private static final Logger logger = Logger.getLogger(DataSetResourceImportTest.class);	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DrumbeatTest.setUpBeforeClass();
		Model defaultJenaModel = getApplication().getJenaProvider().openDefaultModel();		
		String testDataFilePath = getApplication().getRealPath(TestApplication.TEST_RDF_META_DATA_FILE_PATH);
		RdfUtils.importRdfFileToJenaModel(defaultJenaModel, testDataFilePath);
	}
	
	@Test
	public void test_datasets_alive() {
		if (!doTest()) {
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
	public void test_uploadServerFile_ifc_correctDataSet_correctFile() throws JenaProviderException, IOException {
		if (!doTest()) {
			return;
		}
		
		File testModelFilePath = new File(getApplication().getRealPath(TestApplication.TEST_IFC_MODEL_FILE_PATH));
		assertTrue(testModelFilePath.exists());		
		
		Form form = new Form();
		form.param("dataType", DataSetResource.DATA_TYPE_IFC);
		form.param("filePath", testModelFilePath.getAbsolutePath());
		
		Model model = getApplication().getJenaProvider().openDefaultModel();
		assertNotEquals(0L, model.size());
		
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
			logger.error(e.getMessage());
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
	public void test_uploadServerFile_rdf_correctDataSet_correctFile() throws JenaProviderException, IOException {
		if (!doTest()) {
			return;
		}
		
		File testModelFilePath = new File(getApplication().getRealPath(TestApplication.TEST_RDF_MODEL_FILE_PATH));
		assertTrue(testModelFilePath.exists());		
		
		Form form = new Form();
		form.param("dataType", DataSetResource.DATA_TYPE_RDF);
		form.param("dataFormat", "turtle");
		form.param("filePath", testModelFilePath.getAbsolutePath());
		
		Model model = getApplication().getJenaProvider().openDefaultModel();
		assertNotEquals(0L, model.size());
		
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
			logger.error(e.getMessage(), e);
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
