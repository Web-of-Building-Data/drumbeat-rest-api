//package fi.hut.cs.drumbeat.ldp.resources.managers;
//
//import com.hp.hpl.jena.query.Query;
//import com.hp.hpl.jena.query.QueryExecution;
//import com.hp.hpl.jena.query.QueryExecutionFactory;
//import com.hp.hpl.jena.query.QueryFactory;
//import com.hp.hpl.jena.query.ResultSet;
//import com.hp.hpl.jena.rdf.model.Model;
//import com.hp.hpl.jena.rdf.model.Resource;
//
//public class ResourceManager {
//
//	private Model model;
//
//	public ResourceManager(Model model) {
//		this.model = model;
//	}
//
//	public ResultSet listIn(String url) {
//
//		final String queryString = String.format("SELECT ?subject ?property WHERE { ?subject ?property <%s>. } ", url);
//		System.out.println(queryString);
//		final Query query = QueryFactory.create(queryString);
//
//		QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
//
//		return queryExecution.execSelect();
//
//	}
//
//	public ResultSet listOut(String url) {
//
//		final String queryString = String.format("SELECT ?property ?object WHERE { <%s> ?property ?object. } ", url);
//		System.out.println(queryString);
//		final Query query = QueryFactory.create(queryString);
//
//		QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
//
//		return queryExecution.execSelect();
//
//	}
//
//}
