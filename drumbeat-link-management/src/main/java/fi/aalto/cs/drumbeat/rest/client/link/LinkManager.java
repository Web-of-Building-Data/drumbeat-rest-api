package fi.aalto.cs.drumbeat.rest.client.link;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class LinkManager 
{
	
	public static final String PATH_UPLOAD_CONTENT = "uploadContent";
	public static final String PARAM_DATA_TYPE = "dataType";
	public static final String PARAM_DATA_FORMAT = "dataFormat";
	public static final String PARAM_CLEAR_BEFORE = "clearBefore";
	public static final String PARAM_NOTIFY_REMOTE = "notifyRemote";
	public static final String PARAM_CONTENT = "content";
	
	private final String linkSetUri;
	
	private final DrbUriInfo fromDataSourceUriInfo;
	private final DrbUriInfo toDataSourceUriInfo;
	
	private boolean clearBefore;
	private boolean notifyRemote;
	
	private Model changeModel;
	
	public LinkManager(String linkSetUri, String fromDataSourceUri, String toDataSourceUri) {
		this.linkSetUri = linkSetUri;
		this.fromDataSourceUriInfo = new DrbUriInfo(fromDataSourceUri);
		this.toDataSourceUriInfo = new DrbUriInfo(toDataSourceUri);
		changeModel = ModelFactory.createDefaultModel();
		
		clearBefore = true;
		notifyRemote = true;
	}

	public String getLocalDataSourceUri() {
		return fromDataSourceUriInfo.getUri();
	}

	public String getRemoteDataSourceUri() {
		return toDataSourceUriInfo.getUri();
	}

	public DrbUriInfo getLocalDataSourceUriInfo() {
		return fromDataSourceUriInfo;
	}

	public DrbUriInfo getRemoteDataSourceUriInfo() {
		return toDataSourceUriInfo;
	}
	
	public boolean getClearBefore() {
		return clearBefore;
	}
	
	public void setClearBefore(boolean clearBefore) {
		this.clearBefore = clearBefore;
	}
	
	public boolean getNotifyRemote() {
		return notifyRemote;
	}
	
	public void setNotifyRemote(boolean notifyRemote) {
		this.notifyRemote = notifyRemote;
	}

	public synchronized void createLinks(String linkUri, String fromObjectId, String... toObjectIds) {
		
		Resource fromObjectResource = changeModel.createResource(fromDataSourceUriInfo.formatObjectUri(fromObjectId));
		Property linkProperty = changeModel.createProperty(linkUri);
		
		if (toObjectIds != null) {
			for (String toObjectId : toObjectIds) {
				Resource toObjectResource = changeModel.createResource(toDataSourceUriInfo.formatObjectUri(toObjectId));				
				changeModel.add(fromObjectResource, linkProperty, toObjectResource);
			}
		}
		
	}
	
	public synchronized void createLinks(String linkUri, Collection<Entry<String, String>> objectMapping) {
		objectMapping
			.stream()
			.forEach(
					pair -> createLinks(linkUri, pair.getKey(), pair.getValue()));
	}
	
	public synchronized void commit() {
		
		WebTarget target = 
				ClientBuilder
					.newClient()
					.target(linkSetUri)
					.path(PATH_UPLOAD_CONTENT);
		
		Form form = new Form();
		form.param(PARAM_CLEAR_BEFORE, Boolean.toString(clearBefore));		
		form.param(PARAM_NOTIFY_REMOTE, Boolean.toString(notifyRemote));
		form.param(PARAM_CONTENT, null);
		
		final Response response =
				target
					.request()
					.put(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), Response.class);
		
		if (response.getStatusInfo().getFamily() != Status.Family.SUCCESSFUL) {
			throw new RuntimeException(String.format("Error %d (%s): %s", response.getStatus(), response.getStatusInfo(), response.getEntity()));			
		}		
		
		
	}
	
	public synchronized void rollBack() {
		changeModel = ModelFactory.createDefaultModel();
	}
	
	
}
