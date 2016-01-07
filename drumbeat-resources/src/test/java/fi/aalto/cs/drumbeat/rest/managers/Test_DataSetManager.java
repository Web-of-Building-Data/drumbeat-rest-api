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
import fi.aalto.cs.drumbeat.rdf.RdfUtils;

public class Test_DataSetManager extends DrumbeatTest {
	
	private static final boolean DO_TEST = true;
	
	private static DataSetManager dataSetManager;
	private static Model metaDataModel;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DrumbeatTest.setUpBeforeClass();
		metaDataModel = getApplication().getMetaDataModel();		
		String testDataFilePath = getApplication().getRealServerPath(TestApplication.TEST_RDF_META_DATA_FILE_PATH);
		RdfUtils.importRdfFileToJenaModel(metaDataModel, testDataFilePath);
		dataSetManager = new DataSetManager();
	}

	public Test_DataSetManager() {
		super(DO_TEST);
	}
	
	
	/***************************************
	 * getAll()
	 **************************************/
	
	@Test(expected=NotFoundException.class)
	public void test_getAll_wrongCollectionId() {
		if (!doTest()) {
			return;
		}
		Model model = dataSetManager.getAll("col-999", "dso-1-1");
		assertEquals(0L, model.size());		
	}	

	@Test(expected=NotFoundException.class)
	public void test_getAll_wrongDataSourceId() {
		if (!doTest()) {
			return;
		}
		Model model = dataSetManager.getAll("col-1", "dso-1-999");
		assertEquals(0L, model.size());		
	}	

	@Test
	public void test_getAll_correctId_emptyList() {
		if (!doTest()) {
			return;
		}
		Model model = dataSetManager.getAll("col-1", "dso-1-2");
		assertEquals(0L, model.size());
	}	

	@Test
	public void test_getAll_correctId_nonEmptyList() {
		if (!doTest()) {
			return;
		}
		Model model = dataSetManager.getAll("col-1", "dso-1-1");
		assertEquals(2L, model.size());
		
		String baseUri = DrumbeatApplication.getInstance().getBaseUri();
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
	

	/***************************************
	 * getById()
	 **************************************/
	@Test
	public void test_getById_correctId_nonEmptyList() {
		if (!doTest()) {
			return;
		}
		Model model = dataSetManager.getById("col-1", "dso-1-1", "dse-1-1-1");
		
		assertEquals(3L, model.size());
		
		String baseUri = DrumbeatApplication.getInstance().getBaseUri();
		Resource dataSetResource = model.createResource(baseUri + "datasets/col-1/dso-1-1/dse-1-1-1"); 
		
		assertEquals(
				LinkedBuildingDataOntology.DataSet,
				dataSetResource.getProperty(RDF.type).getObject());

		assertEquals(
				"DataSet 1-1-1",
				dataSetResource.getProperty(LinkedBuildingDataOntology.name).getLiteral().getValue());

		assertEquals(
				model.createResource(baseUri + "datasources/col-1/dso-1-1"),
				dataSetResource.getProperty(LinkedBuildingDataOntology.inDataSource).getObject());
	}
	
	
	@Test(expected=NotFoundException.class)
	public void test_getById_wrongId() {
		if (!doTest()) {
			return;
		}
		dataSetManager.getById("col-1", "dso-1-2", "dse-1-1-999");
	}


	/***************************************
	 * create()
	 **************************************/

	@Test(expected=NotFoundException.class)
	public void test_create_wrongId() {
		if (!doTest()) {
			return;
		}
		dataSetManager.create("col-1", "dso-1-999", "dse-1-999-1", "DataSet 1-999-1", null);
	}
	
	@Test(expected=AlreadyExistsException.class)
	public void test_create_correctId_alreadyExists() {
		if (!doTest()) {
			return;
		}
		dataSetManager.create("col-1", "dso-1-1", "dse-1-1-1", "DataSet 1-1-1", null);
	}
	
	@Test
	public void test_create_correctId_new() {
		if (!doTest()) {
			return;
		}
		
		long oldSize = metaDataModel.size();
		
		Model model = dataSetManager.create("col-1", "dso-1-3", "dse-1-3-1", "DataSet 1-3-1", null);
		assertEquals(1L,  model.size());
		
		Resource dataSetResource = model.listSubjects().next();
		assertEquals(
				LinkedBuildingDataOntology.DataSet,
				dataSetResource.getProperty(RDF.type).getObject());
		
		dataSetResource = dataSetResource.inModel(metaDataModel);

		assertTrue(oldSize + 4L <= metaDataModel.size());
		
		String baseUri = DrumbeatApplication.getInstance().getBaseUri();
		assertEquals(
				LinkedBuildingDataOntology.DataSet,
				dataSetResource.getProperty(RDF.type).getObject());
		assertEquals(
				metaDataModel.createResource(baseUri + "datasources/col-1/dso-1-3"),
				dataSetResource.getProperty(LinkedBuildingDataOntology.inDataSource).getObject());
		assertEquals(
				"DataSet 1-3-1",
				dataSetResource.getProperty(LinkedBuildingDataOntology.name).getLiteral().getValue());
		
	}
	
	
	/***************************************
	 * delete()
	 **************************************/

	@Test(expected=NotFoundException.class)
	public void test_delete_wrongId() {
		if (!doTest()) {
			return;
		}
		dataSetManager.delete("col-1", "dso-1-999", "dse-1-999-1");
	}
	

	@Test
	public void test_delete_correctId() {
		if (!doTest()) {
			return;
		}
		
		long oldSize = metaDataModel.size();

		Model model = dataSetManager.create("col-1", "dso-1-4", "dse-1-4-1", "DataSet 1-4-1", null);
		
		assertEquals(1L,  model.size());
		
		Resource dataSetResource = model.listSubjects().next();
		assertEquals(
				LinkedBuildingDataOntology.DataSet,
				dataSetResource.getProperty(RDF.type).getObject());
		
		dataSetResource = dataSetResource.inModel(metaDataModel);
		

		String baseUri = DrumbeatApplication.getInstance().getBaseUri();
		assertEquals(baseUri + "datasets/col-1/dso-1-4/dse-1-4-1", dataSetResource.getURI());

		assertTrue(oldSize + 4L <= metaDataModel.size());
		
		dataSetManager.delete("col-1", "dso-1-4", "dse-1-4-1");
		
		assertEquals(oldSize, metaDataModel.size());
		
		assertFalse(
				metaDataModel.contains(dataSetResource, null, (RDFNode)null));
				
		assertFalse(
				metaDataModel.contains(null, null, dataSetResource));
		
	}
}
