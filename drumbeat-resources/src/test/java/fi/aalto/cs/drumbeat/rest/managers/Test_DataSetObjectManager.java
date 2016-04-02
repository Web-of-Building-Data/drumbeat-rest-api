package fi.aalto.cs.drumbeat.rest.managers;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.AlreadyExistsException;
import org.apache.jena.shared.NotFoundException;
import org.apache.jena.vocabulary.RDF;

import fi.aalto.cs.drumbeat.rest.DrumbeatTest;
import fi.aalto.cs.drumbeat.rest.application.TestApplication;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatOntology;
import fi.aalto.cs.drumbeat.common.DrumbeatException;
import fi.aalto.cs.drumbeat.rdf.utils.RdfIOUtils;

public class Test_DataSetObjectManager extends DrumbeatTest {
	
	private static final boolean DO_TEST = true;
	
	private static DataSetObjectManager dataSetObjectManager;
	private static Model metaDataModel;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DrumbeatTest.setUpBeforeClass();
		metaDataModel = getApplication().getMetaDataModel();		
		String testDataFilePath = getApplication().getRealServerPath(TestApplication.TEST_RDF_META_DATA_FILE_PATH);
		RdfIOUtils.importRdfFileToJenaModel(metaDataModel, testDataFilePath);
		
		
		dataSetObjectManager = new DataSetObjectManager();
	}

	public Test_DataSetObjectManager() {
		super(DO_TEST);
	}
	
	
	/***************************************
	 * getAll()
	 * @throws DrumbeatException 
	 * @throws NotFoundException 
	 **************************************/
//	@Test
//	public void test_getAll() throws NotFoundException, DrumbeatException {
//		dataSetObjectManager.getAll("col-1", "dso-1-1", "dse-1-1-1");
//	}
}
