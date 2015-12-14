package fi.aalto.cs.drumbeat.rest.managers;

import java.util.List;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.ontology.LinkedBuildingDataOntology;

public abstract class MetaDataManager {
	
	private final Model metaDataModel;
	
	public MetaDataManager(Model metaDataModel) {
		this.metaDataModel = metaDataModel; 
	}
	
	public Model getMetaDataModel() {
		return metaDataModel;
	}
	
	public Resource getCollectionResource(String collectionId) {
		return metaDataModel.createResource(
				LinkedBuildingDataOntology.formatCollectionResourceUri(
						DrumbeatApplication.getInstance().getBaseUri(),
						collectionId));
	}
	
	public Resource getDataSourceResource(String collectionId, String dataSourceId) {
		return metaDataModel.createResource(
				LinkedBuildingDataOntology.formatDataSourceResourceUri(
						DrumbeatApplication.getInstance().getBaseUri(),						
						collectionId,
						dataSourceId));
	}

	public Resource getDataSetResource(String collectionId, String dataSourceId, String dataSetId) {
		return metaDataModel.createResource(
				LinkedBuildingDataOntology.formatDataSetResourceUri(
						DrumbeatApplication.getInstance().getBaseUri(),						
						collectionId,
						dataSourceId,
						dataSetId));
	}
	
	public static Model convertResultSetToModel(ResultSet resultSet) {
		List<String> varNames = resultSet.getResultVars();
		if (varNames.size() != 3) {
			throw new IllegalArgumentException(
					String.format("Numer of columns: expected <3>, actual <%d>", varNames.size()));
		}
		
		Model resultModel = ModelFactory.createDefaultModel();
		
		while (resultSet.hasNext()) {
			QuerySolution row = resultSet.next();
			
			Resource subject = row.getResource(varNames.get(0));
			if (subject == null) {
				throw new NullPointerException("Expected non-null resource for being statement subject");
			}
			
			Property predicate = row.getResource(varNames.get(1)).as(Property.class);
			if (predicate == null) {
				throw new NullPointerException("Expected non-null resource for being statement predicate");
			}

			RDFNode object = row.get(varNames.get(2));
			if (object == null) {
				throw new NullPointerException("Expected non-null rdfNode for being statement object");
			}

			resultModel.add(subject, predicate, object);
		}
		
		return resultModel;
	}
	
}