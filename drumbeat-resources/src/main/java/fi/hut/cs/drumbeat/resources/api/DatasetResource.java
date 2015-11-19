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
import com.hp.hpl.jena.rdf.model.Resource;

import fi.hut.cs.drumbeat.resources.managers.AppManager;
import fi.hut.cs.drumbeat.resources.managers.CollectionManager;
import fi.hut.cs.drumbeat.resources.managers.DatasetManager;

@Path("/datasets")
public class DatasetResource {

	private static DatasetManager datasetManager;

	@Context
	private ServletContext servletContext;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String listDatasetsJSON() {
		String json=null;
		try {

			ResultSet results = getDatasetManager(servletContext).listAll();

			// write to a ByteArrayOutputStream
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			ResultSetFormatter.outputAsJSON(outputStream, results);
			// and turn that into a String
			json = new String(outputStream.toByteArray());
			
		} catch (RuntimeException r) {

		}
		return json;
	}

	@Path("/{name}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getDatasetJSON(@PathParam("name") String dataset_guid) {
		try {
			Resource dataset = getDatasetManager(servletContext).get(
					dataset_guid);
		} catch (RuntimeException r) {

		}
		return null;
	}

	@Path("/{name}")
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public void createDatasetJSON(@PathParam("name") String dataset_guid) {
		try {
			getDatasetManager(servletContext).create(dataset_guid);
			System.out.println("Dataset create name: " + dataset_guid);
		} catch (RuntimeException r) {

		}
	}

	private static DatasetManager getDatasetManager(
			ServletContext servletContext) {
		if (datasetManager == null) {
			try {
				Model model = AppManager.getJenaProvider(servletContext)
						.openDefaultModel();
				datasetManager = new DatasetManager(model);
			} catch (Exception e) {
				throw new RuntimeException("Could not get Jena model: "
						+ e.getMessage(), e);
			}
		}
		return datasetManager;

	}

}