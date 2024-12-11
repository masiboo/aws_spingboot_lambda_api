package org.iprosoft.trademarks.aws.artefacts.configuration;

public class AppConstants {

	public static final String KEY_MESSAGE = "message";

	public static final String KEY_STATUS = "status";

	public static final String KEY_MIRIS_DOCID = "mirisDocId";

	public static final String KEY_LOCKED = "locked";

	public static final String MERGED_PDF_ARTEFACT_NAME = "merged_artefact_documents.pdf";

	// S3 Metadata keys
	public static final String METADATA_KEY_ARTEFACT_ID = "artefact-id-key";

	public static final String METADATA_KEY_TRACE_ID = "job-id-key";

	public static final String METADATA_KEY_BATCH_SEQ = "batch-seq-key";

	public static final String METADATA_KEY_MIRIS_DOCID = "miris-doc-id";

	public static final String METADATA_KEY_IS_MERGED_FILE = "is-merged-file";

	public static final String JWT_SECRET_KEY = "JWTSecretKey";

	// index key
	public static final String INDEX_MIRIS_DOC_ID = "GSI-Artefact-2";

	public static final String INDEX_DOC_TYPE = "GSI-Artefact-4";

	public static final String INDEX_INSERTED_DATE = "GSI-Artefact-5";

}
