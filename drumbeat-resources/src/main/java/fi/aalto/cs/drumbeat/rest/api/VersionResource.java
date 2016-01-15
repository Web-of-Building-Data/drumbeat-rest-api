package fi.aalto.cs.drumbeat.rest.api;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;

@Path("/version")
public class VersionResource {
	
	@GET
	@Path("/")
	public String getVersion() throws IOException {
		Properties properties = new Properties();
		InputStream in = new FileInputStream(DrumbeatApplication.getInstance().getRealServerPath("classes/version.txt")); 
		properties.load(in);
		return properties.toString();
	}
	

}
