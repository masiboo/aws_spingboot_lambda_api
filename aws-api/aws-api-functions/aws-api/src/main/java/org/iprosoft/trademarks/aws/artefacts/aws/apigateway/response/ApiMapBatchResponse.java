package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response;

import org.iprosoft.trademarks.aws.artefacts.util.DocumentFormatType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** API Response Object. */
public class ApiMapBatchResponse implements ApiResponse {

	/**
	 * Document Id.
	 */
	private List<Map<String, Object>> mapList;

	private DocumentFormatType format;

	private Map<String, Object> responseMap;

	/**
	 * constructor.
	 */
	public ApiMapBatchResponse() {
	}

	public ApiMapBatchResponse(List<Map<String, Object>> mapList) {
		this.mapList = mapList;
	}

	public ApiMapBatchResponse(List<Map<String, Object>> mapList, DocumentFormatType format) {
		this.mapList = mapList;
		this.format = format;
	}

	public ApiMapBatchResponse(Map<String, Object> responseMap, DocumentFormatType format) {
		this.responseMap = responseMap;
		this.format = format;
		this.mapList = new ArrayList<>();
		this.mapList.add(responseMap);
	}

	@Override
	public String getNext() {
		return null;
	}

	@Override
	public String getPrevious() {
		return null;
	}

	public List<Map<String, Object>> getMapList() {
		return mapList;
	}

	public void setMapList(List<Map<String, Object>> mapList) {
		this.mapList = mapList;
	}

	public DocumentFormatType getFormat() {
		return format;
	}

	public void setFormat(DocumentFormatType format) {
		this.format = format;
	}

}
