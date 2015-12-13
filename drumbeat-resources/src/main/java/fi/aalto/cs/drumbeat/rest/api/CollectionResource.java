package fi.aalto.cs.drumbeat.rest.api;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

@Path("/collections")
public class CollectionResource {

	@Context
	private ServletContext servletContext;
	
	
}