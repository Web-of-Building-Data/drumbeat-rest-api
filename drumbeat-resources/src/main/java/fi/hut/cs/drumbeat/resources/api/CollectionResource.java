
package fi.hut.cs.drumbeat.resources.api;
 
/*
The MIT License (MIT)

Copyright (c) 2015 Jyrki Oraskari

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
 
 
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.sun.jersey.server.impl.application.WebApplicationContext;

import fi.hut.cs.drumbeat.resources.managers.AppManager;
import fi.hut.cs.drumbeat.resources.managers.CollectionManager;

@Path("/collections")
public class CollectionResource {
	
	private static CollectionManager collectionManager;
	
	@Context
	private ServletContext servletContext;
	
//	@Resource
//	private WebApplicationContext webApplicationContext;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	//@Produces("application/ld+json")
//	@Produces(MediaType.TEXT_HTML)
	public String listCollections() {		
		
//		StringWriter writer = new StringWriter();
//		
//		writer.write("App Deployed Directory path: " + servletContext.getRealPath("/") + "<br/>");
//		writer.write("getContextPath(): " + servletContext.getContextPath() + "<br/>");
//		writer.write("Apache Tomcat Server: " + servletContext.getServerInfo() + "<br/>");
//		writer.write("Servlet API version: " + servletContext.getMajorVersion() + "." +servletContext.getMinorVersion() + "<br/>");
//		writer.write("Tomcat Project Name: " + servletContext.getServletContextName());
//		
//		return writer.toString();

		
		ResultSet results = getCollectionManager(servletContext).getAll();
		
		// write to a ByteArrayOutputStream
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		ResultSetFormatter.outputAsJSON(outputStream, results);

		// and turn that into a String
		String json = new String(outputStream.toByteArray());		
		return json;
	}

	
	@Path("/{name}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getInformation(@PathParam("name") String collection_guid) {
		return null;
	}
	
	
	@Path("/{name}")
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public void createCollection(@PathParam("name") String collection_name) {
		System.out.println("Collections create name: "+collection_name);
	}
	
	
	private static CollectionManager getCollectionManager(ServletContext servletContext) {
		if (collectionManager == null) {
			try {
				Model model = AppManager.getJenaProvider(servletContext).openDefaultModel();
				collectionManager = new CollectionManager(model);
			} catch (Exception e) {
				throw new RuntimeException("Could not get Jena model: " + e.getMessage(), e);
			}
			
		}
		return collectionManager;
				
	}
	
	
	
	

}