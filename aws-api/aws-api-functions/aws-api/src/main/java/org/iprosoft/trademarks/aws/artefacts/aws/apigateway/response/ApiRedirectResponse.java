package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response;

/** Redirect Response . */
public class ApiRedirectResponse implements ApiResponse {

	/** Redirect URI. */
	private String redirectUri;

	/**
	 * constructor.
	 * @param uri {@link String}
	 */
	public ApiRedirectResponse(final String uri) {
		this.redirectUri = uri;
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
	 * Get Redirect URI.
	 * @return {@link String}
	 */
	public String getRedirectUri() {
		return this.redirectUri;
	}

}
