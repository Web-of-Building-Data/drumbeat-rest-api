package fi.aalto.cs.drumbeat.rest.api;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

@Path("/datasources")
public class DataSourceResource {

	@Context
	private ServletContext servletContext;
	
	
}