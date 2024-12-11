package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response;

/** API Response Object. */
public interface ApiResponse {

	/**
	 * Get Next Pagination token.
	 * @return {@link String}
	 */
	String getNext();

	/**
	 * Get Prev Pagination token.
	 * @return {@link String}
	 */
	String getPrevious();

}
