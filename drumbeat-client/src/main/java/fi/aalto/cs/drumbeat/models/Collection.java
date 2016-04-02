package fi.aalto.cs.drumbeat.models;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.ClientBuilder;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;

import fi.aalto.cs.drumbeat.controllers.DrumbeatApplication;

public class Collection extends Container {
	
	public static final String PATH = "collections";
	
	public Collection(String uri) {
		super(uri);
	}

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
					.path(DataSource.PATH)
					.path(getId())
					.request(DrumbeatApplication.RDF_LANG_DEFAULT.getHeaderString())
					.get(String.class);
		
		final Model dataSourcesModel = DrumbeatApplication.parseModel(response);
		
		final ResIterator resIterator = dataSourcesModel.listSubjects();
		
		final List<DataSource> dataSources = new ArrayList<>();
		
		while (resIterator.hasNext()) {
			final String dataSourceUri = resIterator.next().getURI();			
			final DataSource dataSource = new DataSource(dataSourceUri);
			dataSource.setParent(this);
			dataSources.add(dataSource);
		}			
		
		return (List<T>) dataSources;
	}


}
