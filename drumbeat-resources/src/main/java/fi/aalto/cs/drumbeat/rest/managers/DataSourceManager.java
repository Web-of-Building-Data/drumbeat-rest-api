package fi.aalto.cs.drumbeat.rest.managers;


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
