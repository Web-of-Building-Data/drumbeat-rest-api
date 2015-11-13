package fi.hut.cs.drumbeat.ldp.resources.managers;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class SiteManager {
	
	private final Model model;	
	
	public SiteManager(Model model) {
		this.model = model;
	}
	
	
	public ResultSet getAll() {
		
//		final String queryString = String.format("SELECT ?site ?name WHERE { ?site a <%s>; <%s> ?name . } ",
//				BuildingDataOntology.Sites.Site,
//				BuildingDataOntology.Sites.name);
////		System.out.println(queryString);
//		final Query query = QueryFactory.create(queryString);
		
		final QueryExecution queryExecution = 
				QueryExecutionFactory.create(
						QueryFactory.create(
								String.format("SELECT ?site ?name WHERE { ?site a <%s>; <%s> ?name . } ",
										BuildingDataOntology.Sites.Site,
										BuildingDataOntology.Sites.name)),
						model);
		
		return queryExecution.execSelect();
		
	}
	
	
	public Resource create(String name) {
		
//		Query query = QueryFactory.create("SELECT ?")
		
//		ResIterator it = model.listResourcesWithProperty(RDF.type, model.createResource(BuildingDataOntology.Sites.Site)).filterKeep();
//		if (it.hasNext()) {
//			throw new IllegalAr
//		}
		
		
		return null;
		
	}
	

}
