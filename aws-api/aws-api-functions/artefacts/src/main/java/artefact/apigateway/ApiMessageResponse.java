package artefact.apigateway;


/** Custom Message {@link ApiResponse}. */
public class ApiMessageResponse implements ApiResponse {

  /** {@link String}. */
  private String message;

  /**
   * constructor.
   *
   */
  public ApiMessageResponse() {}

  /**
   * constructor.
   *
   * @param msg {@link String}
   */
  public ApiMessageResponse(final String msg) {
    this.message = msg;
  }

  /**
   * Get Api Response Message.
   *
   * @return {@link String}
   */
  public String getMessage() {
    return this.message;
  }

  @Override
  public String getNext() {
    return null;
  }

  @Override
  public String getPrevious() {
    return null;
  }

  /**
   * Set Api Response Message.
   * 
   * @param msg {@link String}
   */
  public void setMessage(final String msg) {
    this.message = msg;
  }
}
