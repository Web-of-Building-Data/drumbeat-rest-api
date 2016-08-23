package fi.aalto.cs.drumbeat.adapters.bimserver;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;

public class BimServerApi {
	
	private final String serverUrl;
	
	public BimServerApi(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String createProject(String name, String schema) {

		WebTarget target = ClientBuilder.newClient().target(serverUrl);

		Form form = new Form();
//		form.param(PARAM_DATA_TYPE, DATA_TYPE_RDF);
//		form.param(PARAM_DATA_FORMAT, "." + RDF_LANG.getFileExtensions().get(0));
//		form.param(PARAM_CLEAR_BEFORE, Boolean.toString(clearBefore));
//		form.param(PARAM_NOTIFY_REMOTE, Boolean.toString(notifyRemote));
//
//		StringWriter writer = new StringWriter();
//		changeModel.write(writer, RDF_LANG.getName());
//		form.param(PARAM_CONTENT, writer.toString());
//
//		// System.out.println(writer.toString());
//
//		StringBuilder ontologyUriBuilder = new StringBuilder();
//
//		for (Entry<String, String> namespace : changeModel.getNsPrefixMap().entrySet()) {
//			String prefix = namespace.getKey();
//			if (!prefix.equals(NAMESPACE_PREFIX_FROM) && !prefix.equals(NAMESPACE_PREFIX_TO)) {
//				String uri = namespace.getValue();
//				ontologyUriBuilder.append(uri).append(';');
//			}
//		}
//
//		form.param(PARAM_ONTOLOGY_URI, ontologyUriBuilder.toString());
//
//		final Response response = target.request().post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED),
//				Response.class);
//
//		if (response.getStatusInfo().getFamily() != Status.Family.SUCCESSFUL) {
//			throw new RuntimeException(String.format("Error %d (%s): %s", response.getStatus(),
//					response.getStatusInfo(), response.getEntity()));
//		}
//
//		changeModel.removeAll();
		
		return null;

	}

}
