package fi.aalto.cs.drumbeat.common;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class DrumbeatOntology {
	
	public static final Model DEFAULT_MODEL = ModelFactory.createDefaultModel();

	public static final String GRAPH_NAME_IFC = "owl_ifc_ifc2x3";
	
//	public static final String COLLECTION_OWL = "owl";
//	public static final String DATASOURCE_IFC = "ifc";
//	public static final String DATASET_IFC2X3 = "ifc2x3";
//	
//	public static final String DATASOURCE_IFC = "ifc";
	
	
	public static class LBDHO {
		
		public static final String ONTOLOGY_BASE_PREFIX = "lbdho";
		public static final String ONTOLOGY_BASE_URI = formatDrumbeatOntologyBaseUri("lbdho.ttl");
	
		public static final Resource Collection = DEFAULT_MODEL.createResource(ONTOLOGY_BASE_URI + "Collection");	
		public static final Resource DataSet = DEFAULT_MODEL.createResource(ONTOLOGY_BASE_URI + "DataSet");	
		public static final Resource DataSource = DEFAULT_MODEL.createResource(ONTOLOGY_BASE_URI + "DataSource");	
		public static final Resource LinkSet = DEFAULT_MODEL.createResource(ONTOLOGY_BASE_URI + "LinkSet");	
		public static final Resource LinkSource = DEFAULT_MODEL.createResource(ONTOLOGY_BASE_URI + "LinkSource");	
		public static final Resource Ontology = DEFAULT_MODEL.createResource(ONTOLOGY_BASE_URI + "Ontology");	
		
		public static final Property graphName = DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "graphName");	
		public static final Property hasDataSet = DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "hasDataSet");	
		public static final Property hasDataSource = DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "hasDataSource");	
	//	public static final Property hasLinkSet = DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "hasLinkSet");	
		public static final Property hasLastDataSet = DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "hasLastDataSet");
	//	public static final Property hasLastLinkSet = DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "hasLastLinkSet");
		public static final Property hasLinkSource = DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "hasLinkSource");	
		public static final Property hasOriginalDataSource = DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "hasOriginalDataSource");	
		public static final Property inCollection = DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "inCollection");	
		public static final Property inDataSource = DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "inDataSource");
	//	public static final Property inLinkSource = DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "inLinkSource");
		public static final Property lastModified = DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "lastModified");	
		public static final Property name = DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "name");	
		public static final Property overwrites = DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "overwrites");	
		public static final Property overwritingMethod = DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "overwritingMethod");	
		public static final Property replaces = DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "replaces");	
		public static final Property sizeInTriples = DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "sizeInTriples");
		
	}
	
	
	public static class BLO {
		
		public static final String ONTOLOGY_BASE_PREFIX = "blo";
		public static final String ONTOLOGY_BASE_URI = formatDrumbeatOntologyBaseUri("blo");
		
		public static final Property implements1 = DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "implements");	
		public static final Property isImplementedBy = DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "isImplementedBy");	
		
	}
	
	public static class SEO {
		
		public static final String ONTOLOGY_BASE_PREFIX = "seo";
		public static final String ONTOLOGY_BASE_URI = formatDrumbeatOntologyBaseUri("seo");
		
		public static final Property object = DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "object");	
		public static final Property event = DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "event");	
		
	}

//	@SuppressWarnings("serial")
//	public static Map<String, String> getDefaultNsPrefixes() {
//		final Map<String, String> map =
//			new TreeMap<String, String>() {{
//				put(LBDHO.ONTOLOGY_BASE_PREFIX, LBDHO.ONTOLOGY_BASE_URI);
//				put(BLO.ONTOLOGY_BASE_PREFIX, BLO.ONTOLOGY_BASE_URI);				
//				put("owl", OWL.getURI());
//				put("rdf", RDF.getURI());		
//				put("rdfs", RDFS.getURI());		
//				put("xsd", XSD.getURI());
//				// TODO: get IFC URI from config file
//				put("expr", formatDrumbeatOntologyBaseUri("EXPRESS"));
////				put("step", "http://drumbeat.cs.hut.fi/owl/STEP#");
//				put("ifc", formatDrumbeatOntologyBaseUri("ifc2x3"));
//				put("ifc4", formatDrumbeatOntologyBaseUri("ifc4"));				
//				put("ifc4_add1", formatDrumbeatOntologyBaseUri("ifc4_add1"));				
//			}};
//		return map;
//	}
//	
	public static String formatDrumbeatOntologyBaseUri(String ontologyId) {
		return "http://drumbeat.cs.hut.fi/owl/" + ontologyId + "#";
	}
	
	
}
