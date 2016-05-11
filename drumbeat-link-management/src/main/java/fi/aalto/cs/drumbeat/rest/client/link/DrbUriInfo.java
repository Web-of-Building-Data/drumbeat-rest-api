package fi.aalto.cs.drumbeat.rest.client.link;

public class DrbUriInfo {
	
	public static final String COLLECTIONS = "/collections/";
	public static final String DATA_SOURCES = "/datasources/";
	public static final String DATA_SETS = "/datasets/";
	public static final String OBJECTS = "/objects/";
	
	private final String uri; 
	private String baseUri;
	private String collectionId;
	private String dataSourceId;
	private String dataSetId;
	
	public DrbUriInfo(String uri) {
		
		this.uri = uri;
		
		int index;
//		String tail;
		String tokens[];
		
		if ((index = uri.indexOf(COLLECTIONS)) != -1) {
			
			baseUri = uri.substring(0, index);
			tokens = uri.substring(index + COLLECTIONS.length()).split("/");
			if (tokens.length == 1) {
				collectionId = tokens[0];
			} else {
				throw new IllegalArgumentException("Invalid collectionUri: " + uri);
			}
					
			
		} else if ((index = uri.indexOf(DATA_SOURCES)) != -1) {

			baseUri = uri.substring(0, index);
			tokens = uri.substring(index + DATA_SOURCES.length()).split("/");
			if (tokens.length == 2) {
				collectionId = tokens[0];
				dataSourceId = tokens[1];
			} else {
				throw new IllegalArgumentException("Invalid dataSourceUri: " + uri);
			}
			
		} else if ((index = uri.indexOf(DATA_SETS)) != -1) {
			
			baseUri = uri.substring(0, index);
			tokens = uri.substring(index + DATA_SETS.length()).split("/");
			if (tokens.length == 3) {
				collectionId = tokens[0];
				dataSourceId = tokens[1];
				dataSetId = tokens[2];
			} else {
				throw new IllegalArgumentException("Invalid dataSetUri: " + uri);
			}			

		} else if ((index = uri.indexOf(OBJECTS)) != -1) {
			
			baseUri = uri.substring(0, index);
			tokens = uri.substring(index + OBJECTS.length()).split("/");
			if (tokens.length == 3) {
				collectionId = tokens[0];
				dataSourceId = tokens[1];
			} else {
				throw new IllegalArgumentException("Invalid objectUri: " + uri);
			}
		} 
		
	}
	
	public String getBaseObjectUri() {
		return baseUri + OBJECTS + collectionId + "/" + dataSourceId + "/";
	}
	
	
	public String formatObjectUri(String objectId) {
		return getBaseObjectUri() + objectId; 
	}

	public String getUri() {
		return uri;
	}
	
	public String getBaseUri() {
		return baseUri;
	}
	
	public String getCollectionId() {
		return collectionId;
	}
	
	public String getDataSourceId() {
		return dataSourceId;
	}
	
	public String getDataSetId() {
		return dataSetId;
	}
	
}
