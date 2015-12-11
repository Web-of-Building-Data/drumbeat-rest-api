package fi.aalto.cs.drumbeat.rest.api;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.github.jsonldjava.jena.JenaJSONLD;
import com.hp.hpl.jena.rdf.model.Model;

import fi.aalto.cs.drumbeat.rest.accessory.PrettyPrinting;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatWebApplication;
import fi.aalto.cs.drumbeat.rest.managers.AbstractManager;



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

public abstract class AbstractResource {
	abstract public AbstractManager getManager();
	
	@Path("/url")
	@GET
	public String getURL(@Context HttpServletRequest httpRequest) {
		DrumbeatWebApplication.getInstance().setBaseUrl(httpRequest);
		return "{\"URL\":\""+DrumbeatWebApplication.getInstance().getBaseUri()+"\"}";
	}
	
	
	@Path("/alive")
	@GET
	public String isAlive() {
		return "{\"status\":\"LIVE\"}";
	}
	
	private String model2JSON_LD(Model m) {
		JenaJSONLD.init();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		m.write(os, "JSON-LD");
		try {
			return new String(os.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return PrettyPrinting.format2JSON(e.getMessage(),PrettyPrinting.ERROR);
		}
	}

	private String model2TURTLE(Model m) {
		JenaJSONLD.init();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		m.write(os, "TURTLE");
		try {
			return new String(os.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return PrettyPrinting.format2TURTLE(e.getMessage(),PrettyPrinting.ERROR);
		}
	}
	
	private String model2RDF(Model m) {
		JenaJSONLD.init();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		m.write(os, "RDF/XML");
		try {
			return new String(os.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return PrettyPrinting.format2RDF(e.getMessage(),PrettyPrinting.ERROR);
		}
	}
	
	public String  model2AcceptedFormat(HttpServletRequest httpRequest, Model m) {
		if(httpRequest==null)
		 return "ERROR: HTTP request header is missing";
		if(m==null)
			return PrettyPrinting.formatError(httpRequest, "Check the RDF store.");
		try {
			String accept = httpRequest.getHeader("Accept");
			if (accept != null) {
				switch (accept) {
				case MediaType.TEXT_HTML:
					return PrettyPrinting.prettyPrintingHTML(m);
				case MediaType.TEXT_PLAIN:
					return PrettyPrinting.prettyPrintingTXT(m);
				case MediaType.APPLICATION_JSON:
				case "application/ld+json":
					return model2JSON_LD(m);
				case "text/turtle":
					return model2TURTLE(m);
				case "application/rdf+xml":
					return model2RDF(m);										
				default:									
					return PrettyPrinting.prettyPrintingHTML(m);
				}
			}
			else
			{
				return PrettyPrinting.prettyPrintingTXT(m);
			}

		} catch (Exception e) {
			;
		}
		return PrettyPrinting.formatError(httpRequest, "Data format errot: Check the RDF store.");
		
		
	}
}