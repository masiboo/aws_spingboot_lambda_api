package org.iprosoft.trademarks.aws.artefacts.model.mapper;

import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;
import org.iprosoft.trademarks.aws.artefacts.aws.dynamodb.DbKeys;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.function.Function;

@Slf4j
public class AttributeValueToArtefactMapper implements Function<Map<String, AttributeValue>, Artefact> {

	/**
	 * Applies this function to the given argument.
	 * @param valueMap the function argument
	 * @return the function result
	 */
	@Override
	public Artefact apply(Map<String, AttributeValue> valueMap) {
		if (valueMap == null) {
			throw new IllegalArgumentException("valueMap cannot be null");
		}
		log.info("AttributeValueToArtefactMapper input valueMap: {}", valueMap);
		Artefact artefact = new Artefact();
		if (valueMap.containsKey("artefactId")) {
			AttributeValue artefactIdValue = valueMap.get("artefactId");
			if (artefactIdValue != null && artefactIdValue.s() != null) {
				artefact.setId(artefactIdValue.s());
			}
		}

		if (valueMap.containsKey("type")) {
			AttributeValue artefactClassTypeValue = valueMap.get("type");
			if (artefactClassTypeValue != null && artefactClassTypeValue.s() != null) {
				artefact.setArtefactClassType(classTypeDecode(artefactClassTypeValue.s()));
			}
		}

		if (valueMap.containsKey("fileName")) {
			AttributeValue fileNameValue = valueMap.get("fileName");
			if (fileNameValue != null && fileNameValue.s() != null) {
				artefact.setArtefactName(fileNameValue.s());
			}
		}

		if (valueMap.containsKey("s3Key")) {
			AttributeValue s3KeyValue = valueMap.get("s3Key");
			if (s3KeyValue != null && s3KeyValue.s() != null) {
				artefact.setS3Key(s3KeyValue.s());
			}
		}

		if (valueMap.containsKey("s3Bucket")) {
			AttributeValue s3BucketValue = valueMap.get("s3Bucket");
			if (s3BucketValue != null && s3BucketValue.s() != null) {
				artefact.setS3Bucket(s3BucketValue.s());
			}
		}

		if (valueMap.containsKey("status")) {
			AttributeValue statusValue = valueMap.get("status");
			if (statusValue != null && statusValue.s() != null) {
				artefact.setStatus(statusValue.s());
			}
		}

		if (valueMap.containsKey("mirisDocId")) {
			AttributeValue mirisDocIdValue = valueMap.get("mirisDocId");
			if (mirisDocIdValue != null && mirisDocIdValue.s() != null) {
				artefact.setMirisDocId(mirisDocIdValue.s());
			}
		}

		if (valueMap.containsKey("contentLength")) {
			AttributeValue contentLengthValue = valueMap.get("contentLength");
			if (contentLengthValue != null && contentLengthValue.n() != null) {
				artefact.setContentLength(contentLengthValue.n());
			}
		}

		if (valueMap.containsKey("sizeWarning")) {
			AttributeValue sizeWarningValue = valueMap.get("sizeWarning");
			if (sizeWarningValue != null && sizeWarningValue.bool() != null) {
				artefact.setSizeWarning(sizeWarningValue.bool());
			}
		}

		if (valueMap.containsKey("insertedDate")) {
			AttributeValue insertDateValue = valueMap.get("insertedDate");
			if (insertDateValue != null && insertDateValue.s() != null) {
				artefact.setIndexationDate(DateUtils.parseToZonedDateTime(insertDateValue.s()));
			}
		}
		log.info("Artefact artefact: {}", artefact);
		return artefact;
	}

	/**
	 * Returns a composed function that first applies the {@code before} function to its
	 * input, and then applies this function to the result. If evaluation of either
	 * function throws an exception, it is relayed to the caller of the composed function.
	 * @param before the function to apply before this function is applied
	 * @return a composed function that first applies the {@code before} function and then
	 * applies this function
	 * @throws NullPointerException if before is null
	 * @see #andThen(Function)
	 */
	@Override
	public <V> Function<V, Artefact> compose(Function<? super V, ? extends Map<String, AttributeValue>> before) {
		return Function.super.compose(before);
	}

	/**
	 * Returns a composed function that first applies this function to its input, and then
	 * applies the {@code after} function to the result. If evaluation of either function
	 * throws an exception, it is relayed to the caller of the composed function.
	 * @param after the function to apply after this function is applied
	 * @return a composed function that first applies this function and then applies the
	 * {@code after} function
	 * @throws NullPointerException if after is null
	 * @see #compose(Function)
	 */
	@Override
	public <V> Function<Map<String, AttributeValue>, V> andThen(Function<? super Artefact, ? extends V> after) {
		return Function.super.andThen(after);
	}

	private String classTypeDecode(String valueTypeAtt) {
		return valueTypeAtt.substring(valueTypeAtt.lastIndexOf(DbKeys.TAG_DELIMINATOR) + 1);
	}

}
