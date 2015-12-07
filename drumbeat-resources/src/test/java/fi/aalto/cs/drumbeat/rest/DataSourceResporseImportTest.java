package fi.aalto.cs.drumbeat.rest;

import static org.junit.Assert.*;

import java.io.File;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.client.ClientResponse;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

public class DataSourceResporseImportTest extends JerseyTest {
	
	@Override
	protected AppDescriptor configure() {
		return new WebAppDescriptor.Builder().build();
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DrumbeatTestHelper.init();
	}
	
	
	@Test
	public void test_importServerFile_correctFile() {
		
		WebResource webResource = client().resource(DrumbeatTestHelper.getWebBaseUrl() + "datasets/c1/s1/sc1/importServerFile");
		
		File testModelFilePath = new File(DrumbeatTestHelper.TEST_MODEL_FILE_PATH);
		
		MultivaluedMap<String, String> formData = new MultivaluedHashMap<String, String>();
		
	    formData.add("filePath", testModelFilePath.getAbsolutePath());
	    
	    ClientResponse response = 
	            webResource.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
	                             .post(ClientResponse.class, formData);
	    
	    assertEquals(200, response.getStatus());
	    
		
	}
	
	

}
