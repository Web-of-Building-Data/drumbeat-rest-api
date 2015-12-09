package fi.aalto.cs.drumbeat.rest.accessory;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

import com.github.jsonldjava.jena.JenaJSONLD;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import fi.aalto.cs.drumbeat.rest.application.DrumbeatApplication;
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

public class PrettyPrinting {
	static final public String ERROR = "ERROR"; 
	static final public String OK = "OK"; 
	
	static public String prettyPrintingHTML(Model model) {

		StringBuffer html_src = new StringBuffer();
		html_src.append("<HTML><BODY><style>" + "table#t01 {width: 100%;  background-color: #f1f1c1;}" + "table#t01 th { color: white;    background-color: black;	}"
				+ "table#t01 tr:nth-child(odd) { background-color: #eee;}" + "table#t01 tr:nth-child(even) {background-color: #fff}" + "</style>\n");

		StmtIterator iter = model.listStatements();

		html_src.append("<TABLE id=\"t01\"><tr><td><B>Subject</B></td><td><B>Property</B></td><td><B>Object</B></td> </tr>\n");

		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement(); // get next statement
			html_src.append("<TR>");
			// Subject
			html_src.append("<TD>");
			html_src.append("<A HREF=\"" + stmt.getSubject().getURI() + "\">" + stmt.getSubject().getURI() + "</A>");
			html_src.append("</TD>");

			// Predicate
			html_src.append("<TD>");
			html_src.append("<A HREF=\"" + stmt.getPredicate().getURI() + "\">" + stmt.getPredicate().getURI() + "</A>");
			html_src.append("</TD>");

			// object
			html_src.append("<TD>");
			if (stmt.getObject().isURIResource())
				html_src.append("<A HREF=\"" + ((Resource) stmt.getObject()).getURI() + "\">" + ((Resource) stmt.getObject()).getURI() + "</A>");
			else
				html_src.append(stmt.getObject());
			html_src.append("</TD>");

			html_src.append("</TR>\n");
		}

		html_src.append("\n</TABLE><P><BODY></HTML>\n");
		return html_src.toString();
	}

	static public String prettyPrintingTXT(Model model) {

		StringBuffer html_src = new StringBuffer();

		StmtIterator iter = model.listStatements();

		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement(); // get next statement
			html_src.append("\n");
			// Subject
			html_src.append("\t");
			html_src.append(stmt.getSubject().getURI());

			// Predicate
			html_src.append("\t");
			html_src.append(stmt.getPredicate().getURI());

			// object
			html_src.append("\t");
			if (stmt.getObject().isURIResource())
				html_src.append(((Resource) stmt.getObject()).getURI());
			else
				html_src.append(stmt.getObject());

		}

		html_src.append("\n");
		return html_src.toString();
	}

	static public String format2TXT(String txt,String status_txt) {
		return "Status: "+status_txt+": " + txt;
	}

	static public String format2HTML(String txt,String status_txt) {
		StringBuffer html_src = new StringBuffer();
		html_src.append("<HTML><BODY><style>" + "table#t01 {width: 100%;  background-color: #f1f1c1;}" + "table#t01 th { color: white;    background-color: black;	}"
				+ "table#t01 tr:nth-child(odd) { background-color: #eee;}" + "table#t01 tr:nth-child(even) {background-color: #fff}" + "</style>\n");

		html_src.append("<TABLE id=\"t01\"><tr><td><B>Status</B></td><td><B>Value</B></td> </tr>\n");
		html_src.append("<TR>");
		// Status
		html_src.append("<TD>");
		html_src.append(status_txt);
		html_src.append("</TD>");

		// Value
		html_src.append("<TD>");
		html_src.append(txt);
		html_src.append("</TD>");

		html_src.append("</TR>\n");

		html_src.append("\n</TABLE><P><BODY></HTML>\n");
		return html_src.toString();
	}

	static public String format2JSON(String txt,String status_txt) {
		try {
			Model m = ModelFactory.createDefaultModel();
			Resource status = m.createResource(DrumbeatApplication.getInstance().getBaseUri() + "STATUS");
			Property is_property = ResourceFactory.createProperty(BuildingDataOntology.Ontology_BASE_URL + "is");
			Property description_property = ResourceFactory.createProperty(BuildingDataOntology.Ontology_BASE_URL + "description");
			status.addProperty(is_property, status_txt, XSDDatatype.XSDstring);
			status.addProperty(description_property, txt, XSDDatatype.XSDstring);

			JenaJSONLD.init();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			m.write(os, "JSON-LD");
			try {
				return new String(os.toByteArray(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				return "Status: "+status_txt+": " + txt;
			}
		} catch (Exception e) {
			return "Status: "+status_txt+": " + txt;
		}

	}

	static public String format2TURTLE(String txt,String status_txt) {
		try {
			Model m = ModelFactory.createDefaultModel();
			Resource status = m.createResource(DrumbeatApplication.getInstance().getBaseUri() + "STATUS");
			Property is_property = ResourceFactory.createProperty(BuildingDataOntology.Ontology_BASE_URL + "is");
			Property description_property = ResourceFactory.createProperty(BuildingDataOntology.Ontology_BASE_URL + "description");
			status.addProperty(is_property, status_txt, XSDDatatype.XSDstring);
			status.addProperty(description_property, txt, XSDDatatype.XSDstring);

			JenaJSONLD.init();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			m.write(os, "TURTLE");
			try {
				return new String(os.toByteArray(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				return "Status: "+status_txt+": " + txt;
			}
		} catch (Exception e) {
			return "Status: "+status_txt+": " + txt;
		}

	}

	static public String format2RDF(String txt,String status_txt) {
		try {
			Model m = ModelFactory.createDefaultModel();
			Resource status = m.createResource(DrumbeatApplication.getInstance().getBaseUri() + "STATUS");
			Property is_property = ResourceFactory.createProperty(BuildingDataOntology.Ontology_BASE_URL + "is");
			Property description_property = ResourceFactory.createProperty(BuildingDataOntology.Ontology_BASE_URL + "description");
			status.addProperty(is_property, status_txt, XSDDatatype.XSDstring);
			status.addProperty(description_property, txt, XSDDatatype.XSDstring);

			JenaJSONLD.init();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			m.write(os, "RDF/XML");
			try {
				return new String(os.toByteArray(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				return "Status: "+status_txt+": " + txt;
			}
		} catch (Exception e) {
			return "Status: "+status_txt+": " + txt;
		}

	}
	

	static public String format(HttpServletRequest httpRequest, String txt,String status) {
		try {
			String accept = httpRequest.getHeader("Accept");
			if (accept != null) {
				switch (accept) {
				case MediaType.TEXT_HTML:
					return format2HTML(txt,status);
				case MediaType.TEXT_PLAIN:
					return format2TXT(txt,status);
				case MediaType.APPLICATION_JSON:
				case "application/ld+json":
					return format2JSON(txt,status);
				case "text/turtle":
					return format2TURTLE(txt,status);
				case "application/rdf+xml":
					return format2TURTLE(txt,status);										
				default:									
					return format2TXT(txt,status);
				}
			}

		} catch (Exception e) {
			;
		}
		return txt;
	}
	
	static public String formatError(HttpServletRequest httpRequest, String txt) {
		try {
			String accept = httpRequest.getHeader("Accept");
			if (accept != null) {
				switch (accept) {
				case MediaType.TEXT_HTML:
					return format2HTML(txt,ERROR);
				case MediaType.TEXT_PLAIN:
					return format2TXT(txt,ERROR);
				case MediaType.APPLICATION_JSON:
				case "application/ld+json":
					return format2JSON(txt,ERROR);
				case "text/turtle":
					return format2TURTLE(txt,ERROR);
				case "application/rdf+xml":
					return format2TURTLE(txt,ERROR);										
				default:									
					return format2HTML(txt,ERROR);
				}
			}
			else
				return format2TXT(txt,ERROR);

		} catch (Exception e) {
			;
		}
		return txt;
	}
}
