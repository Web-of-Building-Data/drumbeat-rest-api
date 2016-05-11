package fi.aalto.cs.drumbeat.rest.client.link;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;

//import javax.ws.rs.client.ClientBuilder;
//import javax.ws.rs.client.Entity;
//import javax.ws.rs.client.WebTarget;
//import javax.ws.rs.core.Form;
//import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.Response;
//import javax.ws.rs.core.Response.Status;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class LinkManager 
{
	
	public static final String PATH_UPLOAD_CONTENT = "uploadContent";
	public static final String PARAM_DATA_TYPE = "dataType";
	public static final String PARAM_DATA_FORMAT = "dataFormat";
	public static final String PARAM_CLEAR_BEFORE = "clearBefore";
	public static final String PARAM_NOTIFY_REMOTE = "notifyRemote";
	public static final String PARAM_CONTENT = "content";
	public static final String PARAM_ONTOLOGY_URI = "ontologyUri";
	
	public static final String NAMESPACE_PREFIX_FROM = "from";
	public static final String NAMESPACE_PREFIX_TO = "to";
	
	public static final String DATA_TYPE_RDF = "RDF";
	public static final Lang RDF_LANG = Lang.TURTLE;
	
	public static final boolean PARAM_CLEAR_BEFORE_DEFAULT_VALUE = false; 
	public static final boolean PARAM_NOTIFY_REMOTE_DEFAULT_VALUE = true; 
	
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
		changeModel.setNsPrefix(NAMESPACE_PREFIX_FROM, fromDataSourceUriInfo.getBaseObjectUri());
		changeModel.setNsPrefix(NAMESPACE_PREFIX_TO, toDataSourceUriInfo.getBaseObjectUri());
		
		clearBefore = PARAM_CLEAR_BEFORE_DEFAULT_VALUE;
		notifyRemote = PARAM_NOTIFY_REMOTE_DEFAULT_VALUE;
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
	
	public void defineOntology(String prefix, String uri) {
		changeModel.setNsPrefix(prefix, uri);
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
		for (Entry<String, String> pair : objectMapping) {
			createLinks(linkUri, pair.getKey(), pair.getValue());
		}
	}
	
	private void commitWithSpringWeb() {
		
		final RestTemplate rest = new RestTemplate();
		
		final HttpMessageConverter<MultiValueMap<String, ?>> formHttpMessageConverter = new FormHttpMessageConverter();
		final HttpMessageConverter<String> stringHttpMessageConverternew = new StringHttpMessageConverter();
		rest.setMessageConverters(Arrays.asList(formHttpMessageConverter, stringHttpMessageConverternew));		

		final MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();	
		params.add(PARAM_DATA_TYPE, DATA_TYPE_RDF);
		params.add(PARAM_DATA_FORMAT, "." + RDF_LANG.getFileExtensions().get(0));
		params.add(PARAM_CLEAR_BEFORE, Boolean.toString(clearBefore));		
		params.add(PARAM_NOTIFY_REMOTE, Boolean.toString(notifyRemote));
		
		final StringWriter writer = new StringWriter();
		changeModel.write(writer, RDF_LANG.getName());		
		params.add(PARAM_CONTENT, writer.toString());
		
//		System.out.println(writer.toString());
		
		final StringBuilder ontologyUriBuilder = new StringBuilder();

		for (Entry<String, String> namespace : changeModel.getNsPrefixMap().entrySet()) {
			String prefix = namespace.getKey();
			if (!prefix.equals(NAMESPACE_PREFIX_FROM) && !prefix.equals(NAMESPACE_PREFIX_TO)) {
				String uri = namespace.getValue();
				ontologyUriBuilder
					.append(uri)
					.append(';');
			}			
		}
		
		params.add(PARAM_ONTOLOGY_URI, ontologyUriBuilder.toString());
		
		
		final String url = linkSetUri + "/" + PATH_UPLOAD_CONTENT;
		
		
		rest.postForObject(url, params, String.class);
		
		
		changeModel.removeAll();
	}
	
	
//	private void commitWithJavax() {
//		
//		WebTarget target = 
//				ClientBuilder
//					.newClient()
//					.target(linkSetUri)
//					.path(PATH_UPLOAD_CONTENT);
//		
//		Form form = new Form();
//		form.param(PARAM_DATA_TYPE, DATA_TYPE_RDF);
//		form.param(PARAM_DATA_FORMAT, "." + RDF_LANG.getFileExtensions().get(0));
//		form.param(PARAM_CLEAR_BEFORE, Boolean.toString(clearBefore));		
//		form.param(PARAM_NOTIFY_REMOTE, Boolean.toString(notifyRemote));
//
//		
//		StringWriter writer = new StringWriter();
//		changeModel.write(writer, RDF_LANG.getName());		
//		form.param(PARAM_CONTENT, writer.toString());
//		
////		System.out.println(writer.toString());
//		
//		StringBuilder ontologyUriBuilder = new StringBuilder();
//
//		for (Entry<String, String> namespace : changeModel.getNsPrefixMap().entrySet()) {
//			String prefix = namespace.getKey();
//			if (!prefix.equals(NAMESPACE_PREFIX_FROM) && !prefix.equals(NAMESPACE_PREFIX_TO)) {
//				String uri = namespace.getValue();
//				ontologyUriBuilder
//					.append(uri)
//					.append(';');
//			}			
//		}
//		
//		form.param(PARAM_ONTOLOGY_URI, ontologyUriBuilder.toString());
//		
//		final Response response =
//				target
//					.request()
//					.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED), Response.class);
//		
//		if (response.getStatusInfo().getFamily() != Status.Family.SUCCESSFUL) {
//			throw new RuntimeException(String.format("Error %d (%s): %s", response.getStatus(), response.getStatusInfo(), response.getEntity()));			
//		}		
//		
//		changeModel.removeAll();
//	}
	
	public synchronized void commit() {
		commitWithSpringWeb();
	}
	
	public synchronized void rollBack() {
		changeModel.removeAll();
	}
	
}
