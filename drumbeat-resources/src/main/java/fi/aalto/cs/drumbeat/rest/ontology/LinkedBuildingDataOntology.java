package fi.aalto.cs.drumbeat.rest.ontology;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.hut.cs.drumbeat.rdf.RdfVocabulary;

public class LinkedBuildingDataOntology {
	
	public static final String ONTOLOGY_BASE_PREFIX = "lbdho";
	public static final String ONTOLOGY_BASE_URI = "http://drumbeat.cs.hut.fi/owl/lbdho.ttl#";
	
	public static final Resource Collection = RdfVocabulary.DEFAULT_MODEL.createResource(ONTOLOGY_BASE_URI + "Collection");	
	public static final Resource DataSet = RdfVocabulary.DEFAULT_MODEL.createResource(ONTOLOGY_BASE_URI + "DataSet");	
	public static final Resource DataSource = RdfVocabulary.DEFAULT_MODEL.createResource(ONTOLOGY_BASE_URI + "DataSource");	
	
	public static final Property hasDataSet = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "hasDataSet");	
	public static final Property hasDataSource = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "hasDataSource");	
	public static final Property name = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "name");	
	public static final Property inCollection = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "inCollection");	
	public static final Property inDataSource = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "inDataSource");
	
	@SuppressWarnings("serial")
	public static Map<String, String> getDefaultNsPrefixes() {
		return new TreeMap<String, String>() {{
			put("ldbho", LinkedBuildingDataOntology.ONTOLOGY_BASE_URI);
			put("rdf", RDF.getURI());		
			put("owl", OWL.getURI());			
		}};
	}
	
	public static void fillParameterizedSparqlString(ParameterizedSparqlString pss) {
		pss.setBaseUri(DrumbeatApplication.getInstance().getBaseUri());
		pss.setNsPrefixes(getDefaultNsPrefixes());

		pss.setIri(ONTOLOGY_BASE_PREFIX + "_Collection", LinkedBuildingDataOntology.Collection.getURI());
		pss.setIri(ONTOLOGY_BASE_PREFIX + "_DataSet", LinkedBuildingDataOntology.DataSet.getURI());
		pss.setIri(ONTOLOGY_BASE_PREFIX + "_DataSource", LinkedBuildingDataOntology.DataSource.getURI());
		
		pss.setIri(ONTOLOGY_BASE_PREFIX + "_hasDataSet", LinkedBuildingDataOntology.hasDataSet.getURI());
		pss.setIri(ONTOLOGY_BASE_PREFIX + "_hasDataSource", LinkedBuildingDataOntology.hasDataSource.getURI());
		pss.setIri(ONTOLOGY_BASE_PREFIX + "_name", LinkedBuildingDataOntology.name.getURI());
		pss.setIri(ONTOLOGY_BASE_PREFIX + "_inCollection", LinkedBuildingDataOntology.inCollection.getURI());
		pss.setIri(ONTOLOGY_BASE_PREFIX + "_inDataSource", LinkedBuildingDataOntology.inDataSource.getURI());
	}
	
	public static String formatCollectionResourceUri(String hostBaseUri, String collectionId) {
		return String.format(
						"%scollections/%s",
						hostBaseUri,
						collectionId);
	}
	
	public static String formatDataSourceResourceUri(String hostBaseUri, String collectionId, String dataSourceId) {		
		return String.format(
					"%sdatasources/%s/%s",
					hostBaseUri,
					collectionId,
					dataSourceId);
	}

	public static String formatDataSetResourceUri(String hostBaseUri, String collectionId, String dataSourceId, String dataSetId) {
		return String.format(
				"%sdatasets/%s/%s/%s",
				hostBaseUri,
				collectionId,
				dataSourceId,
				dataSetId);
	}
		
	public static String formatDataSetName(String collectionId, String dataSourceId, String dataSetId)
	{
		return String.format("%s_%s_%s", collectionId, dataSourceId, dataSetId);
	}

}
