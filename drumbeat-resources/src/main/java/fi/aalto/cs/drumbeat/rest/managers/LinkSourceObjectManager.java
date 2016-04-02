package fi.aalto.cs.drumbeat.rest.managers;

import org.apache.jena.rdf.model.Model;

import fi.aalto.cs.drumbeat.common.DrumbeatException;
import fi.aalto.cs.drumbeat.rdf.jena.provider.JenaProvider;

public class LinkSourceObjectManager extends DataSourceObjectManager {
	
	public LinkSourceObjectManager() throws DrumbeatException {
	}
	
	public LinkSourceObjectManager(Model metaDataModel, JenaProvider jenaProvider) {
		super(metaDataModel, jenaProvider);
	}	

	
}
