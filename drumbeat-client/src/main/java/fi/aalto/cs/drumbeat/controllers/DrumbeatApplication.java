package fi.aalto.cs.drumbeat.controllers;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class DrumbeatApplication {
	
	public static final String SERVER_URL_ARCHITECT = "http://architect.drb.cs.hut.fi/";
	public static final String SERVER_URL_STRUCTURAL = "http://structural.drb.cs.hut.fi/";
	
	public static final String[] SERVER_URLS = new String[] { SERVER_URL_ARCHITECT, SERVER_URL_STRUCTURAL };
	
	public static final Lang RDF_LANG_DEFAULT = Lang.TURTLE;
	
	
	public static Model parseModel(String content) {
		Model model = ModelFactory.createDefaultModel();
		InputStream in = new BufferedInputStream(new ByteArrayInputStream(content.getBytes()));
		RDFDataMgr.read(model, in, RDF_LANG_DEFAULT);
		return model;
	}

}
