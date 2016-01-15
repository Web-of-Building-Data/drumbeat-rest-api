package fi.aalto.cs.drumbeat.rest.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringBufferInputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.aalto.cs.drumbeat.rest.DrumbeatTest;
import fi.aalto.cs.drumbeat.rest.application.TestApplication;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.common.LinkedBuildingDataOntology;
import fi.aalto.cs.drumbeat.common.DrumbeatException;
import fi.aalto.cs.drumbeat.rdf.RdfUtils;


public class Test_DataSetResource extends DrumbeatTest {	
	
	private static final boolean DO_TEST = true;
	
	public Test_DataSetResource() {
		super(DO_TEST);
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DrumbeatTest.setUpBeforeClass();
		Model metaDataModel = getApplication().getMetaDataModel();		
		String testDataFilePath = getApplication().getRealServerPath(TestApplication.TEST_RDF_META_DATA_FILE_PATH);
		RdfUtils.importRdfFileToJenaModel(metaDataModel, testDataFilePath);
	}
	
	/***************************************
	 * getAll()
	 **************************************/
	
	@Test(expected=NotFoundException.class)
	public void test_getAll_wrongCollectionId() {
		if (!doTest()) {
			return;
		}
		
		try {
			target("datasets/col-999/dso-1-1/")
				.request(MediaType.APPLICATION_JSON)
				.get(new GenericType<Map<String,String>>(){});
		} catch (NotFoundException e) {
			Response response = e.getResponse();
			String baseUri = DrumbeatApplication.getInstance().getBaseUri();
			String expectedMessage = String.format(
					"%s: DataSource not found: <%sdatasources/col-999/dso-1-1>",
					com.hp.hpl.jena.shared.NotFoundException.class,
					baseUri);

			String actualMessage = response.readEntity(String.class);
			assertTrue(expectedMessage.equals(actualMessage));
			
			throw e;
		}		
		
	}	

	@Test(expected=NotFoundException.class)
	public void test_getAll_wrongDataSourceId() {
		if (!doTest()) {
			return;
		}
		
		try {
			target("datasets/col-1/dso-1-999/")
				.request(MediaType.APPLICATION_JSON)
				.get(new GenericType<Map<String,String>>(){});
		} catch (NotFoundException e) {
			Response response = e.getResponse();
			String baseUri = DrumbeatApplication.getInstance().getBaseUri();
			String expectedMessage = String.format(
					"%s: DataSource not found: <%sdatasources/col-1/dso-1-999>",
					com.hp.hpl.jena.shared.NotFoundException.class, 
					baseUri);

			String actualMessage = response.readEntity(String.class);
			assertTrue(expectedMessage.equals(actualMessage));
			
			throw e;
		}		
		
	}	

	@Test
	public void test_getAll_correctId_nonEmptyList() {
		if (!doTest()) {
			return;
		}
		
		String entity = target("datasets/col-1/dso-1-1")
			.request("text/turtle")
			.get(String.class);		
		
		String baseUri = DrumbeatApplication.getInstance().getBaseUri();

		Model model = ModelFactory.createDefaultModel();
		model.read(new ByteArrayInputStream(entity.getBytes()), baseUri, "TURTLE");
		
		assertEquals(2L, model.size());
		
		assertTrue(
				model.contains(
						model.createResource(baseUri + "datasets/col-1/dso-1-1/dse-1-1-1"),
						RDF.type,
						LinkedBuildingDataOntology.DataSet));

		assertTrue(
				model.contains(
						model.createResource(baseUri + "datasets/col-1/dso-1-1/dse-1-1-2"),
						RDF.type,
						LinkedBuildingDataOntology.DataSet));
	}	
	
	
}
