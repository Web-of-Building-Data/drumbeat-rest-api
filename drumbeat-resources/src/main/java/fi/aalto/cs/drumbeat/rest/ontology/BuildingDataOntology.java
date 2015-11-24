package fi.aalto.cs.drumbeat.rest.ontology;

public class BuildingDataOntology {
	
	public static final String BASE_URL = "http://drumbeat.cs.hut.fi/owl/LDBHO#";
	
	public static class Collections {
		public static final String Collection = BASE_URL + "Collection";
		public static final String name = BASE_URL + "name";
		public static final String hasDataSources = BASE_URL + "hasDataSources";		
	}

	public static class Datasets {
		public static final String Dataset = BASE_URL + "Dataset";
		public static final String name = BASE_URL + "name";
	}

}
