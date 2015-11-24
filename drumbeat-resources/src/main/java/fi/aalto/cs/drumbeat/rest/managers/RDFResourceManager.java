package fi.aalto.cs.drumbeat.rest.managers;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class RDFResourceManager {
	
	private final Model model;	
	
	public RDFResourceManager(Model model) {
		this.model = model;
	}

	
	public Resource get(String name) {
		return null;
	}
	
	

}
