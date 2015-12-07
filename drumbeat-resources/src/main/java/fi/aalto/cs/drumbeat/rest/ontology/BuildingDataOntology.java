package fi.aalto.cs.drumbeat.rest.ontology;

import fi.aalto.cs.drumbeat.rest.application.DrumbeatApplication;

public class BuildingDataOntology {
	
	public static final String BASE_URL = "http://drumbeat.cs.hut.fi/owl/LDBHO#";
	
	public static class Collections {
		public static final String Collection = BASE_URL + "Collection";
		public static final String name = BASE_URL + "name";
		public static final String hasDataSources = BASE_URL + "hasDataSources";
		
		
		public static String formatUrl(String collectionId) {
			return String.format(
						"%scollections/%s",
						DrumbeatApplication.getInstance().getBaseUri(),
						collectionId);
		}
		
	}

	public static class DataSources {
		public static final String DataSource = BASE_URL + "DataSource";
		public static final String name = BASE_URL + "name";
		public static final String isDataSource = BASE_URL + "isDataSource";		
		public static final String hasDataSets = BASE_URL + "hasDataSets";		

		public static String formatUrl(String collectionId, String dataSourceId) {
			return String.format(
					"%sdatasources/%s/%s",
					DrumbeatApplication.getInstance().getBaseUri(),
					collectionId,
					dataSourceId);
		}

	}
	
	public static class DataSets {
		public static final String DataSet = BASE_URL + "DataSet";
		public static final String isDataSet = BASE_URL + "isDataSet";		
		public static final String name = BASE_URL + "name";
		
		public static String formatUrl(String collectionId, String dataSourceId, String dataSetId) {
			return String.format(
					"%sdatasets/%s/%s/%s",
					DrumbeatApplication.getInstance().getBaseUri(),
					collectionId,
					dataSourceId,
					dataSetId);
		}
	}

}
