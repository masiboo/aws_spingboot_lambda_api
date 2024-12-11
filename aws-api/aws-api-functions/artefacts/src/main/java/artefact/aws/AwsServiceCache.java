package artefact.aws;

import artefact.aws.s3.S3ConnectionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import artefact.aws.dbaccess.DBAccessServiceImpl;
import artefact.aws.dynamodb.ArtefactJobServiceImpl;
import artefact.aws.dynamodb.BatchServiceImpl;
import artefact.aws.dynamodb.DocumentServiceImpl;
import artefact.aws.dynamodb.DynamoDbConnectionBuilder;
import artefact.usecase.ArtefactJobServiceInterface;
import artefact.usecase.ArtefactServiceInterface;
import artefact.usecase.BatchServiceInterface;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Objects;

public class AwsServiceCache {

    /** The number of minutes to hold in cache. */
    private static final int CACHE_MINUTES = 15;
    /** {@link DynamoDbConnectionBuilder}. */
    private DynamoDbConnectionBuilder dbConnection;
    /** {@link S3ConnectionBuilder}. */
    private S3ConnectionBuilder s3Connection;

    private S3Service s3Service;

    private ArtefactServiceInterface artefactServiceInterface;

    private ArtefactServiceInterface dbArtefactServiceInterface;

    private ArtefactJobServiceInterface jobServiceInterface;

    private BatchServiceInterface batchServiceInterface;

    private BatchServiceInterface ddBatchServiceInterface;

    private String dbDocumentsTable;

    private static final Logger logger = LoggerFactory.getLogger(AwsServiceCache.class);

    private boolean debug;
    /** Documents S3 Bucket. */
    private String artefactS3bucket;

    private String stages3bucket;
    /** App Environment. */
    private String appEnvironment;

    private String dbArtefactsTable;
    /** {@link String}. */
    private String dbJobCacheTable;

    public AwsServiceCache() {

    }

    /**
     * Get Documents S3 Bucket.
     *
     * @return {@link String}
     */
    public String documents3bucket() {
        return this.artefactS3bucket;
    }


    public String appEnvironment() {
        return this.appEnvironment;
    }

    /**
     * Set App Environment.
     *
     * @param env {@link String}
     * @return {@link AwsServiceCache}
     */
    public AwsServiceCache appEnvironment(final String env) {
        this.appEnvironment = env;
        return this;
    }

    public AwsServiceCache dbConnection(final DynamoDbConnectionBuilder connection,
                                        final String artefactTable, final String cacheTable) {
        this.dbConnection = connection;
        this.dbArtefactsTable = artefactTable;
        this.dbJobCacheTable = cacheTable;

        return this;
    }


    public void init() {
        this.dbConnection.initDbClient();
    }

    public ArtefactServiceInterface documentService() {
        logger.info("Step: 1, AwsServiceCache - 1");
        String serviceInterface = System.getenv("ARTEFACT_SERVICE_INTERFACE");

        logger.info("serviceInterface {}", serviceInterface);
        if (this.artefactServiceInterface == null) {
            if (Objects.equals(serviceInterface, "DBACCESS")) {

                logger.info("Step: 1, AwsServiceCache - 2");
                this.artefactServiceInterface = new DBAccessServiceImpl();
            } else {

                logger.info("Step: 1, AwsServiceCache - 3");
                this.artefactServiceInterface = new DocumentServiceImpl(this.dbConnection, this.dbArtefactsTable);
            }
        }

        logger.info("Step: 1, AwsServiceCache - 4");
        return this.artefactServiceInterface;
    }

    public ArtefactServiceInterface dbDocumentService() {
        logger.info("dbDocumentService");

        if (this.dbArtefactServiceInterface == null) {
            this.dbArtefactServiceInterface = new DBAccessServiceImpl();
        }
        return this.dbArtefactServiceInterface;
    }

    public ArtefactJobServiceInterface jobService() {
        if (this.jobServiceInterface == null) {
            this.jobServiceInterface = new ArtefactJobServiceImpl(this.dbConnection, this.dbArtefactsTable);
        }
        return this.jobServiceInterface;
    }

    public BatchServiceInterface batchService() {
        if (this.batchServiceInterface == null) {
            this.batchServiceInterface = new BatchServiceImpl(this.dbConnection, this.dbArtefactsTable);
        }
        return this.batchServiceInterface;
    }

    /**
     * Set {@link S3ConnectionBuilder}.
     *
     * @param connection {@link S3ConnectionBuilder}
     * @return {@link AwsServiceCache}
     */
    public AwsServiceCache s3Connection(final S3ConnectionBuilder connection) {
        this.s3Connection = connection;
        return this;
    }

    public S3Service s3Service() {
        if (this.s3Service == null) {
            this.s3Service = new S3Service(this.s3Connection);
        }
        return this.s3Service;
    }

    public S3Client s3Client() {
        if (this.s3Service == null) {
            this.s3Service = new S3Service(this.s3Connection);
        }
        return this.s3Service.buildClient();
    }

    public AwsServiceCache debug(final boolean isDebug) {
        this.debug = isDebug;
        return this;
    }

    public boolean debug() {
        return this.debug;
    }


    /**
     * Get Documents S3 Bucket.
     *
     * @return {@link String}
     */
    public String artefactS3bucket() {
        return this.artefactS3bucket;
    }

    /**
     * Set Documents S3 Bucket.
     *
     * @param s3bucket {@link String}
     * @return {@link AwsServiceCache}
     */
    public AwsServiceCache artefactS3bucket(final String s3bucket) {
        this.artefactS3bucket = s3bucket;
        return this;
    }


}
