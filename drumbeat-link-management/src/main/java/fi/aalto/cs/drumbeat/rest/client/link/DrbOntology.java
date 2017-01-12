package fi.aalto.cs.drumbeat.rest.client.link;

/**
 * DRUMBEAT's ontologies
 * 
 * @author vuhoan1
 *
 */
public class DrbOntology {

	/**
	 * Common namespace format of all DRUMBEAT ontologies
	 */
	public static final String DRUMBEAT_ONTOLOGY_NAMESPACE_FORMAT = "http://drumbeat.cs.hut.fi/owl/%s#";	

	/**
	 * Gets the URI for the ontology with the specified name 
	 * 
	 * @param ontologyName the ontology name
	 * 
	 * @return the ontology's URI
	 */
	public static String formatDrumbeatOntologyUri(String ontologyName) {
		return String.format(DRUMBEAT_ONTOLOGY_NAMESPACE_FORMAT, ontologyName);
	}
	
	/**
	 * Building Linking Ontology (BLO)
	 * 
	 */
	public static class BLO {
		
		public static final String NAMESPACE_PREFIX = "blo";
		public static final String NAMESPACE_URI = formatDrumbeatOntologyUri("blo");
		
		public static final String PROPERTY_HAS_NEAR_SPACE = NAMESPACE_URI + "hasNearSpace";
		public static final String PROPERTY_IS_NEAR_SPACE_OF = NAMESPACE_URI + "isNearSpaceOf";
		
	}
	
	
}
