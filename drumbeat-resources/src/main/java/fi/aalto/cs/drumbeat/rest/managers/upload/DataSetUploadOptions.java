package fi.aalto.cs.drumbeat.rest.managers.upload;

import static fi.aalto.cs.drumbeat.rest.common.NameFormatter.formatObjectResourceBaseUri;

import fi.aalto.cs.drumbeat.rest.common.NameFormatter;

public class DataSetUploadOptions {
	
	private String dataSetGraphUri;
	private String dataSourceObjectBaseUri;

	private String dataType;
	private String dataFormat;
	private boolean clearBefore;
	private boolean saveToFiles;
	
	public DataSetUploadOptions(
		String dataSetGraphUri,
		String dataSourceObjectBaseUri,
		String dataType,
		String dataFormat,
		boolean clearBefore,
		boolean saveToFiles)
	{
		this.dataSetGraphUri = dataSetGraphUri;
		this.dataSourceObjectBaseUri = dataSourceObjectBaseUri;
		this.dataType = dataType;
		this.dataFormat = dataFormat;
		this.clearBefore = clearBefore;
		this.saveToFiles = saveToFiles;
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
	
//	public String getDataSetUri() {
//		return NameFormatter.formatDataSetGraphUri(collectionId, dataSourceId, dataSetId);
//	}
//	
//	public String getObjectBaseUri() {
//		return NameFormatter.formatObjectResourceBaseUri(collectionId, dataSourceId);
//	}

}
