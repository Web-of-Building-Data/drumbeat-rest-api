package fi.aalto.cs.drumbeat.rest.managers.upload;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class Xml2RdfConverter {
	
	public static final String XML_ANY = "*";
	
	public Model convertXml2Rdf(InputStream in, DataSetUploadOptions options) throws ParserConfigurationException, SAXException, IOException {
		
//		Model model = ModelFactory.createDefaultModel();
//
//		DocumentBuilder documentBuilder;
//		documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//		Document document = documentBuilder.parse(in);
		
//		Element rootElement = document.getDocumentElement();
//		NodeList dataElements = rootElement.getElementsByTagName("list");
//		
//		for (int i = 0; i < dataElements.getLength(); ++i) {
//			Element dataElement = (Element) dataElements.item(i);
//			String resourceName = String.format("%s%03d", dataElement.getTagName(), i);
//			String resourceUri = formatUri(resourceName, options);
//			
//			String typeResourceName = dataElement.getAttribute("is");
//			String typeResourceUri = formatUri(typeResourceName, options);
//			model.add(model.createResource(resourceUri), RDF.type, model.createResource(typeResourceUri));			
//		}

		//		NodeList childElements = rootElement.getElementsByTagName(XML_ANY);
		
//		return model;
		
		throw new NotImplementedException("Converting XML to RDF is not implemented yet");			
	}
	
//	private String formatUri(String name, DataSetUploadOptions options) {
//		return options.getDataSetBlankObjectBaseUri() + name;
//	}

}
