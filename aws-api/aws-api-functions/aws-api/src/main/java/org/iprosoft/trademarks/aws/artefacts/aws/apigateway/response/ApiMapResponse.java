package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response;

import java.util.Map;

/** API Response Object. */
public class ApiMapResponse implements ApiResponse {

	/** Document Id. */
	private Map<String, Object> map;

	/** constructor. */
	public ApiMapResponse() {
	}

	/**
	 * constructor.
	 * @param m {@link Map}
	 */
	public ApiMapResponse(final Map<String, Object> m) {
		this.map = m;
	}

	@Override
	public String getNext() {
		return (String) this.map.get("next");
	}

	@Override
	public String getPrevious() {
		return (String) this.map.get("previous");
	}

	@Override
	public String toString() {
		return this.map.toString();
	}

	/**
	 * Get {@link Map}.
	 * @return {@link Map}
	 */
	public Map<String, Object> getMap() {
		return this.map;
	}

	/**
	 * Set {@link Map}.
	 * @param m {@link Map}
	 */
	public void setMap(final Map<String, Object> m) {
		this.map = m;
	}

}
