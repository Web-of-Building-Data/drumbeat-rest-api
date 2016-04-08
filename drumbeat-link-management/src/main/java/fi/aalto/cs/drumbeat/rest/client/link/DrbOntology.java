package fi.aalto.cs.drumbeat.rest.client.link;

public class DrbOntology {

	public static final String DRUMBEAT_ONTOLOGY_NAMESPACE_FORMAT = "http://drumbeat.cs.hut.fi/owl/%s#";	

	public static String formatDrumbeatOntologyUri(String ontologyName) {
		return String.format(DRUMBEAT_ONTOLOGY_NAMESPACE_FORMAT, ontologyName);
	}
	
	public static class BLO {
		
		public static final String NAMESPACE_PREFIX = "blo";
		public static final String NAMESPACE_URI = formatDrumbeatOntologyUri("blo");
		
		public static final String PROPERTY_HAS_NEAR_SPACE = NAMESPACE_URI + "hasNearSpace";
		public static final String PROPERTY_IS_NEAR_SPACE_OF = NAMESPACE_URI + "isNearSpaceOf";
		
	}
	
	
}
