package fi.aalto.cs.drumbeat.rest.managers;

import com.hp.hpl.jena.shared.AlreadyExistsException;
import com.hp.hpl.jena.shared.DeleteDeniedException;
import com.hp.hpl.jena.shared.NotFoundException;

import static fi.aalto.cs.drumbeat.rest.ontology.LinkedBuildingDataOntology.*;

import org.apache.commons.codec.language.bm.Lang;

public class ErrorFactory {
	
	public static NotFoundException createCollectionNotFoundException(String collectionId) {
		return new NotFoundException(
				String.format(
						"Collection not found: <%s>",
						formatCollectionResourceUri(collectionId)));
	}
	
	
	public static NotFoundException createDataSourceNotFoundException(String collectionId, String dataSourceId) {
		return new NotFoundException(
				String.format(
						"DataSource not found: <%s>",
						formatDataSourceResourceUri(collectionId, dataSourceId)));
	}
	

	public static NotFoundException createDataSetNotFoundException(String collectionId, String dataSourceId, String dataSetId) {
		return new NotFoundException(
				String.format(
						"DataSet not found: <%s>",
						formatDataSetResourceUri(collectionId, dataSourceId, dataSetId)));
	}
	
	
	public static NotFoundException createObjectNotFoundException(String collectionId, String dataSourceId, String dataSetId, String objectId) {
		return new NotFoundException(
				String.format(
						"Object not found: <%s>",
						formatObjectResourceUri(collectionId, dataSourceId, dataSetId, objectId)));
	}
	

	public static AlreadyExistsException createCollectionAlreadyExistsException(String collectionId) {
		return new AlreadyExistsException(
				String.format(
						"Collection already exists: <%s>",
						formatCollectionResourceUri(collectionId)));
	}
	
	
	public static AlreadyExistsException createDataSourceAlreadyExistsException(String collectionId, String dataSourceId) {
		return new AlreadyExistsException(
				String.format(
						"DataSource already exists: <%s>",
						formatDataSourceResourceUri(collectionId, dataSourceId)));
	}
	

	public static AlreadyExistsException createDataSetAlreadyExistsException(String collectionId, String dataSourceId, String dataSetId) {
		return new AlreadyExistsException(
				String.format(
						"DataSet already exists: <%s>",
						formatDataSetResourceUri(collectionId, dataSourceId, dataSetId)));
	}
	

	public static DeleteDeniedException createCollectionHasChildrenException(String collectionId) {
		return new DeleteDeniedException(
			String.format(
					"Collection has children: <%s>",
					formatCollectionResourceUri(collectionId)));
	}
	

	public static DeleteDeniedException createDataSourceHasChildrenException(String collectionId, String dataSourceId) {
		return new DeleteDeniedException(
			String.format(
					"DataSource has children: <%s>",
					formatDataSourceResourceUri(collectionId, dataSourceId)));
	}
	
	
	public static NotFoundException createLangNotFoundException(String lang) {
		return new NotFoundException(String.format("%s has no such field: %s", Lang.class, lang));
	}


	public static NotFoundException createCompressionFormatNotFoundException(String compressionFormat) {
		return new NotFoundException(String.format("Unknown compression format: %s", compressionFormat));
	}
	
	
}
