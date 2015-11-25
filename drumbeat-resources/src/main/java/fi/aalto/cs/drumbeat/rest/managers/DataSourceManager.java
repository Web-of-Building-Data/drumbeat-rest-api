package fi.aalto.cs.drumbeat.rest.managers;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatchFilter;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.aalto.cs.drumbeat.rest.ontology.BuildingDataOntology;
import fi.aalto.cs.drumbeat.rest.ontology.BuildingDataVocabulary;
import fi.aalto.cs.drumbeat.rest.ontology.BuildingDataOntology.Datasets;;

//public class DataSourceManager {
//
//	private final Model model;	
//	
//	public DataSourceManager(Model model) {
//		this.model = model;
//	}
//	
//	public ResIterator listAll() {
//		return model.listSubjectsWithProperty(RDF.type, BuildingDataVocabulary.DataSource);
//	}
//	
//	public NodeIterator listAllByCollectionName(String collectionName) {
//		model
//			.listSubjectsWithProperty(RDF.type, BuildingDataVocabulary.Collection)
//			.filterKeep(
//				new Filter<Resource>() {
//					@Override
//					public boolean accept(Resource collectionResource) {
//						RDFNode collectionNameNode = collectionResource.getProperty(BuildingDataVocabulary.name).getObject();
//						assert(collectionNameNode != null && collectionNameNode.isLiteral());
//						return collectionNameNode.asLiteral().getValue().equals(collectionName);
//					}
//				});
//			
//		
//		
////		Resource collectionResource = model.createResource(collectionUri);
//		return model.listObjectsOfProperty(collectionResource, BuildingDataVocabulary.hasDataSource);
//	}
//	
//	public void create() {
//		
//	}
//
//}
