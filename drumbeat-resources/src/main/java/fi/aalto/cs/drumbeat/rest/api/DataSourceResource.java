package fi.aalto.cs.drumbeat.rest.api;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import org.glassfish.jersey.media.multipart.FormDataParam;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import fi.aalto.cs.drumbeat.rest.accessory.PrettyPrinting;
import fi.aalto.cs.drumbeat.rest.application.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.managers.DataSourceManager;

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

@Path("/datasources")
public class DataSourceResource extends AbstractResource {
	private static DataSourceManager manager;

	@Context
	private ServletContext servletContext;

	@Path("/{collectionid}")
	@GET
	public String list(@Context HttpServletRequest httpRequest,@PathParam("collectionid") String collectionid) {
		Model m = ModelFactory.createDefaultModel();

		try {
			if (!getManager().listAll2Model(m, collectionid))
				return PrettyPrinting.format(httpRequest, "No datasources",PrettyPrinting.OK);
		} catch (Exception e) {
			return PrettyPrinting.formatError(httpRequest, "Check that the RDF store is started: " + e.getMessage());
		}

		return model2AcceptedFormat(httpRequest, m);
	}

	@Path("/{collectionid}")
	@PUT
	public String listPUT(@Context HttpServletRequest httpRequest,@PathParam("collectionid") String collectionid) {
		return PrettyPrinting.formatError(httpRequest, "Use GET to list data sources");
	}

	@Path("/{collectionid}")
	@POST
	public String listPOST(@Context HttpServletRequest httpRequest,@PathParam("collectionid") String collectionid) {
		return PrettyPrinting.formatError(httpRequest, "Use GET to list data sources");
	}

	
	@Path("/{collectionid}/{datasourceid}")
	@GET
	public String get(@Context HttpServletRequest httpRequest, @PathParam("collectionid") String collectionid, @PathParam("datasourceid") String datasourceid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try {
			if (!getManager().get2Model(m, collectionid, datasourceid))
				return PrettyPrinting.formatError(httpRequest, "The ID does not exist");
		} catch (Exception e) {
			return PrettyPrinting.formatError(httpRequest, "Check that the RDF store is started: " + e.getMessage());
		}

		return model2AcceptedFormat(httpRequest, m);
	}

	@Path("/{collectionid}/{datasourceid}")
	@POST
	public String getPOST(@Context HttpServletRequest httpRequest, @PathParam("collectionid") String collectionid, @PathParam("datasourceid") String datasourceid) {
		return PrettyPrinting.formatError(httpRequest, "Use PUT to create data sources");
	}

	@Path("/{collectionid}/{datasourceid}")
	@PUT
	public String create(@Context HttpServletRequest httpRequest, @PathParam("collectionid") String collectionid, @PathParam("datasourceid") String datasourceid,@DefaultValue("No name given.")  @FormDataParam("name") String name) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		try {
			if(!getManager().create(collectionid, datasourceid, name))
				return PrettyPrinting.formatError(httpRequest,"The named collection does not exist. Creation was not done.");
		} catch (Exception e) {
			return PrettyPrinting.formatError(httpRequest,"Check that the RDF store is started: " + e.getMessage());
		}

		return PrettyPrinting.format(httpRequest, "Done",PrettyPrinting.OK);
	}

	@Path("/{collectionid}/{datasourceid}")
	@DELETE
	public String delete(@Context HttpServletRequest httpRequest, @PathParam("collectionid") String collectionid, @PathParam("datasourceid") String datasourceid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		try {		
			if(!getManager().delete(collectionid, datasourceid))
				return PrettyPrinting.formatError(httpRequest,"The data source has still datasets. Removal was not done.");
		} catch (Exception e) {
			return PrettyPrinting.formatError(httpRequest,"Check that the RDF store is started: " + e.getMessage());
		}

		return PrettyPrinting.format(httpRequest, "Done",PrettyPrinting.OK);
	}

	@Path("/{collectionid}/{datasourceid}/{extras:.+}")
	@GET
	public String getExtrasGET(@Context HttpServletRequest httpRequest,@PathParam("collectionid") String collectionid,@PathParam("extras") String extras) {
		return PrettyPrinting.formatError(httpRequest, "Usage: /datasources  or /datasources/{ID}/{ID} Extra characters were:/"+extras);
	}
	
	@Path("/{collectionid}/{datasourceid}/{extras:.+}")
	@PUT
	public String getExtrasPUT(@Context HttpServletRequest httpRequest,@PathParam("collectionid") String collectionid,@PathParam("extras") String extras) {
		return PrettyPrinting.formatError(httpRequest, "Usage: /datasources  or /datasources/{ID}/{ID} Extra characters were:/"+extras);
	}
	
	@Path("/{collectionid}/{datasourceid}/{extras:.+}")
	@POST
	public String getExtrasPOST(@Context HttpServletRequest httpRequest,@PathParam("collectionid") String collectionid,@PathParam("extras") String extras) {
		return PrettyPrinting.formatError(httpRequest, "Usage: /datasources/{collectionid}  or /datasources/{collectionid}/{datasourceid} Extra characters were:/"+extras);
	}

	
	@Override
	public DataSourceManager getManager() {
		if (manager == null) {
			try {
				Model model = DrumbeatApplication.getInstance().getJenaProvider().openDefaultModel();
				manager = new DataSourceManager(model);
			} catch (Exception e) {
				throw new RuntimeException("Could not get Jena model: " + e.getMessage(), e);
			}
		}
		return manager;
	}

}