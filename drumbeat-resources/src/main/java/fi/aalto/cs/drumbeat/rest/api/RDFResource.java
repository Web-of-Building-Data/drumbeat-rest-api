package fi.aalto.cs.drumbeat.rest.api;

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

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import fi.aalto.cs.drumbeat.rest.managers.AppManager;
import fi.aalto.cs.drumbeat.rest.managers.RDFResourceManager;

@Path("/")
public class RDFResource {

	private static RDFResourceManager resourceManager;

	@Context
	private ServletContext servletContext;

	@Path("/alive")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String isAlive() {		
		return "{\"status\":\"LIVE\"}";
	}

	
	@Path("/{name}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getResourceJSON(@PathParam("name") String resource_guid) {
		try {
			Resource collection = getResourceManager(servletContext).get(resource_guid);
		} catch (RuntimeException r) {

		}
		return null;
	}



	private static RDFResourceManager getResourceManager(
			ServletContext servletContext) {
		if (resourceManager == null) {
			try {
				Model model = AppManager.getJenaProvider(servletContext)
						.openDefaultModel();
				resourceManager = new RDFResourceManager(model);
			} catch (Exception e) {
				throw new RuntimeException("Could not get Jena model: "
						+ e.getMessage(), e);
			}
		}
		return resourceManager;

	}

}