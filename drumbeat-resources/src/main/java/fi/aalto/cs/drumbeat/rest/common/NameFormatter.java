package fi.aalto.cs.drumbeat.rest.common;

public class NameFormatter {
	
	
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

	public static String formatLinkSourceResourceUri(String collectionId, String linkSourceId) {
		return formatDataSourceResourceUri(collectionId, linkSourceId);
	}

	public static String formatDataSetResourceUri(String collectionId, String dataSourceId, String dataSetId) {
		return String.format(
				"%sdatasets/%s/%s/%s",
				DrumbeatApplication.getInstance().getBaseUri(),
				collectionId,
				dataSourceId,
				dataSetId);
	}
		
	public static String formatLinkSetResourceUri(String collectionId, String linkSourceId, String linkSetId) {
		return formatDataSetResourceUri(collectionId, linkSourceId, linkSetId);
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
	
	public static String formatLinkSetGraphUri(String collectionId, String dataSourceId, String dataSetId)
	{
		return formatLinkSetResourceUri(collectionId, dataSourceId, dataSetId);
	}
	
	public static String formatBackLinkSourceUri(String collectionId, String originalDataSourceId) {
		return formatDataSourceResourceUri(collectionId, originalDataSourceId + "_BACK_LINKS");
	}
	
	public static String formatBackLinkSourceUri(String dataSourceUri) {
		return dataSourceUri + "_BACK_LINKS";
	}

	public static String getDataSourceUriFromObjectUri(String objectUri) {
		String baseUri = DrumbeatApplication.getInstance().getBaseUri() + "objects/";
		
		if (!objectUri.startsWith(baseUri)) {
			throw new IllegalArgumentException("Invalid object base URI: " + objectUri);			
		}
		
		objectUri = objectUri.substring(baseUri.length());
		
		String[] tokens = objectUri.split("/");
		if (tokens.length != 3) {
			throw new IllegalArgumentException("Invalid object URI: " + objectUri);
		}
		
		String dataSourceId = tokens[1];
		String collectionId = tokens[0];
		
		return formatDataSourceResourceUri(collectionId, dataSourceId);
	}
	
	
	

}
