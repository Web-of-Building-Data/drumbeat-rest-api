package fi.aalto.cs.drumbeat.models;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.ClientBuilder;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;

import fi.aalto.cs.drumbeat.controllers.DrumbeatApplication;

public class Server {
	
	private final String baseUri;
	
	public Server(String baseUri) {
		this.baseUri = baseUri;
	}
	
	public List<Collection> getCollections() {
		
		final String response =
				ClientBuilder
					.newClient()
					.target(baseUri)
					.path(Collection.PATH)
					.request(DrumbeatApplication.RDF_LANG_DEFAULT.getHeaderString())
					.get(String.class);
		
		final Model collectionsModel = DrumbeatApplication.parseModel(response);
		
		final ResIterator resIterator = collectionsModel.listSubjects();
		
		final List<Collection> collections = new ArrayList<>();
		
		while (resIterator.hasNext()) {
			final String collectionUri = resIterator.next().getURI();			
			final Collection collection = new Collection(collectionUri);			
			collections.add(collection);
		}			
		
		return collections;
		
	}
	
}
