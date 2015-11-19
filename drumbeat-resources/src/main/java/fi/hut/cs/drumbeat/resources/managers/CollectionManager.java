package fi.hut.cs.drumbeat.resources.managers;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import fi.hut.cs.drumbeat.resources.ontology.BuildingDataOntology;

public class CollectionManager {
	
	private final Model model;	
	
	public CollectionManager(Model model) {
		this.model = model;
	}
	
	
	public ResultSet getAll() {
		
		final QueryExecution queryExecution = 
				QueryExecutionFactory.create(
						QueryFactory.create(
								String.format("SELECT ?collection ?name WHERE { ?collection a <%s>; <%s> ?name . } ",
										BuildingDataOntology.Collections.Collection,
										BuildingDataOntology.Collections.name)),
						model);
		
		return queryExecution.execSelect();
		
	}
	
	public Resource get(String name) {
		return null;
	}
	
	
	public Resource create(String name) {
		
		
		return null;
		
	}
	

}
