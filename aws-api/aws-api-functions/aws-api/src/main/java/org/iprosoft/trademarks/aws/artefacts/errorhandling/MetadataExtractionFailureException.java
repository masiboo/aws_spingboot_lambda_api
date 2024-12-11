package org.iprosoft.trademarks.aws.artefacts.errorhandling;

public class MetadataExtractionFailureException extends Exception {

	public MetadataExtractionFailureException() {
		super();
	}

	public MetadataExtractionFailureException(String message) {
		super(message);
	}

	public MetadataExtractionFailureException(String message, Throwable cause) {
		super(message, cause);
	}

	public MetadataExtractionFailureException(Throwable cause) {
		super(cause);
	}

}
