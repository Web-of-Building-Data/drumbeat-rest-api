package fi.aalto.cs.drumbeat.rest.common;

import java.util.Map;
import java.util.TreeMap;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import fi.aalto.cs.drumbeat.rdf.RdfVocabulary;

public class DrumbeatOntology {
	
	public static final String GRAPH_NAME_IFC = "owl_ifc_ifc2x3";
	
	//	public static final String COLLECTION_OWL = "owl";
//	public static final String DATASOURCE_IFC = "ifc";
//	public static final String DATASET_IFC2X3 = "ifc2x3";
//	
//	public static final String DATASOURCE_IFC = "ifc";
	
	
	public static class LBDHO {
		
		public static final String ONTOLOGY_BASE_PREFIX = "lbdho";
		public static final String ONTOLOGY_BASE_URI = formatDrumbeatOntologyBaseUri("lbdho.ttl");
	
		public static final Resource Collection = RdfVocabulary.DEFAULT_MODEL.createResource(ONTOLOGY_BASE_URI + "Collection");	
		public static final Resource DataSet = RdfVocabulary.DEFAULT_MODEL.createResource(ONTOLOGY_BASE_URI + "DataSet");	
		public static final Resource DataSource = RdfVocabulary.DEFAULT_MODEL.createResource(ONTOLOGY_BASE_URI + "DataSource");	
		public static final Resource LinkSet = RdfVocabulary.DEFAULT_MODEL.createResource(ONTOLOGY_BASE_URI + "LinkSet");	
		public static final Resource LinkSource = RdfVocabulary.DEFAULT_MODEL.createResource(ONTOLOGY_BASE_URI + "LinkSource");	
		public static final Resource Ontology = RdfVocabulary.DEFAULT_MODEL.createResource(ONTOLOGY_BASE_URI + "Ontology");	
		
		public static final Property graphName = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "graphName");	
		public static final Property hasDataSet = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "hasDataSet");	
		public static final Property hasDataSource = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "hasDataSource");	
	//	public static final Property hasLinkSet = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "hasLinkSet");	
		public static final Property hasLastDataSet = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "hasLastDataSet");
	//	public static final Property hasLastLinkSet = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "hasLastLinkSet");
		public static final Property hasLinkSource = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "hasLinkSource");	
		public static final Property hasOriginalDataSource = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "hasOriginalDataSource");	
		public static final Property inCollection = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "inCollection");	
		public static final Property inDataSource = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "inDataSource");
	//	public static final Property inLinkSource = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "inLinkSource");
		public static final Property lastModified = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "lastModified");	
		public static final Property name = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "name");	
		public static final Property overwrites = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "overwrites");	
		public static final Property overwritingMethod = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "overwritingMethod");	
		public static final Property replaces = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "replaces");	
		public static final Property sizeInTriples = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "sizeInTriples");
		
	}
	
	
	public static class BLO {
		
		public static final String ONTOLOGY_BASE_PREFIX = "blo";
		public static final String ONTOLOGY_BASE_URI = formatDrumbeatOntologyBaseUri("blo");
		
		public static final Property implements1 = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "implements");	
		public static final Property isImplementedBy = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "isImplementedBy");
		
		public static final Property hasNearSpace = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "hasNearSpace");	
		public static final Property isNearSpaceOf = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "isNearSpaceOf");
		
	}
	
	public static class SEO {
		
		public static final String ONTOLOGY_BASE_PREFIX = "seo";
		public static final String ONTOLOGY_BASE_URI = formatDrumbeatOntologyBaseUri("seo");
		
		public static final Property object = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "object");	
		public static final Property event = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "event");	
		
	}

	@SuppressWarnings("serial")
	public static Map<String, String> getDefaultNsPrefixes() {
		final Map<String, String> map =
			new TreeMap<String, String>() {{
				put(LBDHO.ONTOLOGY_BASE_PREFIX, LBDHO.ONTOLOGY_BASE_URI);
				put(BLO.ONTOLOGY_BASE_PREFIX, BLO.ONTOLOGY_BASE_URI);				
				put(RdfVocabulary.OWL.BASE_PREFIX, OWL.getURI());
				put(RdfVocabulary.RDF.BASE_PREFIX, RDF.getURI());		
				put(RdfVocabulary.RDFS.BASE_PREFIX, RDFS.getURI());		
				put(RdfVocabulary.XSD.BASE_PREFIX, XSD.getURI());
				// TODO: get IFC URI from config file
				put("expr", formatDrumbeatOntologyBaseUri("EXPRESS"));
//				put("step", "http://drumbeat.cs.hut.fi/owl/STEP#");
				put("ifc", formatDrumbeatOntologyBaseUri("ifc2x3"));
//				put("ifc4", formatDrumbeatOntologyBaseUri("ifc4"));				
				put("ifc4", formatDrumbeatOntologyBaseUri("ifc4_add1"));				
			}};
		return map;
	}
	
	public static void fillParameterizedSparqlString(ParameterizedSparqlString pss) {
		pss.setBaseUri(DrumbeatApplication.getInstance().getBaseUri());
		pss.setNsPrefixes(getDefaultNsPrefixes());

//		pss.setIri(ONTOLOGY_BASE_PREFIX + "_Collection", LinkedBuildingDataOntology.Collection.getURI());
//		pss.setIri(ONTOLOGY_BASE_PREFIX + "_DataSet", LinkedBuildingDataOntology.DataSet.getURI());
//		pss.setIri(ONTOLOGY_BASE_PREFIX + "_DataSource", LinkedBuildingDataOntology.DataSource.getURI());
//		
//		pss.setIri(ONTOLOGY_BASE_PREFIX + "_hasDataSet", LinkedBuildingDataOntology.hasDataSet.getURI());
//		pss.setIri(ONTOLOGY_BASE_PREFIX + "_hasDataSource", LinkedBuildingDataOntology.hasDataSource.getURI());
//		pss.setIri(ONTOLOGY_BASE_PREFIX + "_name", LinkedBuildingDataOntology.name.getURI());
//		pss.setIri(ONTOLOGY_BASE_PREFIX + "_inCollection", LinkedBuildingDataOntology.inCollection.getURI());
//		pss.setIri(ONTOLOGY_BASE_PREFIX + "_inDataSource", LinkedBuildingDataOntology.inDataSource.getURI());
	}
	
	public static String formatDrumbeatOntologyBaseUri(String ontologyId) {
		return "http://drumbeat.cs.hut.fi/owl/" + ontologyId + "#";
	}
	
	
}
