package fi.aalto.cs.drumbeat.rest.common;

import com.github.jsonldjava.jena.JenaJSONLD;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import fi.aalto.cs.drumbeat.rest.ontology.LinkedBuildingDataOntology;

import static javax.ws.rs.core.MediaType.*;

import java.io.StringWriter;
import java.util.List;

import javax.ws.rs.NotSupportedException;
import javax.ws.rs.core.MediaType;


public class ModelToMediaTypeConverter {
	
	public static final String APPLICATION_RDF_XML = "application/rdf+xml";
	public static final String APPLICATION_LD_JSON = "application/ld+json";
	public static final String TEXT_TURTLE = "text/turtle";	

	public static String convert(Model model, MediaType mediaType) throws NotSupportedException {
		return convert(model, mediaType.getType() + "/" + mediaType.getSubtype());
	}
		
	public static String convert(Model model, String mediaTypeString)
		throws NotSupportedException
	{
		switch (mediaTypeString) {
		
		case APPLICATION_JSON:
		case APPLICATION_LD_JSON:			
			return convertToRdf(model, "JSON-LD");
			
		case TEXT_PLAIN:
		case TEXT_TURTLE:
			return convertToRdf(model, "TURTLE");

		case APPLICATION_RDF_XML:
		case APPLICATION_XML:
			return convertToRdf(model, "RDF/XML");

		case TEXT_HTML:
		case WILDCARD:
			return convertToHtml(model);
			
		default:
			throw new NotSupportedException(
					String.format("Use one of following media types: %s", getSupportedMediaTypes().toString()));
		}
		
	}
	
	public static String[] getSupportedMediaTypes() {
		
		return 
			new String[]{
					APPLICATION_JSON,
					APPLICATION_LD_JSON,			
					TEXT_PLAIN,
					TEXT_TURTLE,
					APPLICATION_RDF_XML,
					APPLICATION_XML,
					TEXT_HTML,
					WILDCARD
				};
		
	}
	
//	public static String convertToRdfFormat(Model model, RDFFormat format) {
//		StringWriter out = new StringWriter();
//		model.setNsPrefixes(LinkedBuildingDataOntology.getDefaultNsPrefixes());
//		RDFDataMgr.write(out, model, format);		
//		return out.toString();
//	}
	
	public static String convertToRdf(Model model, String lang) {
		if (lang.equalsIgnoreCase("JSON-LD")) {
			JenaJSONLD.init();
		}
		StringWriter writer = new StringWriter();
		model.setNsPrefixes(LinkedBuildingDataOntology.getDefaultNsPrefixes());
		model.write(writer, lang, DrumbeatApplication.getInstance().getBaseUri());
		return writer.toString();
	}

	public static String convertToHtml(Model model) {
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
	
	private static String convertRdfNodeToHtml(RDFNode node) {
		if (node.isURIResource()) {
			return String.format("<a href=\"%s\">%s</a>", node.toString(), node.toString());
		} else {
			return node.toString();
		}
	}

	

}
