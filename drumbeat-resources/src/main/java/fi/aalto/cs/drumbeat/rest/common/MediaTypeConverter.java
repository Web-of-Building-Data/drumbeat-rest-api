package fi.aalto.cs.drumbeat.rest.common;

import com.github.jsonldjava.jena.JenaJSONLD;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.XSD;

import fi.aalto.cs.drumbeat.rest.ontology.LinkedBuildingDataOntology;

import static javax.ws.rs.core.MediaType.*;

import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.NotSupportedException;
import javax.ws.rs.core.MediaType;


public class MediaTypeConverter {
	
	public static final String APPLICATION_RDF_XML = "application/rdf+xml";
	public static final String APPLICATION_LD_JSON = "application/ld+json";
	public static final String TEXT_TURTLE = "text/turtle";	

	public static String convertModel(Model model, MediaType mediaType) throws NotSupportedException {
		return convertModel(model, mediaType.getType() + "/" + mediaType.getSubtype());
	}
		
	public static String convertModel(Model model, String mediaTypeString)
		throws NotSupportedException
	{
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
				.append("table.rdf { width:100%; } ")
				.append("table.rdf th { color:#fff; background-color: #000; } ")
				.append("table.rdf tr:nth-child(odd) { background-color: #e0e0e0; } ")
				.append("table.rdf tr:nth-child(even) { background-color: #fff; } ")
				.append("</style>")
				.append("<table class=\"rdf\">")
				.append("<tr><th>Subject</th><th>Predicate</th><th>Object</th></tr>");
		
		String baseUri = DrumbeatApplication.getInstance().getBaseUri();
		Map<String, String> nsPrefixMap = LinkedBuildingDataOntology.getDefaultNsPrefixes();
		Set<String> usedNsPrefixSet = new TreeSet<>();
		
		StmtIterator stmtIterator = model.listStatements();
		while(stmtIterator.hasNext()) {
			Statement statement = stmtIterator.nextStatement();
			stringBuilder
				.append("<tr><td>")
				.append(convertRdfNodeToHtml(statement.getSubject(), baseUri, nsPrefixMap, usedNsPrefixSet))
				.append("</td><td>")
				.append(convertRdfNodeToHtml(statement.getPredicate(), baseUri, nsPrefixMap, usedNsPrefixSet))
				.append("</td><td>")
				.append(convertRdfNodeToHtml(statement.getObject(), baseUri, nsPrefixMap, usedNsPrefixSet))
				.append("</td></tr>");
		}
		
		stringBuilder
				.append("</table>");
		
		if (!usedNsPrefixSet.isEmpty()) {
			stringBuilder
				.append("<br/>")
				.append("<h2>Prefixes</h2>")
				.append("<table class=\"rdf\">")
				.append("<tr><th>Prefix</th><th>URI</th></tr>");
			
			for (String prefix : usedNsPrefixSet) {
				String uri;
				if (prefix.equals("")) {
					prefix = "&lt;&gt;";
					uri = baseUri;
				} else {
					uri = nsPrefixMap.get(prefix);
					prefix += ":";
				}
					
				stringBuilder
					.append("<tr><td>")
					.append(prefix)
					.append("</td><td>")
					.append("<a href=\"")
					.append(uri)
					.append("\">")
					.append(uri)
					.append("</a")
					.append("</td></tr>");
			}
			
			stringBuilder
					.append("</table>");			
		}
		
		return stringBuilder
				.append("</body></html>")
				.toString();
	}
	
	private static String convertRdfNodeToHtml(RDFNode node, String baseUri, Map<String, String> nsPrefixMap, Set<String> usedNsPrefixSet) {
		if (node.isURIResource()) {
			
			String nodeString = node.toString();
			if (nodeString.startsWith(baseUri)) {
				nodeString = "&lt;" + nodeString.substring(baseUri.length()) + "&gt;";
				usedNsPrefixSet.add("");
			} else {
				String nameSpace = node.asResource().getNameSpace();				
				for (Entry<String,String> nsPrefix : nsPrefixMap.entrySet()) {
					if (nsPrefix.getValue().equals(nameSpace)) {
						nodeString = nsPrefix.getKey() + ":" + node.asResource().getLocalName();
						usedNsPrefixSet.add(nsPrefix.getKey());
						break;
					}
				}
			}
			return String.format("<a href=\"%s\">%s</a>", node.toString(), nodeString);
			
		} else if (node.isLiteral()) {
			
			String dataTypeUri = node.asLiteral().getDatatypeURI();
			if (dataTypeUri != null) {
				return String.format("\"%s\"^^%s", node.asLiteral().getLexicalForm(), dataTypeUri.replaceAll(XSD.getURI(), "xsd:"));				
			} else {
				return String.format("\"%s\"", node.toString());
			}
			
		} else {
			
			return node.toString();
			
		}
	}

	

}
