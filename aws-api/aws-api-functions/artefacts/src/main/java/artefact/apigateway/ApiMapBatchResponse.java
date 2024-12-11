package artefact.apigateway;

import artefact.entity.DocumentFormatType;

import java.util.List;
import java.util.Map;

/** API Response Object. */
public class ApiMapBatchResponse implements ApiResponse {

  /**
   * Document Id.
   */
  private List<Map<String, Object>> mapList;

  private DocumentFormatType format;


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


