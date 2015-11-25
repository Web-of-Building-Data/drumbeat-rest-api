package fi.aalto.cs.drumbeat.rest;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;


public class CollectionResourceTest extends JerseyTest {
	 
	@Override
	protected AppDescriptor configure() {
		return new WebAppDescriptor.Builder().build();
	}
 
	/*
	@Test
	public void testAlive() throws JSONException,
			URISyntaxException {
		WebResource webResource = client().resource("http://localhost:8080/r");
		JSONObject json = webResource.path("/collections/alive")
				.get(JSONObject.class);
		assertEquals("LIVE", json.get("status"));
	}

	@Test
	public void listCollections() throws JSONException,
			URISyntaxException {
		WebResource webResource = client().resource("http://localhost:8080/");
		JSONObject json = webResource.path("/collections")
				.get(JSONObject.class);
		assertEquals("12", json.get("id"));
	}

	@Test
	public void testCreateCollection() throws JSONException,
			URISyntaxException {
		WebResource webResource = client().resource("http://localhost:8080/r");
		JSONObject json = webResource.path("/collections/alive")
				.get(JSONObject.class);
		assertEquals("LIVE", json.get("status"));
	}
	*/
 
}