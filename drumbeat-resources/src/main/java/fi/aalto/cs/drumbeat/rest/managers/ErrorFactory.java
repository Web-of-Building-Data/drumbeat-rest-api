package fi.aalto.cs.drumbeat.rest.managers;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.AlreadyExistsException;
import com.hp.hpl.jena.shared.NotFoundException;

public class ErrorFactory {
	
	public static NotFoundException createCollectionNotFoundException(Resource collectionResource) {
		return new NotFoundException(
				String.format("Collection not found: <%s>", collectionResource.getURI()));
	}
	
	public static NotFoundException createDataSourceNotFoundException(Resource dataSourceResource) {
		return new NotFoundException(
				String.format("DataSource not found: <%s>", dataSourceResource.getURI()));
	}

	public static NotFoundException createDataSetNotFoundException(Resource dataSetResource) {
		return new NotFoundException(
				String.format("DataSet not found: <%s>", dataSetResource.getURI()));
	}
	
	public static AlreadyExistsException createCollectionAlreadyExistsException(Resource collectionResource) {
		return new AlreadyExistsException(
				String.format("Collection already exists: <%s>", collectionResource.getURI()));
	}
	
	public static AlreadyExistsException createDataSourceAlreadyExistsException(Resource dataSourceResource) {
		return new AlreadyExistsException(
				String.format("DataSource already exists: <%s>", dataSourceResource.getURI()));
	}

	public static AlreadyExistsException createDataSetAlreadyExistsException(Resource dataSetResource) {
		return new AlreadyExistsException(
				String.format("DataSet already exists: <%s>", dataSetResource.getURI()));
	}
}
