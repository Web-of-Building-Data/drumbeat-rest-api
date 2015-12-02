package fi.aalto.cs.drumbeat.rest.ontology;

public class BuildingDataOntology {
	
	public static final String BASE_URL = "http://drumbeat.cs.hut.fi/owl/LDBHO#";
	
	public static class Collections {
		public static final String Collection = BASE_URL + "Collection";
		public static final String property_name = BASE_URL + "name";
		public static final String property_hasDataSources = BASE_URL + "hasDataSources";		
	}

	public static class DataSources {
		public static final String class_DataSource = BASE_URL + "DataSource";
		public static final String property_name = BASE_URL + "name";
		public static final String property_isDataSource = BASE_URL + "isDataSource";		
		public static final String property_hasDataSets = BASE_URL + "hasDataSets";		
	}
	
	public static class DataSets {
		public static final String class_DataSet = BASE_URL + "DataSet";
		public static final String property_isDataSet = BASE_URL + "isDataSet";		
		public static final String property_name = BASE_URL + "name";
	}

}
