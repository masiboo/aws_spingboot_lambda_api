package org.iprosoft.trademarks.aws.artefacts.aws.dynamodb;

/**
 *
 * Site Id Key Generator.
 *
 */
public final class SiteIdKeyGenerator {

	/** Default Site Id. */
	public static final String DEFAULT_SITE_ID = "default";

	/**
	 * Build DynamoDB PK that handles with/out siteId.
	 * @param id {@link String}
	 * @return {@link String}
	 */
	public static String createDatabaseKey(final String id) {
		return id;
	}

	/**
	 * Create S3 Key.
	 * @param siteId {@link String}
	 * @param id {@link String}
	 * @return {@link String}
	 */
	public static String createS3Key(final String siteId, final String id) {
		return createDatabaseKey(id);
	}

	/**
	 * Create S3 Key.
	 * @param siteId {@link String}
	 * @param id {@link String}
	 * @param contentType {@link String}
	 * @return {@link String}
	 */
	public static String createS3Key(final String siteId, final String id, final String contentType) {
		return createDatabaseKey(id + "/" + contentType);
	}

	/**
	 * Split {@link String} by Deliminator.
	 * @param s {@link String}
	 * @param element int
	 * @return {@link String}
	 */
	public static String getDeliminator(final String s, final int element) {
		String[] strs = s.split(DbKeys.TAG_DELIMINATOR);
		return strs.length > element ? strs[element] : null;
	}

	/**
	 * Get DocumentId from {@link String}.
	 * @param s {@link String}
	 * @return {@link String}
	 */
	public static String getDocumentId(final String s) {
		int pos = s != null ? s.indexOf("/") : 0;
		return pos > 0 && s != null ? s.substring(pos + 1) : s;
	}

	/**
	 * Get SiteId from {@link String}.
	 * @param s {@link String}
	 * @return {@link String}
	 */
	public static String getSiteId(final String s) {
		int pos = s != null ? s.indexOf("/") : 0;
		String siteId = pos > 0 && s != null ? s.substring(0, pos) : null;
		return !DEFAULT_SITE_ID.equals(siteId) ? siteId : null;
	}

	/**
	 * Is SiteId the Default site.
	 * @param siteId {@link String}
	 * @return boolean
	 */
	public static boolean isDefaultSiteId(final String siteId) {
		return siteId == null || DEFAULT_SITE_ID.equals(siteId);
	}

	/**
	 * Remove Key siteId from {@link String}.
	 * @param siteId {@link String}
	 * @param s {@link String}
	 * @return {@link String}
	 */
	public static String resetDatabaseKey(final String siteId, final String s) {

		String text = s;
		if (siteId != null && s != null) {
			text = s.replaceAll("^" + siteId + "\\/", "");
		}

		return text;
	}

	/**
	 * private constructor.
	 */
	private SiteIdKeyGenerator() {
	}

}
