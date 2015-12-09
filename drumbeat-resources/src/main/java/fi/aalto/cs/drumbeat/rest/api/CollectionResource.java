package fi.aalto.cs.drumbeat.rest.api;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import org.glassfish.jersey.media.multipart.FormDataParam;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.aalto.cs.drumbeat.rest.accessory.PrettyPrinting;
import fi.aalto.cs.drumbeat.rest.application.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.managers.CollectionManager;
import fi.aalto.cs.drumbeat.rest.ontology.BuildingDataOntology;

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

@Path("/collections")
public class CollectionResource extends AbstractResource {
	private static CollectionManager manager;

	@Context
	private ServletContext servletContext;

	@GET
	public String list(@Context HttpServletRequest httpRequest) {
		Model m = ModelFactory.createDefaultModel();

		try {
			if (!getManager().listAll2Model(m))
				return PrettyPrinting.formatError(httpRequest, "No collections");
		} catch (Exception e) {
			return PrettyPrinting.formatError(httpRequest, "Check that the RDF store is started: " + e.getMessage());
		}

		return model2AcceptedFormat(httpRequest, m);
	}

	private Model listSample() {
		Model model = ModelFactory.createDefaultModel();
		Resource ctype = model.createResource(BuildingDataOntology.Collections.Collection);
		Resource c1 = model.createResource(BuildingDataOntology.Collections.Collection + "/nonexisting_sample_1");
		Resource c2 = model.createResource(BuildingDataOntology.Collections.Collection + "/nonexisting_sample_2");
		Resource c3 = model.createResource(BuildingDataOntology.Collections.Collection + "/nonexisting_sample_3");
		c1.addProperty(RDF.type, ctype);
		c2.addProperty(RDF.type, ctype);
		c3.addProperty(RDF.type, ctype);
		return model;
	}

	@Path("/example")
	@GET
	public String listSample(@Context HttpServletRequest httpRequest) {
		Model m = listSample();
		return model2AcceptedFormat(httpRequest, m);
	}

	@Path("/{collectionid}")
	@GET
	public String get(@Context HttpServletRequest httpRequest, @PathParam("collectionid") String collectionid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		Model m = ModelFactory.createDefaultModel();
		try {
			if (!getManager().get2Model(m, collectionid))
				return PrettyPrinting.formatError(httpRequest,"The ID does not exists");
		} catch (Exception e) {
			return PrettyPrinting.formatError(httpRequest,"Check that the RDF store is started: " + e.getMessage());
		}
		return model2AcceptedFormat(httpRequest, m);
	}


	@Path("/{collectionid}")
	@PUT
	public String create(@Context HttpServletRequest httpRequest, @PathParam("collectionid") String collectionid, @FormDataParam("name") String name) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		try {
			getManager().create(collectionid, name);
		} catch (Exception e) {
			return PrettyPrinting.formatError(httpRequest,"Check that the RDF store is started: " + e.getMessage());
		}

		return PrettyPrinting.format(httpRequest, "Done",PrettyPrinting.OK);
	}

	@Path("/{collectionid}")
	@DELETE
	public String delete(@Context HttpServletRequest httpRequest, @PathParam("collectionid") String collectionid) {
		DrumbeatApplication.getInstance().setBaseUrl(httpRequest);
		try {
			getManager().delete(collectionid);
		} catch (Exception e) {
			return PrettyPrinting.formatError(httpRequest,"Check that the RDF store is started: " + e.getMessage());
		}

		return PrettyPrinting.format(httpRequest, "Done",PrettyPrinting.OK);
	}

	@Override
	public CollectionManager getManager() {
		if (manager == null) {
			try {
				Model model = DrumbeatApplication.getInstance().getJenaProvider().openDefaultModel();
				manager = new CollectionManager(model);
			} catch (Exception e) {
				throw new RuntimeException("Could not get Jena model: " + e.getMessage(), e);
			}
		}
		return manager;
	}

}