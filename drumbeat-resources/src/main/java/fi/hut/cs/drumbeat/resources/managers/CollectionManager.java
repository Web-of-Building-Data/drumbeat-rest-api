package fi.hut.cs.drumbeat.resources.managers;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class CollectionManager {
	
	private final Model model;	
	
	public CollectionManager(Model model) {
		this.model = model;
	}
	
	
	public ResultSet getAll() {
		
//		final String queryString = String.format("SELECT ?collection ?name WHERE { ?collection a <%s>; <%s> ?name . } ",
//				BuildingDataOntology.Collections.Collection,
//				BuildingDataOntology.Collections.name);
////		System.out.println(queryString);
//		final Query query = QueryFactory.create(queryString);
		
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
		
//		Query query = QueryFactory.create("SELECT ?")
		
//		ResIterator it = model.listResourcesWithProperty(RDF.type, model.createResource(BuildingDataOntology.Collections.Collection)).filterKeep();
//		if (it.hasNext()) {
//			throw new IllegalAr
//		}
		
		
		return null;
		
	}
	

}
