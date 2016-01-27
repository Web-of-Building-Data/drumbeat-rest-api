package fi.aalto.cs.drumbeat.rest.common;


import com.hp.hpl.jena.rdf.model.Property;

import fi.aalto.cs.drumbeat.rdf.RdfVocabulary;

public class BimLinkingOntology {
	
	public static final String ONTOLOGY_BASE_PREFIX = "blo";
	public static final String ONTOLOGY_BASE_URI = LinkedBuildingDataOntology.formatDrumbeatOntologyBaseUri("blo.ttl");
	
	public static final Property implements1 = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "implements");	
	public static final Property implementedBy = RdfVocabulary.DEFAULT_MODEL.createProperty(ONTOLOGY_BASE_URI + "implementedBy");	
	
	
	
}
