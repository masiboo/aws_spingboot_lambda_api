package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response;

/** Http Status Codes. */
public enum ApiResponseStatus {

	/** {@code 200 OK} (HTTP/1.0 - RFC 1945). */
	SC_OK(200),
	/** {@code 201 CREATED} (HTTP/1.0 - RFC 1945). */
	SC_CREATED(201),
	/** {@code 202 ACCEPTED} (HTTP/1.0 - RFC 1945). */
	SC_ACCEPTED(202),
	/** Found (Previously "Moved Permanently"). */
	MOVED_PERMANENTLY(301),
	/** Found (Previously "Moved temporarily"). */
	SC_FOUND(302),
	/** {@code 303 SEE Other}. */
	SC_SEE_OTHER(303),
	/** Temporary Redirect. */
	SC_TEMPORARY_REDIRECT(307),
	/** {@code 400 bad request} (HTTP/1.0 - RFC 1945). */
	SC_BAD_REQUEST(400),
	/** {@code 401 bad request} (HTTP/1.0 - RFC 1945). */
	SC_UNAUTHORIZED(401),
	/** {@code 403 forbidden} (HTTP/1.0 - RFC 1945). */
	SC_FORBIDDEN(403),
	/** {@code 404 notfound} (HTTP/1.0 - RFC 1945). */
	SC_NOT_FOUND(404),
	/** {@code 409 conflict} (HTTP/1.1 RFC 7231). */
	SC_CONFLICT(409),
	/** {@code 429 Too Many Requests} (HTTP/1.0 - RFC 1945). */
	SC_TOO_MANY_REQUESTS(429),
	/** {@code 500 Server Error} (HTTP/1.0 - RFC 1945). */
	SC_ERROR(500),
	/** {@code 501 Server Error} (HTTP/1.0 - RFC 1945). */
	SC_NOT_IMPLEMENTED(501);

	/** Http Status Code. */
	private final int statusCode;

	/**
	 * constructor.
	 * @param code int
	 */
	ApiResponseStatus(final int code) {
		this.statusCode = code;
	}

	/**
	 * Get {@link ApiResponse} Status.
	 * @return int
	 */
	public int getStatusCode() {
		return this.statusCode;
	}

}
