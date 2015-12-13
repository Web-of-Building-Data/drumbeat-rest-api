package fi.aalto.cs.drumbeat.rest.managers;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.AlreadyExistsException;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.aalto.cs.drumbeat.rest.DrumbeatTest;
import fi.aalto.cs.drumbeat.rest.application.TestApplication;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.ontology.LinkedBuildingDataOntology;
import fi.hut.cs.drumbeat.rdf.RdfUtils;

public class Test_DataSourceManager extends DrumbeatTest {
	
	private static final boolean DO_TEST = true;
	
	private static DataSourceManager dataSourceManager;
	private static Model metaDataModel;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DrumbeatTest.setUpBeforeClass();
		metaDataModel = getApplication().getMetaDataModel();		
		String testDataFilePath = getApplication().getRealPath(TestApplication.TEST_RDF_META_DATA_FILE_PATH);
		RdfUtils.importRdfFileToJenaModel(metaDataModel, testDataFilePath);
		dataSourceManager = new DataSourceManager();
	}

	public Test_DataSourceManager() {
		super(DO_TEST);
	}
	
	
	/***************************************
	 * getAll()
	 **************************************/
	
	@Test(expected=NotFoundException.class)
	public void test_getAll_wrongId() {
		Model model = dataSourceManager.getAll("col-1-999");
		assertEquals(0L, model.size());		
	}	

	@Test
	public void test_getAll_correctId_emptyList() {
		Model model = dataSourceManager.getAll("col-2");
		assertEquals(0L, model.size());
	}	

	@Test
	public void test_getAll_correctId_nonEmptyList() {
		Model model = dataSourceManager.getAll("col-1");
		assertEquals(4L, model.size());
		
		String baseUri = DrumbeatApplication.getInstance().getBaseUri();
		assertTrue(
				model.contains(
						model.createResource(baseUri + "datasources/col-1/dso-1-1"),
						RDF.type,
						LinkedBuildingDataOntology.DataSource));

		assertTrue(
				model.contains(
						model.createResource(baseUri + "datasources/col-1/dso-1-2"),
						RDF.type,
						LinkedBuildingDataOntology.DataSource));

		assertTrue(
				model.contains(
						model.createResource(baseUri + "datasources/col-1/dso-1-3"),
						RDF.type,
						LinkedBuildingDataOntology.DataSource));

		assertTrue(
				model.contains(
						model.createResource(baseUri + "datasources/col-1/dso-1-4"),
						RDF.type,
						LinkedBuildingDataOntology.DataSource));
	}
	

	/***************************************
	 * getById()
	 **************************************/
	@Test
	public void test_getById_correctId_nonEmptyList() {
		Model model = dataSourceManager.getById("col-1", "dso-1-1");
		
		assertEquals(5L, model.size());
		
		String baseUri = DrumbeatApplication.getInstance().getBaseUri();
		Resource dataSourceResource = model.createResource(baseUri + "datasources/col-1/dso-1-1"); 
		
		assertEquals(
				LinkedBuildingDataOntology.DataSource,
				dataSourceResource.getProperty(RDF.type).getObject());

		assertEquals(
				"DataSource 1-1",
				dataSourceResource.getProperty(LinkedBuildingDataOntology.name).getLiteral().getValue());

		assertEquals(
				model.createResource(baseUri + "collections/col-1"),
				dataSourceResource.getProperty(LinkedBuildingDataOntology.inCollection).getObject());
		
		assertTrue(
				model.contains(
						dataSourceResource,
						LinkedBuildingDataOntology.hasDataSet,
						model.createResource(baseUri + "datasets/col-1/dso-1-1/dse-1-1-1")));
				
		
		assertTrue(
				model.contains(
						dataSourceResource,
						LinkedBuildingDataOntology.hasDataSet,
						model.createResource(baseUri + "datasets/col-1/dso-1-1/dse-1-1-2")));
	}
	
	
	@Test(expected=NotFoundException.class)
	public void test_getById_wrongId() {
		dataSourceManager.getById("col-1", "dso-1-999");
	}


	/***************************************
	 * create()
	 **************************************/

	@Test(expected=NotFoundException.class)
	public void test_create_wrongId() {
		dataSourceManager.create("col-999", "dso-999-1", "DataSource 1-999-1");
	}
	
	@Test(expected=AlreadyExistsException.class)
	public void test_create_correctId_alreadyExists() {
		dataSourceManager.create("col-1", "dso-1-1", "DataSource 1-1-1");
	}
	
	@Test
	public void test_create_correctId_new() {
		
		long oldSize = metaDataModel.size();
		
		Resource dataSourceResource = dataSourceManager.create("col-3", "dso-3-1", "DataSource 3-1");
		
		assertEquals(oldSize + 4L, metaDataModel.size());
		
		String baseUri = DrumbeatApplication.getInstance().getBaseUri();
		assertEquals(
				LinkedBuildingDataOntology.DataSource,
				dataSourceResource.getProperty(RDF.type).getObject());
		assertEquals(
				metaDataModel.createResource(baseUri + "collections/col-3"),
				dataSourceResource.getProperty(LinkedBuildingDataOntology.inCollection).getObject());
		assertEquals(
				"DataSource 3-1",
				dataSourceResource.getProperty(LinkedBuildingDataOntology.name).getLiteral().getValue());
		
	}
	
	
	/***************************************
	 * delete()
	 **************************************/

	@Test(expected=NotFoundException.class)
	public void test_delete_wrongId() {
		dataSourceManager.delete("col-1", "dso-1-999");
	}
	

	@Test
	public void test_delete_correctId() {
		
		long oldSize = metaDataModel.size();

		Resource dataSourceResource = dataSourceManager.create("col-4", "dso-4-1", "DataSource 4-1");
		
		String baseUri = DrumbeatApplication.getInstance().getBaseUri();
		assertEquals(baseUri + "datasources/col-4/dso-4-1", dataSourceResource.getURI());

		assertEquals(oldSize + 4L, metaDataModel.size());
		
		dataSourceManager.delete("col-4", "dso-4-1");
		
		assertEquals(oldSize, metaDataModel.size());
		
		assertFalse(
				metaDataModel.contains(dataSourceResource, null, (RDFNode)null));
				
		assertFalse(
				metaDataModel.contains(null, null, dataSourceResource));
		
	}
}
