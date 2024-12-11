package org.iprosoft.trademarks.aws.artefacts.util;

import software.amazon.awssdk.utils.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class S3UrlValidatorUtil {

	private static final String S3_BUCKET_NAME_REGEX = "^[a-z0-9.-]{3,63}$";

	private static final String S3_OBJECT_KEY_REGEX = "^[a-zA-Z0-9!_.*'()/-]+$";

	private static final String URL_PATTERN = ".*(s3|http|https).*";

	public static boolean isValidS3Url(String url) {
		if (!StringUtils.isNotBlank(url)) {
			return false;
		}
		if (!containsS3OrHttp(url)) {
			return false;
		}

		try {
			URI uri = new URI(url);

			// Check host (bucket name)
			String bucketName = uri.getHost();
			if (bucketName == null || !bucketName.matches(S3_BUCKET_NAME_REGEX)) {
				return false;
			}

			// Check path (object key)
			String objectKey = uri.getPath();
			if (objectKey == null || objectKey.isEmpty() || !objectKey.matches(S3_OBJECT_KEY_REGEX)) {
				return false;
			}
			return true;
		}
		catch (URISyntaxException e) {
			return false;
		}
	}

	public static boolean containsS3OrHttp(String url) {
		Pattern pattern = Pattern.compile(URL_PATTERN, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(url);
		return matcher.matches();
	}

}
