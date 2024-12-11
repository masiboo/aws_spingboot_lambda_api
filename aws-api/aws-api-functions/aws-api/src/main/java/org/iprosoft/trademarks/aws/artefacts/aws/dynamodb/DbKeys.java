package org.iprosoft.trademarks.aws.artefacts.aws.dynamodb;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.iprosoft.trademarks.aws.artefacts.aws.dynamodb.SiteIdKeyGenerator.createDatabaseKey;

/**
 *
 * DynamoDB Keys.
 *
 */
public interface DbKeys {

	enum Type {

		CERTIFICATE("ARTEFACT#CERTIFICATE"), DOCUMENT("ARTEFACT#DOCUMENT"), BWLOGO("ARTEFACT#BWLOGO"),
		COLOURLOGO("ARTEFACT#COLOURLOGO"),

		SOUND("ARTEFACT#SOUND"), MULTIMEDIA("ARTEFACT#MULTIMEDIA");

		private final String key;

		Type(String key) {
			this.key = key;
		}

		public String getKey() {
			return this.key;
		}

	}

	/** Partition Key of Table. */
	String PK = "PK";

	/** Sort Key of Table. */
	String SK = "SK";

	/** Global Secondary Index 1. */
	String GSI1 = "GSI1";

	/** Global Secondary Index 1 Primary Key. */
	String GSI1_PK = GSI1 + PK;

	/** Global Secondary Index 1 Sort Key. */
	String GSI1_SK = GSI1 + SK;

	/** Global Secondary Index 2. */
	String GSI2 = "GSI2";

	/** Global Secondary Index 2 Primary Key. */
	String GSI2_PK = GSI2 + PK;

	/** Global Secondary Index 2 Sort Key. */
	String GSI2_SK = GSI2 + SK;

	/** Composite Tag Key Deliminator. */
	String TAG_DELIMINATOR = "#";

	/** Config Partition Key Prefix. */
	String PREFIX_CONFIG = "configs" + TAG_DELIMINATOR;

	/** Webhooks Partition Key Prefix. */
	String PREFIX_WEBHOOKS = "webhooks" + TAG_DELIMINATOR;

	/** Webhooks Partition Key Prefix. */
	String PREFIX_WEBHOOK = "webhook" + TAG_DELIMINATOR;

	/** Documents Partition Key Prefix. */
	String PREFIX_DOCS = "artefacts" + TAG_DELIMINATOR;

	/** TAGS Partition Key Prefix. */

	String PREFIX_JOBS = "jobs" + TAG_DELIMINATOR;

	String PREFIX_BATCH = "batch" + TAG_DELIMINATOR;

	/** TAGS Partition Key Prefix */
	String PREFIX_TAG = "tag" + TAG_DELIMINATOR;

	/** TAGS Partition Keys Prefix. */

	String PREFIX_TAGS = "tags" + TAG_DELIMINATOR;

	/** Document Date Time Series Partition Keys Prefix. */
	String PREFIX_DOCUMENT_DATE_TS = "artefactts" + TAG_DELIMINATOR;

	/** Document Date Partition Keys Prefix. */
	String PREFIX_DOCUMENT_DATE = "artefactdate";

	/** Document Format Prefix. */
	String PREFIX_DOCUMENT_FORMAT = "format" + TAG_DELIMINATOR;

	/** FORMATS Partition Key Prefix. */
	String PREFIX_FORMATS = "formats" + TAG_DELIMINATOR;

	/** Preset Partition Key Prefix. */
	String PREFIX_PRESETS = "pre";

	/** Preset Tag Partition Key Prefix. */
	String PREFIX_PRESETTAGS = "pretag";

	/**
	 * Add Number to {@link Map} {@link AttributeValue}.
	 * @param map {@link Map} {@link AttributeValue}
	 * @param key {@link String}
	 * @param value {@link String}
	 */
	default void addN(final Map<String, AttributeValue> map, final String key, final String value) {
		if (value != null) {
			map.put(key, AttributeValue.builder().n(value).build());
		}
	}

	/**
	 * Add {@link String} to {@link Map} {@link AttributeValue}.
	 * @param map {@link Map} {@link AttributeValue}
	 * @param key {@link String}
	 * @param value {@link String}
	 */
	default void addS(final Map<String, AttributeValue> map, final String key, final String value) {
		if (value != null) {
			map.put(key, AttributeValue.builder().s(value).build());
		}
	}

	default void addEnum(final Map<String, AttributeValue> map, final String key, final String value) {
		if (value != null) {
			map.put(key, AttributeValue.builder().s(value).build());
		}
	}

	default void addStringSet(final Map<String, AttributeValue> map, final String key, final List<String> value) {
		if (value != null) {
			map.put(key, AttributeValue.builder().ss(value).build());
		}
	}

	/**
	 * Get Db Index.
	 * @param pk {@link String}
	 * @return {@link String}
	 */
	default String getIndexName(String pk) {
		String index = null;

		if (pk.startsWith(GSI1)) {
			index = GSI1;
		}
		else if (pk.startsWith(GSI2)) {
			index = GSI2;
		}

		return index;
	}

	default Map<String, AttributeValue> keysDocument(String documentId) {
		return keysDocument(documentId, Optional.empty());
	}

	/**
	 * Document Key {@link AttributeValue}.
	 * @param documentId {@link String}
	 * @param childdocument {@link Optional} {@link String}
	 * @return {@link Map}
	 */
	default Map<String, AttributeValue> keysDocument(String documentId, Optional<String> childdocument) {
		return childdocument.isPresent()
				? keysGeneric(PREFIX_DOCS + documentId, "document" + TAG_DELIMINATOR + childdocument.get())
				: keysGeneric(PREFIX_DOCS + documentId, "document");
	}

	/**
	 * Document Formats Key {@link AttributeValue}.
	 * @param documentId {@link String}
	 * @param contentType {@link String}
	 * @return {@link Map}
	 */
	default Map<String, AttributeValue> keysDocumentFormats(String documentId, final String contentType) {
		String sk = contentType != null ? PREFIX_DOCUMENT_FORMAT + contentType : PREFIX_DOCUMENT_FORMAT;
		return keysGeneric(PREFIX_DOCS + documentId, sk);
	}

	/**
	 * Document Tag Key {@link AttributeValue}.
	 * @param siteId {@link String}
	 * @param documentId {@link String}
	 * @param tagKey {@link String}
	 * @return {@link Map}
	 */
	default Map<String, AttributeValue> keysDocumentTag(String siteId, String documentId, final String tagKey) {
		return keysGeneric(PREFIX_DOCS + documentId, tagKey != null ? PREFIX_TAGS + tagKey : PREFIX_TAGS);
	}

	/**
	 * Generic Key {@link AttributeValue}.
	 * @param pk {@link String}
	 * @param sk {@link String}
	 * @return {@link Map}
	 */
	// default Map<String, AttributeValue> keysGeneric(String pk, String sk) {
	// Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
	// key.put(PK, AttributeValue.builder().s(pk).build());
	//
	// if (sk != null) {
	// key.put(SK, AttributeValue.builder().s(sk).build());
	// }
	//
	// return key;
	// }

	/**
	 * Generic Key {@link AttributeValue}.
	 * @param pk {@link String}
	 * @param sk {@link String}
	 * @return {@link Map}
	 */
	default Map<String, AttributeValue> keysGeneric(String pk, String sk) {
		// 1. B. A:B
		return keysGeneric(PK, pk, SK, sk);
	}

	/**
	 * Generic Key {@link AttributeValue}.
	 * @param pkKey {@link String}
	 * @param pk {@link String}
	 * @param skKey {@link String}
	 * @param sk {@link String}
	 * @return {@link Map}
	 */
	default Map<String, AttributeValue> keysGeneric(String pkKey, String pk, String skKey, String sk) {
		Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
		key.put(pkKey, AttributeValue.builder().s(createDatabaseKey(pk)).build());

		if (sk != null) {
			key.put(skKey, AttributeValue.builder().s(sk).build());
		}

		return key;
	}

	/**
	 * Preset Key {@link AttributeValue}.
	 * @param id {@link String}
	 * @return {@link Map}
	 */
	default Map<String, AttributeValue> keysPreset(String id) {
		String sk = "preset";
		if (id == null) {
			throw new IllegalArgumentException("'id' required");
		}
		return keysGeneric(PREFIX_PRESETS + TAG_DELIMINATOR + id, sk);
	}

	/**
	 * Preset Key {@link AttributeValue}.
	 * @param siteId {@link String}
	 * @param id {@link String}
	 * @param type {@link String}
	 * @param name {@link String}s
	 * @return {@link Map}
	 */
	default Map<String, AttributeValue> keysPresetGsi2(String siteId, String id, final String type, final String name) {
		if (type == null) {
			throw new IllegalArgumentException("'type' required");
		}
		String pk = PREFIX_PRESETS + "_name";
		String sk = MessageFormat.format("{0}" + TAG_DELIMINATOR, type);

		if (name != null && id != null) {
			sk = MessageFormat.format("{0}" + TAG_DELIMINATOR + "{1}" + TAG_DELIMINATOR + "{2}", type, name, id);
		}
		else if (name != null) {
			sk = MessageFormat.format("{0}" + TAG_DELIMINATOR + "{1}" + TAG_DELIMINATOR, type, name);
		}

		return keysGeneric(GSI2_PK, pk, GSI2_SK, sk);
	}

	/**
	 * Preset Key Tag {@link AttributeValue}.
	 * @param siteId {@link String}
	 * @param id {@link String}
	 * @param tag {@link String}
	 * @return {@link Map}
	 */
	default Map<String, AttributeValue> keysPresetTag(String siteId, String id, final String tag) {
		if (id == null) {
			throw new IllegalArgumentException("'id' required");
		}
		return keysGeneric(PREFIX_PRESETTAGS + TAG_DELIMINATOR + id, tag);
	}

	/**
	 * Convert PK / SK values to Query {@link AttributeValue} map.
	 * @param keys {@link Map}
	 * @return {@link Map}
	 */
	default Map<String, AttributeValue> queryKeys(final Map<String, AttributeValue> keys) {

		Map<String, AttributeValue> map = new HashMap<>(2);

		if (keys.containsKey(PK)) {
			map.put(":" + PK.toLowerCase(), keys.get(PK));
		}

		if (keys.containsKey(SK)) {
			map.put(":" + SK.toLowerCase(), keys.get(SK));
		}

		return map;
	}

	default String getDefaultTypeKey() {
		return Type.DOCUMENT.getKey();
	}

	static String getTypeKey(String docType) {
		Type keyByDocType;
		try {
			keyByDocType = Type.valueOf(docType.toUpperCase());
		}
		catch (IllegalArgumentException | NullPointerException e) {
			throw new IllegalArgumentException("Invalid ArtefactType Passed :" + docType);
		}
		return keyByDocType.getKey();
	}

}
