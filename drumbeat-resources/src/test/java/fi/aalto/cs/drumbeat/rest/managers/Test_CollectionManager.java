package fi.aalto.cs.drumbeat.rest.managers;

import static org.junit.Assert.*;

import org.apache.log4j.Level;
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

public class Test_CollectionManager extends DrumbeatTest {
	
	private static final boolean DO_TEST = true;
	
	private static CollectionManager collectionManager;
	private static Model metaDataModel;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DrumbeatTest.setUpBeforeClass();
		metaDataModel = getApplication().getMetaDataModel();		
		String testDataFilePath = getApplication().getRealPath(TestApplication.TEST_RDF_META_DATA_FILE_PATH);
		RdfUtils.importRdfFileToJenaModel(metaDataModel, testDataFilePath);
		collectionManager = new CollectionManager();
	}

	public Test_CollectionManager() {
		super(DO_TEST);
	}
	
	
	/***************************************
	 * getAll()
	 **************************************/
	
	@Test
	public void test_getAll_nonEmptyList() {
		Model model = collectionManager.getAll();
		
		logModel(Level.ERROR, model);
		
		assertTrue(model.size() >= 4L);
		
		String baseUri = DrumbeatApplication.getInstance().getBaseUri();
		assertTrue(
				model.contains(
						model.createResource(baseUri + "collections/col-1"),
						RDF.type,
						LinkedBuildingDataOntology.Collection));

		assertTrue(
				model.contains(
						model.createResource(baseUri + "collections/col-2"),
						RDF.type,
						LinkedBuildingDataOntology.Collection));

		assertTrue(
				model.contains(
						model.createResource(baseUri + "collections/col-3"),
						RDF.type,
						LinkedBuildingDataOntology.Collection));

		assertTrue(
				model.contains(
						model.createResource(baseUri + "collections/col-4"),
						RDF.type,
						LinkedBuildingDataOntology.Collection));
	}
	

	/***************************************
	 * getById()
	 **************************************/
	@Test
	public void test_getById_correctId_nonEmptyList() {
		Model model = collectionManager.getById("col-1");
		
		assertEquals(6L, model.size());
		
		String baseUri = DrumbeatApplication.getInstance().getBaseUri();
		Resource collectionResource = model.createResource(baseUri + "collections/col-1"); 
		
		assertEquals(
				LinkedBuildingDataOntology.Collection,
				collectionResource.getProperty(RDF.type).getObject());

		assertEquals(
				"Collection 1",
				collectionResource.getProperty(LinkedBuildingDataOntology.name).getLiteral().getValue());

		assertTrue(
				model.contains(
						collectionResource,
						LinkedBuildingDataOntology.hasDataSource,
						model.createResource(baseUri + "datasources/col-1/dso-1-1")));
		
		assertTrue(
				model.contains(
						collectionResource,
						LinkedBuildingDataOntology.hasDataSource,
						model.createResource(baseUri + "datasources/col-1/dso-1-2")));

		assertTrue(
				model.contains(
						collectionResource,
						LinkedBuildingDataOntology.hasDataSource,
						model.createResource(baseUri + "datasources/col-1/dso-1-3")));

		assertTrue(
				model.contains(
						collectionResource,
						LinkedBuildingDataOntology.hasDataSource,
						model.createResource(baseUri + "datasources/col-1/dso-1-4")));
	}
	
	
	@Test(expected=NotFoundException.class)
	public void test_getById_wrongId() {
		collectionManager.getById("col-999");
	}


	/***************************************
	 * create()
	 **************************************/

	@Test(expected=AlreadyExistsException.class)
	public void test_create_correctId_alreadyExists() {
		collectionManager.create("col-1", "Collection 1-1-1");
	}
	
	@Test
	public void test_create_correctId_new() {
		
		long oldSize = metaDataModel.size();
		
		Resource collectionResource = collectionManager.create("col-998", "Collection 998");
		
		assertEquals(oldSize + 2L, metaDataModel.size());
		
		assertEquals(
				LinkedBuildingDataOntology.Collection,
				collectionResource.getProperty(RDF.type).getObject());

		assertEquals(
				"Collection 998",
				collectionResource.getProperty(LinkedBuildingDataOntology.name).getLiteral().getValue());
		
	}
	
	
	/***************************************
	 * delete()
	 **************************************/

	@Test(expected=NotFoundException.class)
	public void test_delete_wrongId() {
		collectionManager.delete("col-999");
	}
	

	@Test
	public void test_delete_correctId() {
		
		long oldSize = metaDataModel.size();

		Resource collectionResource = collectionManager.create("col-997", "Collection 997");
		
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
