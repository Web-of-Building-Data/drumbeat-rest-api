package fi.aalto.cs.drumbeat.rest.common;

import com.fasterxml.jackson.core.JsonParseException;
import com.github.jsonldjava.core.JsonLdError;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.XSD;

import fi.aalto.cs.drumbeat.common.string.StringUtils;

import static javax.ws.rs.core.MediaType.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.ws.rs.NotSupportedException;
import javax.ws.rs.core.MediaType;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.system.PrefixMap;


public class MediaTypeConverter {
	
	public static String convertModel(Model model, MediaType mediaType, String baseUri) throws Exception {
		return convertModel(model, mediaType.getType() + "/" + mediaType.getSubtype(), baseUri);
	}
		
	public static String convertModel(Model model, String mediaTypeString, String baseUri)
		throws Exception
	{
		
		switch (mediaTypeString) {
		
//		case APPLICATION_JSON:
//			return convertModelToRdf(model, Lang.JSONLD, baseUri);
			
//		case APPLICATION_XML:
//			return convertModelToRdf(model, Lang.RDFXML, baseUri);

		case TEXT_HTML:
		case WILDCARD:
			return convertModelToHtml(model, baseUri);
			
		default:
			Lang lang = RDFLanguages.contentTypeToLang(mediaTypeString);
			if (lang != null) {
				return convertModelToRdf(model, lang, baseUri);
			} else {
				throw new NotSupportedException(
						"Unsupported media type: " + mediaTypeString);
			}
		}
		
	}
	
	public static String[] getSupportedMediaTypes() {
		
		List<String> mediaTypes = 
				RDFLanguages
					.getRegisteredLanguages()
					.stream()
					.map(x -> x.getContentType().toHeaderString())
					.distinct()
					.sorted()
					.collect(Collectors.toList());
		
		mediaTypes.add(0, TEXT_HTML);
		mediaTypes.add(0, WILDCARD);
		
		String[] result = new String[mediaTypes.size()];		
		return mediaTypes.toArray(result);
		
	}
	
//	public static String convertToRdfFormat(Model model, RDFFormat format) {
//		StringWriter out = new StringWriter();
//		model.setNsPrefixes(LinkedBuildingDataOntology.getDefaultNsPrefixes());
//		RDFDataMgr.write(out, model, format);		
//		return out.toString();
//	}
	
	public static String convertModelToRdf(Model model, Lang lang, String baseUri) throws JsonParseException, IOException, JsonLdError {
		
//		if (lang.equals(Lang.JSONLD)) {
//			JenaJSONLD.init();
//			baseUri = null;
//		} else
		if (lang.equals(Lang.RDFJSON)) {
			RIOT.init();			
		}		
		
		StringWriter writer = new StringWriter();

		model.setNsPrefixes(DrumbeatOntology.getDefaultNsPrefixes());
		
		//if (StringUtils.isEmptyOrNull(baseUri) && !lang.equals(Lang.JSONLD)) {
		if (StringUtils.isEmptyOrNull(baseUri)) {
			baseUri = DrumbeatApplication.getInstance().getBaseUri();
		}
		
		if (Lang.JSONLD.getAltNames().contains(lang.getName())) {
			model.setNsPrefix("base", baseUri);
			baseUri = null;
		}	
		
		model.write(writer, lang.getName(), baseUri);
		
//		if (lang.equals(Lang.JSONLD)) {
//			assert(tempLang.equals(Lang.RDFJSON));
//			
//			Object jsonObject = JsonUtils.fromString(writer.toString());
//			JsonLdOptions options = new JsonLdOptions(baseUri);
//			Map<Object, Object> context = new HashMap<>();
//			Object jsonCompact = JsonLdProcessor.compact(jsonObject, context, options);
//			return JsonUtils.toPrettyString(jsonCompact);
//		}
		
		return writer.toString();
	}

	public static String convertModelToHtml(Model model, String baseUri) {
		return convertModelToHtml(model, baseUri, true);
	}

	public static String convertModelToHtml(Model model, String baseUri, boolean supportSorting) {
		// TODO: Use local style sheet file
		
		StringBuilder stringBuilder = new StringBuilder()
				.append("<html>")
				.append("<head>")
				.append("<style type=\"text/css\">")
				.append("	table.rdf { width:100%; font-family:arial; font-size: 9pt; text-align: left; } ")
				.append("	table.rdf thead tr th { color:#fff; background-color: #000; } ")
				.append("	table.rdf tbody tr:nth-child(odd) { background-color: #e0e0e0; } ")
				.append("	table.rdf tbody tr:nth-child(even) { background-color: #fff; } ")
				.append("	table.rdf thead tr th, table.rdf tfoot tr th { border: 1px solid #FFF; font-size: 10pt; padding: 4px; } ")
				.append("	table.rdf thead tr .header { background-repeat: no-repeat; background-position: center right; cursor: pointer; } ")
				.append("	table.rdf  thead tr .headerSortUp { ")
				.append("		background-image: url(http://www.cypressgs.com/sbpmdemo/grid-filtering/extjs/resources/themes/images/access/grid/sort_asc.gif); }")
				.append("	table.rdf thead tr .headerSortDown { ")
				.append("		background-image: url(http://www.cypressgs.com/sbpmdemo/grid-filtering/extjs/resources/themes/images/access/grid/sort_desc.gif); }")
				.append("</style>");
//				.append("<link rel=\"stylesheet\" href=\"http://tablesorter.com/themes/blue/style.css\" type=\"text/css\" id=\"\" media=\"print, projection, screen\" />");
		
		if (supportSorting) {
			// TODO: use local script files and use min .js versions
			stringBuilder
				.append("<script type=\"text/javascript\" src=\"http://tablesorter.com/jquery-latest.js\"></script>")
				.append("<script type=\"text/javascript\" src=\"http://tablesorter.com/__jquery.tablesorter.js\"></script>")
				.append("<script type=\"text/javascript\">")
				.append("	$(document).ready(function(){")
				.append("		$(\"table#graph\").tablesorter(")
				.append("			{sortList: [[0,0],[1,0],[2,0]]});")
				.append("		$(\"table#prefixes\").tablesorter(")
				.append("			{sortList: [[0,0],[1,0]]});")
				.append("	});")
				.append("</script>");			
		}
		
		stringBuilder
				.append("</head><body>")
				.append("<table id=\"graph\" class=\"rdf tablesorter\">")
				.append("<thead><tr><th>Subject</th><th>Predicate</th><th>Object</th></tr></thead>")
				.append("<tbody>");
		
		if (StringUtils.isEmptyOrNull(baseUri)) {
			baseUri = DrumbeatApplication.getInstance().getBaseUri();
		}
		
		model.setNsPrefixes(DrumbeatOntology.getDefaultNsPrefixes());
		Map<String, String> nsPrefixMap = model.getNsPrefixMap();
		
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
				.append("</tbody>")
				.append("</table>");
		
		if (!usedNsPrefixSet.isEmpty()) {
			stringBuilder
				.append("<br/>")
				.append("<h2>Prefixes</h2>")
				.append("<table id=\"prefixes\" class=\"rdf\">")
				.append("<thead><tr><th>Prefix</th><th>URI</th></tr></thead>")
				.append("<tbody>");
			
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
					.append("</a>")
					.append("</td></tr>");
			}
			
			stringBuilder
					.append("</tbody>")			
					.append("</table>");			
		}
		
		return stringBuilder
				.append("</body></html>")
				.toString();
	}
	
	private static String convertRdfNodeToHtml(RDFNode node, String baseUri, Map<String, String> nsPrefixMap, Set<String> usedNsPrefixSet) {
		if (node.isURIResource()) {
			
			boolean useBrackets = true;

			String nodeString = node.toString();
			String href = nodeString;
			String nameSpace = node.asResource().getNameSpace();
			
			for (Entry<String,String> nsPrefix : nsPrefixMap.entrySet()) {
				if (nsPrefix.getValue().equals(nameSpace)) {
					usedNsPrefixSet.add(nsPrefix.getKey());
					useBrackets = false;

//					href = DrumbeatApplication.getInstance().getRealUri(nodeString, false);

					String localName = node.asResource().getLocalName();
					nodeString = nsPrefix.getKey() + ":" + localName;
					
					break;
				}
			}
			
			if (useBrackets && nodeString.startsWith(baseUri)) {
				href = DrumbeatApplication.getInstance().getRealUri(nodeString, false);
				nodeString = nodeString.substring(baseUri.length());
				usedNsPrefixSet.add("");
			}
			
			return String.format(
					"<a href=\"%s\">%s%s%s</a>",
					href,
					useBrackets ? "&lt;" : "",
					nodeString,
					useBrackets ? "&gt;" : "");
			
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
