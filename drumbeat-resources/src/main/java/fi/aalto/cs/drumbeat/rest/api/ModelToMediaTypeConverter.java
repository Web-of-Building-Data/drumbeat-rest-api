package fi.aalto.cs.drumbeat.rest.api;

import java.io.StringWriter;
import java.util.List;

import static javax.ws.rs.core.MediaType.*;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.github.jsonldjava.jena.JenaJSONLD;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatWebException;
import fi.aalto.cs.drumbeat.rest.ontology.LinkedBuildingDataOntology;

public class ModelToMediaTypeConverter {
	
	public static final String APPLICATION_RDF_XML = "application/rdf+xml";
	public static final String APPLICATION_LD_JSON = "application/ld+json";
	public static final String TEXT_TURTLE = "text/turtle";
	
	public static String convertModelToAcceptableMediaTypes(Model model, List<MediaType> mediaTypes) {
		
		for (MediaType mediaType : mediaTypes) {
			
			String result = convertModelToAcceptableMediaType(model, mediaType);
			if (result != null) {
				return result;
			}
			
		}
		
		throw new DrumbeatWebException(
				Response.Status.UNSUPPORTED_MEDIA_TYPE,
				"Unsupported media type(s): " + mediaTypes.toString(),
				null);
	}
	
	public static String convertModelToAcceptableMediaType(Model model, MediaType mediaType) {
		return convertModelToAcceptableMediaType(model, mediaType.getType() + "/" + mediaType.getSubtype());
	}
		
	public static String convertModelToAcceptableMediaType(Model model, String mediaTypeString) {
		switch (mediaTypeString) {
		
		case APPLICATION_JSON:
		case APPLICATION_LD_JSON:			
			return convertModelToRdf(model, "JSON-LD");
			
		case TEXT_PLAIN:
		case TEXT_TURTLE:
			return convertModelToRdf(model, "TURTLE");

		case APPLICATION_RDF_XML:
		case APPLICATION_XML:
			return convertModelToRdf(model, "RDF/XML");

		case TEXT_HTML:
		case WILDCARD:
			return convertModelToHtml(model);
			
		default:
			return null;			
		}
		
	}
	
//	public static String convertModelToRdfFormat(Model model, RDFFormat format) {
//		StringWriter out = new StringWriter();
//		model.setNsPrefixes(LinkedBuildingDataOntology.getDefaultNsPrefixes());
//		RDFDataMgr.write(out, model, format);		
//		return out.toString();
//	}
	
	public static String convertModelToRdf(Model model, String lang) {
		if (lang.equalsIgnoreCase("JSON-LD")) {
			JenaJSONLD.init();
		}
		StringWriter writer = new StringWriter();
		model.setNsPrefixes(LinkedBuildingDataOntology.getDefaultNsPrefixes());
		model.write(writer, lang, DrumbeatApplication.getInstance().getBaseUri());
		return writer.toString();
	}

	public static String convertModelToHtml(Model model) {
		StringBuilder stringBuilder = new StringBuilder()
				.append("<html><body>")
				.append("<style type=\"text/css\">")
				.append("table.rdf { width:100%; }")
				.append("table.rdf th { color:#fff; background-color: #000; }")
				.append("table.rdf tr:nth-child(odd) { background-color: #e0e0e0; }")
				.append("table.rdf tr:nth-child(even) { background-color: #fff; }")
				.append("</style>")
				.append("<table class=\"rdf\">")
				.append("<tr><th>Subject</th><th>Predicate</th><th>Object</th></tr>");
		
		StmtIterator stmtIterator = model.listStatements();
		while(stmtIterator.hasNext()) {
			Statement statement = stmtIterator.nextStatement();
			stringBuilder
				.append("<tr><td>")
				.append(convertRdfNodeToHtml(statement.getSubject()))
				.append("</td><td>")
				.append(convertRdfNodeToHtml(statement.getPredicate()))
				.append("</td><td>")
				.append(convertRdfNodeToHtml(statement.getObject()))
				.append("</td></tr>");
		}		
		
		return stringBuilder
				.append("</table>")
				.append("</body></html>")
				.toString();
	}
	
	public static String convertRdfNodeToHtml(RDFNode node) {
		if (node.isURIResource()) {
			return String.format("<a href=\"%s\">%s</a>", node.toString(), node.toString());
		} else {
			return node.toString();
		}
	}

}
