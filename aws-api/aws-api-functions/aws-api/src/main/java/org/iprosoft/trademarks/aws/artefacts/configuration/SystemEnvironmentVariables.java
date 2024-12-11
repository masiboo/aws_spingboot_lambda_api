package org.iprosoft.trademarks.aws.artefacts.configuration;

import lombok.NoArgsConstructor;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.AwsEnvironmentEnum;
import org.springframework.context.annotation.Configuration;

@Configuration
@NoArgsConstructor
public class SystemEnvironmentVariables {

	public static final String REGISTRY_TABLE_NAME = System.getenv().get("REGISTRY_TABLE_NAME");

	public static final String AUDIT_EVENT_TABLE_NAME = System.getenv().get("AUDIT_EVENT_TABLE_NAME");

	public static final String ARTEFACTS_S3_BUCKET = System.getenv().get("ARTEFACTS_S3_BUCKET");

	public static final String BATCH_S3_REPORTS_BUCKET = System.getenv().get("BATCH_S3_REPORTS_BUCKET");

	public static final String API_VERSION = System.getenv().get("API_VERSION");

	public static final String CORE_VERSION = System.getenv().get("CORE_VERSION");

	public static final String MIRIS_CHECK_ENABLED = System.getenv().get("MIRIS_CHECK_ENABLED");

	public static final String Aws_CORE_MIRIS_CHECK_API_URL = System.getenv().get("Aws_CORE_MIRIS_PROXY_API_URL");

	public static final String Aws_CORE_EMAIL_SERVICE_API_URL = System.getenv().get("Aws_CORE_EMAIL_SERVICE_API_URL");

	public static final String Aws_CORE_DB_INIT_ACCESS_URL = System.getenv().get("Aws_CORE_DB_ACCESS_API_URL");

	public static final String SOUND_FILE_SIZE_LIMIT = System.getenv().get("SOUND_FILE_SIZE_LIMIT");

	public static final String MULTIMEDIA_FILE_SIZE_LIMIT = System.getenv().get("MULTIMEDIA_FILE_SIZE_LIMIT");

	public static final String Aws_CORE_MEDIA_PROCESS_API_URL = System.getenv().get("Aws_CORE_MEDIA_PROCESS_API_URL");

	public static final String UNBLOCK_UI = System.getenv().get("UNBLOCK_UI");

	public static final String Aws_API_SIGNED_URL_NAME = System.getenv().get("Aws_API_SIGNED_URL_NAME");

	public static final String Aws_ENVIRONMENT = System.getenv("Aws_ENVIRONMENT") != null
			? System.getenv("Aws_ENVIRONMENT") : AwsEnvironmentEnum.DEV.name();

	public static final Boolean Aws_SQS_INSTRUMENTAL = Boolean
		.parseBoolean(System.getenv().getOrDefault("Aws_SQS_INSTRUMENTAL", "true"));

}
