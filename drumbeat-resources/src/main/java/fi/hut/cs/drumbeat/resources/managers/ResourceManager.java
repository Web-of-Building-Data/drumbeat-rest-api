package fi.hut.cs.drumbeat.resources.managers;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class ResourceManager {
	
	private final Model model;	
	
	public ResourceManager(Model model) {
		this.model = model;
	}

	
	public Resource get(String name) {
		return null;
	}
	
	

}
