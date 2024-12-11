package org.iprosoft.trademarks.aws.artefacts.model.mapper;

import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactBatch;
import org.iprosoft.trademarks.aws.artefacts.aws.dynamodb.DbKeys;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.function.Function;

@Slf4j
public class AttributeValueToBatchedArtefactMapper implements Function<Map<String, AttributeValue>, ArtefactBatch> {

	/**
	 * Applies this function to the given argument.
	 * @param valueMap the function argument
	 * @return the function result
	 */
	@Override
	public ArtefactBatch apply(Map<String, AttributeValue> valueMap) {
		if (valueMap == null) {
			throw new IllegalArgumentException("valueMap cannot be null");
		}
		log.info("AttributeValueToBatchedArtefactMapper input valueMap: {}", valueMap);
		ArtefactBatch artefactBatch = new ArtefactBatch();

		if (valueMap.containsKey("artefactId")) {
			AttributeValue artefactIdValue = valueMap.get("artefactId");
			if (artefactIdValue != null && artefactIdValue.s() != null) {
				artefactBatch.setArtefactId(artefactIdValue.s());
			}
		}

		if (valueMap.containsKey("type")) {
			AttributeValue typeValue = valueMap.get("type");
			if (typeValue != null && typeValue.s() != null) {
				artefactBatch.setArtefactClassType(typeValue.s());
			}
		}

		if (valueMap.containsKey("fileName")) {
			AttributeValue fileNameValue = valueMap.get("fileName");
			if (fileNameValue != null && fileNameValue.s() != null) {
				artefactBatch.setFilename(fileNameValue.s());
			}
		}

		if (valueMap.containsKey("path")) {
			AttributeValue pathValue = valueMap.get("path");
			if (pathValue != null && pathValue.s() != null) {
				artefactBatch.setPath(pathValue.s());
			}
		}

		if (valueMap.containsKey("contentType")) {
			AttributeValue contentTypeValue = valueMap.get("contentType");
			if (contentTypeValue != null && contentTypeValue.s() != null) {
				artefactBatch.setContentType(contentTypeValue.s());
			}
		}

		if (valueMap.containsKey("requestType")) {
			AttributeValue requestTypeValue = valueMap.get("requestType");
			if (requestTypeValue != null && requestTypeValue.s() != null) {
				artefactBatch.setRequestType(requestTypeValue.s());
			}
		}

		if (valueMap.containsKey("creationDate")) {
			AttributeValue creationDateValue = valueMap.get("creationDate");
			if (creationDateValue != null && creationDateValue.s() != null) {
				artefactBatch.setCreationDate(creationDateValue.s());
			}
		}

		if (valueMap.containsKey("userId")) {
			AttributeValue userIdValue = valueMap.get("userId");
			if (userIdValue != null && userIdValue.s() != null) {
				artefactBatch.setUser(userIdValue.s());
			}
		}

		if (valueMap.containsKey("jobId")) {
			AttributeValue jobIdValue = valueMap.get("jobId");
			if (jobIdValue != null && jobIdValue.s() != null) {
				artefactBatch.setJobId(jobIdValue.s());
			}
		}

		if (valueMap.containsKey("artefactName")) {
			AttributeValue artefactNameValue = valueMap.get("artefactName");
			if (artefactNameValue != null && artefactNameValue.s() != null) {
				artefactBatch.setArtefactName(artefactNameValue.s());
			}
		}

		if (valueMap.containsKey("s3Bucket")) {
			AttributeValue s3BucketValue = valueMap.get("s3Bucket");
			if (s3BucketValue != null && s3BucketValue.s() != null) {
				artefactBatch.setS3Bucket(s3BucketValue.s());
			}
		}

		if (valueMap.containsKey("s3Key")) {
			AttributeValue s3KeyValue = valueMap.get("s3Key");
			if (s3KeyValue != null && s3KeyValue.s() != null) {
				artefactBatch.setS3Key(s3KeyValue.s());
			}
		}

		if (valueMap.containsKey("s3Url")) {
			AttributeValue s3UrlValue = valueMap.get("s3Url");
			if (s3UrlValue != null && s3UrlValue.s() != null) {
				artefactBatch.setS3Url(s3UrlValue.s());
			}
		}

		if (valueMap.containsKey("status")) {
			AttributeValue statusValue = valueMap.get("status");
			if (statusValue != null && statusValue.s() != null) {
				artefactBatch.setStatus(statusValue.s());
			}
		}

		if (valueMap.containsKey("mirisDocId")) {
			AttributeValue mirisDocIdValue = valueMap.get("mirisDocId");
			if (mirisDocIdValue != null && mirisDocIdValue.s() != null) {
				artefactBatch.setMirisDocId(mirisDocIdValue.s());
			}
		}

		if (valueMap.containsKey("contentLength")) {
			AttributeValue contentLengthValue = valueMap.get("contentLength");
			if (contentLengthValue != null && contentLengthValue.s() != null) {
				artefactBatch.setContentLength(contentLengthValue.s());
			}
		}

		if (valueMap.containsKey("sizeWarning")) {
			AttributeValue sizeWarningValue = valueMap.get("sizeWarning");
			if (sizeWarningValue != null && sizeWarningValue.bool() != null) {
				artefactBatch.setSizeWarning(sizeWarningValue.bool());
			}
		}

		if (valueMap.containsKey("MERGED_ARTEFACT_ID")) {
			AttributeValue mergedArtefactIdValue = valueMap.get("MERGED_ARTEFACT_ID");
			if (mergedArtefactIdValue != null && mergedArtefactIdValue.s() != null) {
				artefactBatch.setArtefactMergeId(mergedArtefactIdValue.s());
			}
		}

		if (valueMap.containsKey("pageNumber")) {
			AttributeValue pageNumberValue = valueMap.get("pageNumber");
			if (pageNumberValue != null && pageNumberValue.s() != null) {
				artefactBatch.setPage(pageNumberValue.s());
			}
		}

		// #todo:mpd807
		if (valueMap.containsKey("artefactMergeId")) {
			AttributeValue artefactMergeIdValue = valueMap.get("artefactMergeId");
			if (artefactMergeIdValue != null && artefactMergeIdValue.s() != null) {
				artefactBatch.setArtefactMergeId(artefactMergeIdValue.s());
			}
		}

		if (valueMap.containsKey("artefaactItemFileName")) {
			AttributeValue artefaactItemFileNameValue = valueMap.get("artefaactItemFileName");
			if (artefaactItemFileNameValue != null && artefaactItemFileNameValue.s() != null) {
				artefactBatch.setArtefactItemFileName(artefaactItemFileNameValue.s());
			}
		}

		if (valueMap.containsKey("BatchSequenceId")) {
			AttributeValue batchSequenceIdValue = valueMap.get("BatchSequenceId");
			if (batchSequenceIdValue != null && batchSequenceIdValue.s() != null) {
				artefactBatch.setBatchSequence(batchSequenceIdValue.s());
			}
		}

		if (valueMap.containsKey("validationStatus")) {
			AttributeValue validationStatusValue = valueMap.get("validationStatus");
			if (validationStatusValue != null && validationStatusValue.s() != null) {
				artefactBatch.setValidationStatus(validationStatusValue.s());
			}
		}
		// log.info("ArtefactBatch artefactBatch: {}", artefactBatch);
		return artefactBatch;
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
	public <V> Function<V, ArtefactBatch> compose(Function<? super V, ? extends Map<String, AttributeValue>> before) {
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
	public <V> Function<Map<String, AttributeValue>, V> andThen(Function<? super ArtefactBatch, ? extends V> after) {
		return Function.super.andThen(after);
	}

	private String classTypeDecode(String valueTypeAtt) {
		return valueTypeAtt.substring(valueTypeAtt.lastIndexOf(DbKeys.TAG_DELIMINATOR) + 1);
	}

}
