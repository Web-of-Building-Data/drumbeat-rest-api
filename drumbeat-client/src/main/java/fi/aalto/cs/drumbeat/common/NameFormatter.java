package fi.aalto.cs.drumbeat.common;

public class NameFormatter {
	
	
	public static String formatCollectionResourceUri(String baseUri, String collectionId) {
		return String.format(
						"%scollections/%s",
						baseUri,
						collectionId);
	}
	
	public static String formatDataSourceResourceUri(String baseUri, String collectionId, String dataSourceId) {		
		return String.format(
					"%sdatasources/%s/%s",
					baseUri,
					collectionId,
					dataSourceId);
	}

	public static String formatLinkSourceResourceUri(String baseUri, String collectionId, String linkSourceId) {
		return formatDataSourceResourceUri(baseUri, collectionId, linkSourceId);
	}

	public static String formatDataSetResourceUri(String baseUri, String collectionId, String dataSourceId, String dataSetId) {
		return String.format(
				"%sdatasets/%s/%s/%s",
				baseUri,
				collectionId,
				dataSourceId,
				dataSetId);
	}
		
	public static String formatLinkSetResourceUri(String baseUri, String collectionId, String linkSourceId, String linkSetId) {
		return formatDataSetResourceUri(baseUri, collectionId, linkSourceId, linkSetId);
	}

	public static String formatObjectResourceBaseUri(String baseUri, String collectionId, String dataSourceId) {
		return String.format(
				"%sobjects/%s/%s/",
				baseUri,
				collectionId,
				dataSourceId);
	}

	public static String formatObjectResourceUri(String baseUri, String collectionId, String dataSourceId, String objectId) {
		return formatObjectResourceBaseUri(baseUri, collectionId, dataSourceId) + objectId;
	}

	public static String formatLocalOntologyUri(String baseUri, String ontolgoyId)
	{
		return String.format(
				"%sowl/%s",
				baseUri,
				ontolgoyId);
	}
	
	public static String formatLocalOntologyBaseUri(String baseUri, String ontologyId)
	{
		return formatLocalOntologyUri(baseUri, ontologyId) + "#";
	}

	public static String formatDataSetGraphUri(String baseUri, String collectionId, String dataSourceId, String dataSetId)
	{
		return formatDataSetResourceUri(baseUri, collectionId, dataSourceId, dataSetId);
	}
	
	public static String formatLinkSetGraphUri(String baseUri, String collectionId, String dataSourceId, String dataSetId)
	{
		return formatLinkSetResourceUri(baseUri, collectionId, dataSourceId, dataSetId);
	}
	
	public static String formatBackLinkSourceUri(String baseUri, String collectionId, String originalDataSourceId) {
		return formatDataSourceResourceUri(baseUri, collectionId, originalDataSourceId + "_BACK_LINKS");
	}
	
	public static String formatBackLinkSourceUri(String dataSourceUri) {
		return dataSourceUri + "_BACK_LINKS";
	}

	public static String getDataSourceUriFromObjectUri(String objectUri) {
		String baseUri = objectUri.substring(0, objectUri.indexOf("objects/")); 		
		
		objectUri = objectUri.substring(baseUri.length() + "objects/".length());
		
		String[] tokens = objectUri.split("/");
		if (tokens.length != 3) {
			throw new IllegalArgumentException("Invalid object URI: " + objectUri);
		}
		
		String dataSourceId = tokens[1];
		String collectionId = tokens[0];
		
		return formatDataSourceResourceUri(baseUri, collectionId, dataSourceId);
	}
	
	
	

}
