package fi.aalto.cs.drumbeat.models;

import java.util.List;

import javax.ws.rs.client.ClientBuilder;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;

import fi.aalto.cs.drumbeat.common.DrumbeatOntology.LBDHO;
import fi.aalto.cs.drumbeat.controllers.DrumbeatApplication;

public abstract class Container implements Comparable<Container> {
	
	private String uri;
	private Model data;
	private Object parent;

	public Container(String uri) {
		this.uri = uri;
	}
	
	public abstract String getLocalPath();	

	public abstract <T> List<T> getChildren(); 

	public String getUri() {
		return uri;
	}
	
	public Model getData() {
		if (data == null) {			
			
			String response =
					ClientBuilder
						.newClient()
						.target(uri)
						.request(DrumbeatApplication.RDF_LANG_DEFAULT.getHeaderString())
						.get(String.class);
			
			data = DrumbeatApplication.parseModel(response);
		}
		return data;
	}
	
	public String getId() {
		return uri.substring(uri.lastIndexOf('/') + 1);
	}
	
	public String getName() {
		NodeIterator it = getData().listObjectsOfProperty(LBDHO.name);
		if (it.hasNext()) {
			return it.next().asLiteral().getString();
		} else {
			throw new NullPointerException("Property " + LBDHO.name + " is not found");
		}
	}
	
	public Object getParent() {
		return parent;
	}
	
	public void setParent(Object parent) {
		this.parent = parent;
	}
	
	@Override
	public String toString() {
		return String.format("%s [%s]", getName(), getId()) ;
	}	
	
	public String getBaseUri() {
		return uri.substring(0, uri.indexOf('/' + getLocalPath()));
	}

	
	@Override
	public int compareTo(Container o) {
		return uri.compareTo(o.uri);
	}

}
