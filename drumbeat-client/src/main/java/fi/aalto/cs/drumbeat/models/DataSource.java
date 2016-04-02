package fi.aalto.cs.drumbeat.models;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.ClientBuilder;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;

import fi.aalto.cs.drumbeat.controllers.DrumbeatApplication;

public class DataSource extends Container {
	
	public DataSource(String uri) {
		super(uri);
	}

	public static final String PATH = "datasources";

	@Override
	public String getLocalPath() {
		return PATH;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getChildren() {
		
		final String response =
				ClientBuilder
					.newClient()
					.target(getBaseUri())
					.path(DataSet.PATH)
					.path(((Collection)getParent()).getId())
					.path(getId())
					.request(DrumbeatApplication.RDF_LANG_DEFAULT.getHeaderString())
					.get(String.class);
		
		final Model dataSetsModel = DrumbeatApplication.parseModel(response);
		
		final ResIterator resIterator = dataSetsModel.listSubjects();
		
		final List<DataSet> dataSets = new ArrayList<>();
		
		while (resIterator.hasNext()) {
			final String dataSetUri = resIterator.next().getURI();			
			final DataSet dataSet = new DataSet(dataSetUri);			
			dataSet.setParent(this);
			dataSets.add(dataSet);
		}			
		
		return (List<T>) dataSets;
	}
	

}
