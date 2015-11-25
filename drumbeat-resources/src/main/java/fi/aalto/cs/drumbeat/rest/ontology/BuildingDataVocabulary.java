package fi.aalto.cs.drumbeat.rest.ontology;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class BuildingDataVocabulary {
	
	public static final Model DEFAULT_MODEL = ModelFactory.createDefaultModel();
	
	public static final String BASE_URL = "http://drumbeat.cs.hut.fi/owl/LDBHO#";
	
	
	public static final Resource BimModel = DEFAULT_MODEL.createResource(BASE_URL + "BimModel");
	public static final Resource Collection = DEFAULT_MODEL.createResource(BASE_URL + "Collection");
	public static final Resource ContainerSource = DEFAULT_MODEL.createResource(BASE_URL + "ContainerSource");	
	public static final Resource DataSource = DEFAULT_MODEL.createResource(BASE_URL + "DataSource");
	public static final Resource DataFormat = DEFAULT_MODEL.createResource(BASE_URL + "DataFormat");
	public static final Resource DataOrigin = DEFAULT_MODEL.createResource(BASE_URL + "DataOrigin");
	public static final Resource DataSet = DEFAULT_MODEL.createResource(BASE_URL + "DataSet");
	public static final Resource EventContainer = DEFAULT_MODEL.createResource(BASE_URL + "EventContainer");
	public static final Resource LinkContainer = DEFAULT_MODEL.createResource(BASE_URL + "LinkContainer");	
	

	public static final Property description = DEFAULT_MODEL.createProperty(BASE_URL + "description");
	public static final Property hasDataSource = DEFAULT_MODEL.createProperty(BASE_URL + "hasDataSource");
	public static final Property hasDataSet = DEFAULT_MODEL.createProperty(BASE_URL + "hasDataSet");
	public static final Property name = DEFAULT_MODEL.createProperty(BASE_URL + "name");		


}
