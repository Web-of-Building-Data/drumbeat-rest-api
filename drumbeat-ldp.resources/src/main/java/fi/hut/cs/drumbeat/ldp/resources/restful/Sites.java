
package fi.hut.cs.drumbeat.ldp.resources.restful;
 
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
import java.io.File;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;

import fi.hut.cs.drumbeat.ldp.resources.managers.AppManager;
import fi.hut.cs.drumbeat.ldp.resources.managers.SiteManager;

@Path("/sites")
public class Sites {
	
	private static SiteManager siteManager;
	
	@SuppressWarnings("unchecked")
	@GET
	@Produces("application/ld+json")
	public String listSites() {
		
		ResultSet results = getSiteManager().getAll();
		
		// write to a ByteArrayOutputStream
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		ResultSetFormatter.outputAsJSON(outputStream, results);

		// and turn that into a String
		String json = new String(outputStream.toByteArray());		
		return json;
	}

	
	@SuppressWarnings("unchecked")
	@Path("/{name}")
	@GET
	@Produces("application/json")
	public String getInformation(@PathParam("name") String site_guid) {
		return null;
	}
	
	
	@SuppressWarnings("unchecked")
	@Path("/{name}")
	@PUT
	@Produces("application/json")
	public void createSite(@PathParam("name") String site_name) {
		System.out.println("Sites create name: "+site_name);
		//createSite(site_name);
	}
	
	
	private static SiteManager getSiteManager() {
		if (siteManager == null) {
			try {
				Model model = AppManager.getModelFactory().getModel();
				siteManager = new SiteManager(model);			
			} catch (Exception e) {
				throw new RuntimeException("Could not get Jena model: " + e.getMessage(), e);
			}
			
		}
		return siteManager;
				
	}
	
	
	
	

}