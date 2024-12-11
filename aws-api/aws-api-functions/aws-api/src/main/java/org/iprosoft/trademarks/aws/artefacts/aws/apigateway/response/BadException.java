package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response;

/** {@link Exception} that will return a 400 error. */
public class BadException extends Exception {

	/** serialVersionUID. */
	private static final long serialVersionUID = -3307625320614270509L;

	/**
	 * constructor.
	 * @param msg {@link String}
	 */
	public BadException(final String msg) {
		super(msg);
	}

}
