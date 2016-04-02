package fi.aalto.cs.drumbeat.rest.managers;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.AlreadyExistsException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.NotFoundException;
import org.apache.jena.vocabulary.RDF;

import fi.aalto.cs.drumbeat.rest.DrumbeatTest;
import fi.aalto.cs.drumbeat.rest.application.TestApplication;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatOntology;
import fi.aalto.cs.drumbeat.rdf.utils.RdfIOUtils;

public class Test_DataSourceManager extends DrumbeatTest {
	
	private static final boolean DO_TEST = true;
	
	private static DataSourceManager dataSourceManager;
	private static Model metaDataModel;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DrumbeatTest.setUpBeforeClass();
		metaDataModel = getApplication().getMetaDataModel();		
		String testDataFilePath = getApplication().getRealServerPath(TestApplication.TEST_RDF_META_DATA_FILE_PATH);
		RdfIOUtils.importRdfFileToJenaModel(metaDataModel, testDataFilePath);
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
		if (!doTest()) {
			return;
		}
		Model model = dataSourceManager.getAll("col-1-999");
		assertEquals(0L, model.size());		
	}	

	@Test
	public void test_getAll_correctId_emptyList() {
		if (!doTest()) {
			return;
		}
		Model model = dataSourceManager.getAll("col-4");
		assertEquals(0L, model.size());
	}	

	@Test
	public void test_getAll_correctId_nonEmptyList() {
		if (!doTest()) {
			return;
		}
		
		Model model = dataSourceManager.getAll("col-1");
		assertEquals(4L, model.size());
		
		String baseUri = DrumbeatApplication.getInstance().getBaseUri();
		assertTrue(
				model.contains(
						model.createResource(baseUri + "datasources/col-1/dso-1-1"),
						RDF.type,
						DrumbeatOntology.LBDHO.DataSource));

		assertTrue(
				model.contains(
						model.createResource(baseUri + "datasources/col-1/dso-1-2"),
						RDF.type,
						DrumbeatOntology.LBDHO.DataSource));

		assertTrue(
				model.contains(
						model.createResource(baseUri + "datasources/col-1/dso-1-3"),
						RDF.type,
						DrumbeatOntology.LBDHO.DataSource));

		assertTrue(
				model.contains(
						model.createResource(baseUri + "datasources/col-1/dso-1-4"),
						RDF.type,
						DrumbeatOntology.LBDHO.DataSource));
	}
	

	/***************************************
	 * getById()
	 **************************************/
	@Test
	public void test_getById_correctId_nonEmptyList() {
		if (!doTest()) {
			return;
		}

		Model model = dataSourceManager.getById("col-1", "dso-1-1");
		
		assertTrue(model.size() >= 5L);
		
		String baseUri = DrumbeatApplication.getInstance().getBaseUri();
		Resource dataSourceResource = model.createResource(baseUri + "datasources/col-1/dso-1-1"); 
		
		assertEquals(
				DrumbeatOntology.LBDHO.DataSource,
				dataSourceResource.getProperty(RDF.type).getObject());

		assertEquals(
				"DataSource 1-1",
				dataSourceResource.getProperty(DrumbeatOntology.LBDHO.name).getLiteral().getValue());

		assertEquals(
				model.createResource(baseUri + "collections/col-1"),
				dataSourceResource.getProperty(DrumbeatOntology.LBDHO.inCollection).getObject());
		
		assertTrue(
				model.contains(
						dataSourceResource,
						DrumbeatOntology.LBDHO.hasDataSet,
						model.createResource(baseUri + "datasets/col-1/dso-1-1/dse-1-1-1")));
				
		
		assertTrue(
				model.contains(
						dataSourceResource,
						DrumbeatOntology.LBDHO.hasDataSet,
						model.createResource(baseUri + "datasets/col-1/dso-1-1/dse-1-1-2")));
	}
	
	
	@Test(expected=NotFoundException.class)
	public void test_getById_wrongId() {
		if (!doTest()) {
			return;
		}
		dataSourceManager.getById("col-1", "dso-1-999");
	}


	/***************************************
	 * create()
	 **************************************/

	@Test(expected=NotFoundException.class)
	public void test_create_wrongId() {
		if (!doTest()) {
			return;
		}
		dataSourceManager.create("col-999", "dso-999-1", "DataSource 1-999-1");
	}
	
	@Test(expected=AlreadyExistsException.class)
	public void test_create_correctId_alreadyExists() {
		if (!doTest()) {
			return;
		}
		dataSourceManager.create("col-1", "dso-1-1", "DataSource 1-1-1");
	}
	
	@Test
	public void test_create_correctId_new() {
		if (!doTest()) {
			return;
		}
		
		long oldSize = metaDataModel.size();
		
		Model model = dataSourceManager.create("col-3", "dso-3-1", "DataSource 3-1");
		
		assertEquals(1L,  model.size());
		
		Resource dataSourceResource = model.listSubjects().next();
		assertEquals(
				DrumbeatOntology.LBDHO.DataSource,
				dataSourceResource.getProperty(RDF.type).getObject());
		
		dataSourceResource = dataSourceResource.inModel(metaDataModel);
		
		
		assertEquals(oldSize + 4L, metaDataModel.size());
		
		String baseUri = DrumbeatApplication.getInstance().getBaseUri();
		assertEquals(
				DrumbeatOntology.LBDHO.DataSource,
				dataSourceResource.getProperty(RDF.type).getObject());
		assertEquals(
				metaDataModel.createResource(baseUri + "collections/col-3"),
				dataSourceResource.getProperty(DrumbeatOntology.LBDHO.inCollection).getObject());
		assertEquals(
				"DataSource 3-1",
				dataSourceResource.getProperty(DrumbeatOntology.LBDHO.name).getLiteral().getValue());
		
	}
	
	
	/***************************************
	 * delete()
	 **************************************/

	@Test(expected=NotFoundException.class)
	public void test_delete_wrongId() {
		if (!doTest()) {
			return;
		}
		dataSourceManager.delete("col-1", "dso-1-999");
	}
	
	@Test(expected=DeleteDeniedException.class)
	public void test_delete_correctId_hasChildren() {
		if (!doTest()) {
			return;
		}
		dataSourceManager.delete("col-2", "dso-2-1");
	}

	@Test
	public void test_delete_correctId_noChildren() {
		if (!doTest()) {
			return;
		}
		
		long oldSize = metaDataModel.size();

		Model model = dataSourceManager.create("col-4", "dso-4-1", "DataSource 4-1");
		
		assertEquals(1L,  model.size());
		
		Resource dataSourceResource = model.listSubjects().next();
		assertEquals(
				DrumbeatOntology.LBDHO.DataSource,
				dataSourceResource.getProperty(RDF.type).getObject());
		
		dataSourceResource = dataSourceResource.inModel(metaDataModel);
		
		
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
