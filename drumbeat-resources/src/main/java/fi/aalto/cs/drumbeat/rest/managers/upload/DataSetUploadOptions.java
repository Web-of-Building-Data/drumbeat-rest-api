package fi.aalto.cs.drumbeat.rest.managers.upload;

public class DataSetUploadOptions {
	
	private final String collectionId;
	private final String dataSourceId;
	private final String dataSetId;
	
	private String dataSetGraphUri;
	private String dataSourceObjectBaseUri;
	private String dataSetBlankObjectBaseUri;

	private String dataType;
	private String dataFormat;
	private boolean clearBefore;
	private boolean saveToFiles;
	
	public DataSetUploadOptions(
		String collectionId,
		String dataSourceId,
		String dataSetId,
		String dataSetGraphUri,
		String dataSourceObjectBaseUri,
		String dataSetBlankObjectBaseUri,
		String dataType,
		String dataFormat,
		boolean clearBefore,
		boolean saveToFiles)
	{
		this.collectionId = collectionId;
		this.dataSourceId = dataSourceId;
		this.dataSetId = dataSetId;
		
		this.dataSetGraphUri = dataSetGraphUri;
		this.dataSourceObjectBaseUri = dataSourceObjectBaseUri;
		this.dataSetBlankObjectBaseUri = dataSetBlankObjectBaseUri;
		
		this.dataType = dataType;
		this.dataFormat = dataFormat;
		this.clearBefore = clearBefore;
		this.saveToFiles = saveToFiles;
	}
	
	public String getCollectionId() {
		return collectionId;
	}
	
	public String getDataSourceId() {
		return dataSourceId;
	}
	
	public String getDataSetId() {
		return dataSetId;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(String dataFormat) {
		this.dataFormat = dataFormat;
	}

	public boolean isClearBefore() {
		return clearBefore;
	}

	public void setClearBefore(boolean clearBefore) {
		this.clearBefore = clearBefore;
	}

	public boolean isSaveToFiles() {
		return saveToFiles;
	}

	public void setSaveToFiles(boolean saveToFiles) {
		this.saveToFiles = saveToFiles;
	}

	public String getDataSetGraphUri() {
		return dataSetGraphUri;
	}

	public void setDataSetGraphUri(String dataSetGraphUri) {
		this.dataSetGraphUri = dataSetGraphUri;
	}

	public String getDataSourceObjectBaseUri() {
		return dataSourceObjectBaseUri;
	}

	public void setDataSourceObjectBaseUri(String dataSourceObjectBaseUri) {
		this.dataSourceObjectBaseUri = dataSourceObjectBaseUri;
	}

	public String getDataSetBlankObjectBaseUri() {
		return dataSetBlankObjectBaseUri;
	}

	public void setDataSetBlankObjectBaseUri(String dataSetBlankObjectBaseUri) {
		this.dataSetBlankObjectBaseUri = dataSetBlankObjectBaseUri;
	}
	
	
//	public String getDataSetUri() {
//		return NameFormatter.formatDataSetGraphUri(collectionId, dataSourceId, dataSetId);
//	}
//	
//	public String getObjectBaseUri() {
//		return NameFormatter.formatObjectResourceBaseUri(collectionId, dataSourceId);
//	}

}
