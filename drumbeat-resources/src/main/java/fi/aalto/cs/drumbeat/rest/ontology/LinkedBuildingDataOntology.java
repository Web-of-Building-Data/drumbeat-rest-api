package fi.aalto.cs.drumbeat.rest.ontology;

import java.util.Map;
import java.util.TreeMap;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rdf.RdfVocabulary;

public class LinkedBuildingDataOntology {
	
	public static final String ONTOLOGY_BASE_PREFIX = "lbdho";
	public static final String ONTOLOGY_BASE_URI = formatDrumbeatOntologyBaseUri("lbdho.ttl");
	
	public static final String GRAPH_NAME_IFC = "owl_ifc_ifc2x3";
	
//	public static final String COLLECTION_OWL = "owl";
//	public static final String DATASOURCE_IFC = "ifc";
//	public static final String DATASET_IFC2X3 = "ifc2x3";
//	
//	public static final String DATASOURCE_IFC = "ifc";
	
	
	public static final Resource Collection = RdfVocabulary.DEFAULT_MODEL.createResource(ONTOLOGY_BASE_URI + "Collection");	
	public static final Resource DataSet = RdfVocabulary.DEFAULT_MODEL.createResource(ONTOLOGY_BASE_URI + "DataSet");	
	public static final Resource DataSource = RdfVocabulary.DEFAULT_MODEL.createResource(ONTOLOGY_BASE_URI + "DataSource");	
	public static final Resource Ontology = RdfVocabulary.DEFAULT_MODEL.createResource(ONTOLOGY_BASE_URI + "Ontology");	
	
	public static final Property graphName = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "graphName");	
	public static final Property hasDataSet = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "hasDataSet");	
	public static final Property hasDataSource = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "hasDataSource");	
	public static final Property hasLastDataSet = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "hasLastDataSet");
	public static final Property inCollection = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "inCollection");	
	public static final Property inDataSource = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "inDataSource");
	public static final Property lastModified = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "lastModified");	
	public static final Property name = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "name");	
	public static final Property overwrites = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "overwrites");	
	public static final Property overwritingMethod = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "overwritingMethod");	
	public static final Property replaces = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "replaces");	
	public static final Property sizeInTriples = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "sizeInTriples");
	
	@SuppressWarnings("serial")
	public static Map<String, String> getDefaultNsPrefixes() {
		final Map<String, String> map =
			new TreeMap<String, String>() {{
				put(ONTOLOGY_BASE_PREFIX, ONTOLOGY_BASE_URI);
				put(RdfVocabulary.OWL.BASE_PREFIX, OWL.getURI());
				put(RdfVocabulary.RDF.BASE_PREFIX, RDF.getURI());		
				put(RdfVocabulary.RDFS.BASE_PREFIX, RDFS.getURI());		
				put(RdfVocabulary.XSD.BASE_PREFIX, XSD.getURI());
				// TODO: get IFC URI from config file
				put("expr", formatDrumbeatOntologyBaseUri("EXPRESS"));
//				put("step", "http://drumbeat.cs.hut.fi/owl/STEP#");
				put("ifc", formatDrumbeatOntologyBaseUri("ifc2x3"));
//				put("ifc4", "http://drumbeat.cs.hut.fi/owl/IFC2X3#");				
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
	
	public static String formatCollectionResourceUri(String collectionId) {
		return String.format(
						"%scollections/%s",
						DrumbeatApplication.getInstance().getBaseUri(),
						collectionId);
	}
	
	public static String formatDataSourceResourceUri(String collectionId, String dataSourceId) {		
		return String.format(
					"%sdatasources/%s/%s",
					DrumbeatApplication.getInstance().getBaseUri(),
					collectionId,
					dataSourceId);
	}

	public static String formatDataSetResourceUri(String collectionId, String dataSourceId, String dataSetId) {
		return String.format(
				"%sdatasets/%s/%s/%s",
				DrumbeatApplication.getInstance().getBaseUri(),
				collectionId,
				dataSourceId,
				dataSetId);
	}
		
	public static String formatObjectResourceBaseUri(String collectionId, String dataSourceId) {
		return String.format(
				"%sobjects/%s/%s/",
				DrumbeatApplication.getInstance().getBaseUri(),
				collectionId,
				dataSourceId);
	}

	public static String formatObjectResourceUri(String collectionId, String dataSourceId, String objectId) {
		return formatObjectResourceBaseUri(collectionId, dataSourceId) + objectId;
	}

	public static String formatLocalOntologyUri(String ontolgoyId)
	{
		return String.format(
				"%sowl/%s",
				DrumbeatApplication.getInstance().getBaseUri(),
				ontolgoyId);
	}

	public static String formatLocalOntologyBaseUri(String ontologyId)
	{
		return formatLocalOntologyUri(ontologyId) + "#";
	}

	public static String formatDataSetGraphUri(String collectionId, String dataSourceId, String dataSetId)
	{
		return formatDataSetResourceUri(collectionId, dataSourceId, dataSetId);
	}
	
	public static String formatDrumbeatOntologyBaseUri(String ontologyId) {
		return "http://drumbeat.cs.hut.fi/owl/" + ontologyId + "#";
	}
	
}
