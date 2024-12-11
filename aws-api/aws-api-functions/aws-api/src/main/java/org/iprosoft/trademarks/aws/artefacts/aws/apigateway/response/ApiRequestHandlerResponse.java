package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response;

import java.util.HashMap;
import java.util.Map;

/** {@link ApiResponse} for Api Request Handler. */
public class ApiRequestHandlerResponse {

	/** HTTP Headers. */
	private Map<String, String> headers = new HashMap<>();

	/**
	 * {@link org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiResponseStatus}.
	 */
	private org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiResponseStatus status;

	/** {@link ApiResponse}. */
	private ApiResponse response;

	/**
	 * constructor.
	 * @param responseStatus
	 * {@link org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiResponseStatus}
	 * @param apiResponse {@link ApiResponse}
	 */
	public ApiRequestHandlerResponse(final ApiResponseStatus responseStatus, final ApiResponse apiResponse) {
		this.status = responseStatus;
		this.response = apiResponse;
	}

	/**
	 * Add HTTP Headers.
	 * @param key {@link String}
	 * @param value {@link String}
	 */
	public void addHeader(final String key, final String value) {
		this.headers.put(key, value);
	}

	/**
	 * Get HTTP Headers.
	 * @return {@link Map}
	 */
	public Map<String, String> getHeaders() {
		return this.headers;
	}

	/**
	 * Get {@link ApiResponse}.
	 * @return {@link ApiResponse}
	 */
	public ApiResponse getResponse() {
		return this.response;
	}

	/**
	 * Get {@link ApiResponse} Status.
	 * @return {@link ApiResponseStatus}
	 */
	public ApiResponseStatus getStatus() {
		return this.status;
	}

	/**
	 * Set Http Headers.
	 * @param map {@link Map}
	 */
	public void setHeaders(final Map<String, String> map) {
		this.headers = map;
	}

}
