package fi.aalto.cs.drumbeat.rest.managers;

import static org.junit.Assert.*;

import org.apache.log4j.Level;
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
import fi.aalto.cs.drumbeat.common.DrumbeatException;
import fi.aalto.cs.drumbeat.rdf.utils.RdfIOUtils;

public class Test_CollectionManager extends DrumbeatTest {
	
	private static final boolean DO_TEST = true;
	
	private static CollectionManager collectionManager;
	private static Model metaDataModel;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DrumbeatTest.setUpBeforeClass();
		metaDataModel = getApplication().getMetaDataModel();		
		String testDataFilePath = getApplication().getRealServerPath(TestApplication.TEST_RDF_META_DATA_FILE_PATH);
		RdfIOUtils.importRdfFileToJenaModel(metaDataModel, testDataFilePath);
		collectionManager = new CollectionManager();
	}

	public Test_CollectionManager() {
		super(DO_TEST);
	}
	
	
	/***************************************
	 * getAll()
	 * @throws DrumbeatException 
	 **************************************/
	
	@Test
	public void test_getAll_nonEmptyList() throws DrumbeatException {
		if (!doTest()) {
			return;
		}

		Model model = collectionManager.getAll();
		
		logModel(Level.ERROR, model);
		
		assertTrue(model.size() >= 4L);
		
		String baseUri = DrumbeatApplication.getInstance().getBaseUri();
		assertTrue(
				model.contains(
						model.createResource(baseUri + "collections/col-1"),
						RDF.type,
						DrumbeatOntology.LBDHO.Collection));

		assertTrue(
				model.contains(
						model.createResource(baseUri + "collections/col-2"),
						RDF.type,
						DrumbeatOntology.LBDHO.Collection));

		assertTrue(
				model.contains(
						model.createResource(baseUri + "collections/col-3"),
						RDF.type,
						DrumbeatOntology.LBDHO.Collection));

		assertTrue(
				model.contains(
						model.createResource(baseUri + "collections/col-4"),
						RDF.type,
						DrumbeatOntology.LBDHO.Collection));
	}
	

	/***************************************
	 * getById()
	 * @throws DrumbeatException 
	 * @throws NotFoundException 
	 **************************************/
	@Test
	public void test_getById_correctId_nonEmptyList() throws NotFoundException, DrumbeatException {
		if (!doTest()) {
			return;
		}

		Model model = collectionManager.getById("col-1");
		
		assertEquals(6L, model.size());
		
		String baseUri = DrumbeatApplication.getInstance().getBaseUri();
		Resource collectionResource = model.createResource(baseUri + "collections/col-1"); 
		
		assertEquals(
				DrumbeatOntology.LBDHO.Collection,
				collectionResource.getProperty(RDF.type).getObject());

		assertEquals(
				"Collection 1",
				collectionResource.getProperty(DrumbeatOntology.LBDHO.name).getLiteral().getValue());

		assertTrue(
				model.contains(
						collectionResource,
						DrumbeatOntology.LBDHO.hasDataSource,
						model.createResource(baseUri + "datasources/col-1/dso-1-1")));
		
		assertTrue(
				model.contains(
						collectionResource,
						DrumbeatOntology.LBDHO.hasDataSource,
						model.createResource(baseUri + "datasources/col-1/dso-1-2")));

		assertTrue(
				model.contains(
						collectionResource,
						DrumbeatOntology.LBDHO.hasDataSource,
						model.createResource(baseUri + "datasources/col-1/dso-1-3")));

		assertTrue(
				model.contains(
						collectionResource,
						DrumbeatOntology.LBDHO.hasDataSource,
						model.createResource(baseUri + "datasources/col-1/dso-1-4")));
	}
	
	
	@Test(expected=NotFoundException.class)
	public void test_getById_wrongId() throws NotFoundException, DrumbeatException {
		if (!doTest()) {
			return;
		}
		collectionManager.getById("col-999");
	}


	/***************************************
	 * create()
	 **************************************/

	@Test(expected=AlreadyExistsException.class)
	public void test_create_correctId_alreadyExists() {
		if (!doTest()) {
			return;
		}
		collectionManager.create("col-1", "Collection 1-1-1");
	}
	
	@Test
	public void test_create_correctId_new() {
		if (!doTest()) {
			return;
		}
		
		long oldSize = metaDataModel.size();
		
		Model model = collectionManager.create("col-998", "Collection 998");
		assertEquals(1L,  model.size());
		
		Resource collectionResource = model.listSubjects().next();
		assertEquals(
				DrumbeatOntology.LBDHO.Collection,
				collectionResource.getProperty(RDF.type).getObject());
		

		assertEquals(oldSize + 2L, metaDataModel.size());

		collectionResource = collectionResource.inModel(metaDataModel);		
		
		assertEquals(
				DrumbeatOntology.LBDHO.Collection,
				collectionResource.getProperty(RDF.type).getObject());

		assertEquals(
				"Collection 998",
				collectionResource.getProperty(DrumbeatOntology.LBDHO.name).getLiteral().getValue());
		
	}
	
	
	/***************************************
	 * delete()
	 **************************************/

	@Test(expected=NotFoundException.class)
	public void test_delete_wrongId() {
		if (!doTest()) {
			return;
		}
		collectionManager.delete("col-999");
	}
	
	@Test(expected=DeleteDeniedException.class)
	public void test_delete_correctId_hasChildren() {
		if (!doTest()) {
			return;
		}
		collectionManager.delete("col-2");
	}	

	@Test
	public void test_delete_correctId() {
		if (!doTest()) {
			return;
		}
		
		long oldSize = metaDataModel.size();

		Model model = collectionManager.create("col-997", "Collection 997");
		assertEquals(1L,  model.size());
		
		Resource collectionResource = model.listSubjects().next();
		assertEquals(
				DrumbeatOntology.LBDHO.Collection,
				collectionResource.getProperty(RDF.type).getObject());
		
		collectionResource = collectionResource.inModel(metaDataModel);
		
		String baseUri = DrumbeatApplication.getInstance().getBaseUri();
		assertEquals(baseUri + "collections/col-997", collectionResource.getURI());

		assertEquals(oldSize + 2L, metaDataModel.size());
		
		collectionManager.delete("col-997");
		
		assertEquals(oldSize, metaDataModel.size());
		
		assertFalse(
				metaDataModel.contains(collectionResource, null, (RDFNode)null));
				
		assertFalse(
				metaDataModel.contains(null, null, collectionResource));
		
	}
}
