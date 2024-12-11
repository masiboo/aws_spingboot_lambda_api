package org.iprosoft.trademarks.aws.artefacts.model.mapper;

import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactJob;
import org.iprosoft.trademarks.aws.artefacts.util.SafeParserUtil;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class AttributeValueToArtefactJobMapper implements Function<Map<String, AttributeValue>, ArtefactJob> {

	/**
	 * Applies this function to the given argument.
	 * @param map the function argument
	 * @return the function result
	 */
	@Override
	public ArtefactJob apply(Map<String, AttributeValue> map) {
		if (map == null) {
			throw new IllegalArgumentException("map cannot be null");
		}
		// log.info("AttributeValueToArtefactJobMapper input map = {}", map);
		AttributeValue jobIdValue = map.get("jobId");
		String jobId = (jobIdValue != null && jobIdValue.s() != null) ? jobIdValue.s() : "";

		ArtefactJob item = new ArtefactJob().withId(jobId);

		if (map.containsKey("jobId") && map.get("jobId") != null) {
			item.setId(map.get("jobId").s());
		}
		if (map.containsKey("path") && map.get("path") != null) {
			item.setPath(map.get("path").s());
		}
		if (map.containsKey("filename") && map.get("filename") != null) {
			item.setFilename(map.get("filename").s());
		}
		if (map.containsKey("status") && map.get("status") != null) {
			item.setStatus(map.get("status").s());
		}
		if (map.containsKey("jobStatus") && map.get("jobStatus") != null) {
			item.setStatus(map.get("jobStatus").s());
		}
		if (map.containsKey("s3_signed_url") && map.get("s3_signed_url") != null) {
			item.setS3SignedUrl(map.get("s3_signed_url").s());
		}
		if (map.containsKey("insertedDate") && map.get("insertedDate") != null) {
			item.setCreationDate(SafeParserUtil.safeParseZonedDateTime(map.get("insertedDate").s()));
		}
		if (map.containsKey("updatedDate") && map.get("updatedDate") != null) {
			item.setUpdatedDate(SafeParserUtil.safeParseZonedDateTime(map.get("updatedDate").s()));
		}
		if (map.containsKey("artefactId") && map.get("artefactId") != null) {
			item.setArtefactId(map.get("artefactId").s());
		}
		if (map.containsKey("requestId") && map.get("requestId") != null) {
			item.setRequestId(map.get("requestId").s());
		}
		if (map.containsKey("batchSequence") && map.get("batchSequence") != null) {
			item.setBatchSequence(map.get("batchSequence").s());
		}
		log.info("Filled ArtefactJob = {}", item);
		return item;
	}

}
