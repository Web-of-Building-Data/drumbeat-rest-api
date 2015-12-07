package fi.aalto.cs.drumbeat.rest.api;

import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.util.Map;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONException;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.test.JerseyTest;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import fi.aalto.cs.drumbeat.rest.DrumbeatTest;
import fi.aalto.cs.drumbeat.rest.application.TestApplication;


public class CollectionResourceTest extends DrumbeatTest {
	 
	static final boolean testing = true;
	
	@Test
	public void testAlive() throws JSONException,
			URISyntaxException {
		if(!testing)
			return;
		
		Map<String, String> result =
				target("datasets/alive")
					.request(MediaType.APPLICATION_JSON_TYPE)
					.get(new GenericType<Map<String,String>>(){});
		
		String status = result.get("status");
		assertEquals("LIVE", status);		
	}

//	@Test
//	public void listCollections() throws JSONException,
//			URISyntaxException {
//		if(!testing)
//			return;
//		WebResource webResource = client().resource(DrumbeatTestHelper.getWebBaseUrl());
//		JSONObject json = webResource.path("/collections")
//				.get(JSONObject.class);
//		assertEquals("12", json.get("id"));
//	}
//
//	@Test
//	public void testCreateCollection() throws JSONException,
//			URISyntaxException {
//		if(!testing)
//			return;
//		WebResource webResource = client().resource(DrumbeatTestHelper.getWebBaseUrl());
//		JSONObject json = webResource.path("/collections/alive")
//				.get(JSONObject.class);
//		
//		assertEquals("LIVE", json.get("status"));
//	}
 
}