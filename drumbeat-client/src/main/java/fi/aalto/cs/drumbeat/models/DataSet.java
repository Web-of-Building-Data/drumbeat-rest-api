package fi.aalto.cs.drumbeat.models;

import java.util.List;

public class DataSet extends Container {
	
	public static final String PATH = "datasets";

	public DataSet(String uri) {
		super(uri);
	}

	@Override
	public String getLocalPath() {
		return PATH;
	}

	@Override
	public <T> List<T> getChildren() {
		return null;
	}
	

}
