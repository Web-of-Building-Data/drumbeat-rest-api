package fi.aalto.cs.drumbeat.rest.managers;

import com.hp.hpl.jena.shared.AlreadyExistsException;
import com.hp.hpl.jena.shared.DeleteDeniedException;
import com.hp.hpl.jena.shared.NotFoundException;

import fi.aalto.cs.drumbeat.rest.common.NameFormatter;

import org.apache.commons.codec.language.bm.Lang;

public class ErrorFactory {
	
	public static NotFoundException createCollectionNotFoundException(String collectionId) {
		return new NotFoundException(
				String.format(
						"Collection not found: <%s>",
						NameFormatter.formatCollectionResourceUri(collectionId)));
	}
	
	public static AlreadyExistsException createCollectionAlreadyExistsException(String collectionId) {
		return new AlreadyExistsException(
				String.format(
						"Collection already exists: <%s>",
						NameFormatter.formatCollectionResourceUri(collectionId)));
	}
	
	public static DeleteDeniedException createCollectionHasChildrenException(String collectionId) {
		return new DeleteDeniedException(
			String.format(
					"Collection has children: <%s>",
					NameFormatter.formatCollectionResourceUri(collectionId)));
	}
	
	
	
	public static NotFoundException createDataSourceNotFoundException(String collectionId, String dataSourceId) {
		return new NotFoundException(
				String.format(
						"DataSource not found: <%s>",
						NameFormatter.formatDataSourceResourceUri(collectionId, dataSourceId)));
	}
	
	public static AlreadyExistsException createDataSourceAlreadyExistsException(String collectionId, String dataSourceId) {
		return new AlreadyExistsException(
				String.format(
						"DataSource already exists: <%s>",
						NameFormatter.formatDataSourceResourceUri(collectionId, dataSourceId)));
	}
	
	public static DeleteDeniedException createDataSourceHasChildrenException(String collectionId, String dataSourceId) {
		return new DeleteDeniedException(
			String.format(
					"DataSource has children: <%s>",
					NameFormatter.formatDataSourceResourceUri(collectionId, dataSourceId)));
	}
	


//	public static NotFoundException createLinkSourceNotFoundException(String collectionId, String linkSourceId) {
//		return new NotFoundException(
//				String.format(
//						"LinkSource not found: <%s>",
//						formatLinkSourceResourceUri(collectionId, linkSourceId)));
//	}
//	
//	public static AlreadyExistsException createLinkSourceAlreadyExistsException(String collectionId, String linkSourceId) {
//		return new AlreadyExistsException(
//				String.format(
//						"LinkSource already exists: <%s>",
//						formatLinkSourceResourceUri(collectionId, linkSourceId)));
//	}
//	
//	public static DeleteDeniedException createLinkSourceHasChildrenException(String collectionId, String linkSourceId) {
//		return new DeleteDeniedException(
//			String.format(
//					"LinkSource has children: <%s>",
//					formatLinkSourceResourceUri(collectionId, linkSourceId)));
//	}
	
	
	
	public static NotFoundException createDataSetNotFoundException(String collectionId, String dataSourceId, String dataSetId) {
		return new NotFoundException(
				String.format(
						"DataSet not found: <%s>",
						NameFormatter.formatDataSetResourceUri(collectionId, dataSourceId, dataSetId)));
	}
	
	public static AlreadyExistsException createDataSetAlreadyExistsException(String collectionId, String dataSourceId, String dataSetId) {
		return new AlreadyExistsException(
				String.format(
						"DataSet already exists: <%s>",
						NameFormatter.formatDataSetResourceUri(collectionId, dataSourceId, dataSetId)));
	}
	

	public static NotFoundException createLinkSetNotFoundException(String collectionId, String dataSourceId, String dataSetId) {
		return new NotFoundException(
				String.format(
						"LinkSet not found: <%s>",
						NameFormatter.formatLinkSetResourceUri(collectionId, dataSourceId, dataSetId)));
	}
	
	public static AlreadyExistsException createLinkSetAlreadyExistsException(String collectionId, String dataSourceId, String dataSetId) {
		return new AlreadyExistsException(
				String.format(
						"LinkSet already exists: <%s>",
						NameFormatter.formatLinkSetResourceUri(collectionId, dataSourceId, dataSetId)));
	}

	
	public static NotFoundException createObjectNotFoundException(String collectionId, String dataSourceId, String objectUri) {
		return new NotFoundException(
				String.format(
						"Object not found: <%s>",
						objectUri));
	}
	


	
	public static NotFoundException createRdfLangNotFoundException(String lang) {
		return new NotFoundException(String.format("%s has no such field: %s", Lang.class, lang));
	}


	public static NotFoundException createCompressionFormatNotFoundException(String compressionFormat) {
		return new NotFoundException(String.format("Unknown compression format: %s", compressionFormat));
	}
	
	public static NotFoundException createOntologyNotFoundException(String ontologyId) {
		return new NotFoundException(
				String.format(
						"Ontology not found: <%s>",
						NameFormatter.formatLocalOntologyUri(ontologyId)));
	}
	
	
	public static AlreadyExistsException createOntologyAlreadyExistsException(String ontologyId) {
		return new AlreadyExistsException(
				String.format(
						"Ontology already exists: <%s>",
						NameFormatter.formatLocalOntologyUri(ontologyId)));
	}

	public static IllegalArgumentException createInvalidOverwritingMethodException(String overwritingMethod) {
		return new IllegalArgumentException(String.format("Invalid overwriting method: %s", overwritingMethod));
	}

	public static IllegalArgumentException createInvalidLinkType(String linkType) {
		return new IllegalArgumentException(String.format("Invalid link type: %s", linkType));
	}
	
	
}
