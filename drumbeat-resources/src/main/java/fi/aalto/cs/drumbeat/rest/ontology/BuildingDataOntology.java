package fi.aalto.cs.drumbeat.rest.ontology;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatWebApplication;

public class BuildingDataOntology {
	
	public static final String ONTOLOGY_BASE_URL = "http://drumbeat.cs.hut.fi/owl/lbdho.ttl#";
	
	public static class Collections {
		public static final String Collection = ONTOLOGY_BASE_URL + "Collection";
		public static final String name = ONTOLOGY_BASE_URL + "name";
		public static final String hasDataSource = ONTOLOGY_BASE_URL + "hasDataSource";
		
		
		public static String formatUrl(String collectionId) {
			return String.format(
						"%scollections/%s",
						DrumbeatWebApplication.getInstance().getBaseUri(),
						collectionId);
		}

	}

	public static class DataSources {
		public static final String DataSource = ONTOLOGY_BASE_URL + "DataSource";
		public static final String name = ONTOLOGY_BASE_URL + "name";
		public static final String inCollection = ONTOLOGY_BASE_URL + "inCollection";		
		public static final String hasDataSet = ONTOLOGY_BASE_URL + "hasDataSet";		

		public static String formatUrl(String collectionId, String dataSourceId) {
			
			return String.format(
					"%sdatasources/%s/%s",
					DrumbeatWebApplication.getInstance().getBaseUri(),
					collectionId,
					dataSourceId);
		}

	}
	
	public static class DataSets {
		public static final String DataSet = ONTOLOGY_BASE_URL + "DataSet";
		public static final String inDataSource = ONTOLOGY_BASE_URL + "inDataSource";		
		public static final String name = ONTOLOGY_BASE_URL + "name";
		
		public static String formatUrl(String collectionId, String dataSourceId, String dataSetId) {
			return String.format(
					"%sdatasets/%s/%s/%s",
					DrumbeatWebApplication.getInstance().getBaseUri(),
					collectionId,
					dataSourceId,
					dataSetId);
		}
	}

}
