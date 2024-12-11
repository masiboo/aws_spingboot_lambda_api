package org.iprosoft.trademarks.aws.artefacts.model.mapper;

import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchOutput;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Operator;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.function.Function;

import static org.iprosoft.trademarks.aws.artefacts.util.AppConstants.KEY_LOCKED;

@Slf4j
public class AttributeValueDB2ToBatchMapper implements Function<Map<String, AttributeValue>, BatchOutput> {

	@Override
	public BatchOutput apply(Map<String, AttributeValue> stringAttributeValueMap) {
		if (stringAttributeValueMap == null) {
			throw new IllegalArgumentException("stringAttributeValueMap cannot be null");
		}
		log.info("AttributeValueDB2ToBatchMapper input stringAttributeValueMap: {}", stringAttributeValueMap);

		AttributeValue batchSequenceValue = stringAttributeValueMap.get("batchSequence");
		String id = (batchSequenceValue != null && batchSequenceValue.s() != null) ? batchSequenceValue.s() : "";

		BatchOutput item = new BatchOutput().withId(id).withBatchSequence(id);

		if (stringAttributeValueMap.containsKey("batchStatus")) {
			AttributeValue batchStatusValue = stringAttributeValueMap.get("batchStatus");
			if (batchStatusValue != null && batchStatusValue.s() != null) {
				item.setStatus(batchStatusValue.s());
			}
		}

		if (stringAttributeValueMap.containsKey("requestType")) {
			AttributeValue requestTypeValue = stringAttributeValueMap.get("requestType");
			if (requestTypeValue != null && requestTypeValue.s() != null) {
				item.setRequestType(requestTypeValue.s());
			}
		}

		if (stringAttributeValueMap.containsKey("user")) {
			AttributeValue userValue = stringAttributeValueMap.get("user");
			if (userValue != null && userValue.s() != null) {
				item.setUser(userValue.s());
			}
		}

		if (stringAttributeValueMap.containsKey("operator")) {
			AttributeValue operatorValue = stringAttributeValueMap.get("operator");
			if (operatorValue != null && operatorValue.s() != null) {
				item.setOperator(new Operator().withUsername(operatorValue.s()));
			}
		}

		if (stringAttributeValueMap.containsKey("reportDate")) {
			AttributeValue reportDateValue = stringAttributeValueMap.get("reportDate");
			if (reportDateValue != null && reportDateValue.s() != null) {
				item.setReportDate(reportDateValue.s());
			}
		}

		if (stringAttributeValueMap.containsKey("reportUrl")) {
			AttributeValue reportUrlValue = stringAttributeValueMap.get("reportUrl");
			if (reportUrlValue != null && reportUrlValue.s() != null) {
				item.setReportUrl(reportUrlValue.s());
			}
		}

		if (stringAttributeValueMap.containsKey("s3Bucket")) {
			AttributeValue reportUrlValue = stringAttributeValueMap.get("s3Bucket");
			if (reportUrlValue != null && reportUrlValue.s() != null) {
				item.setS3Bucket(reportUrlValue.s());
			}
		}

		if (stringAttributeValueMap.containsKey("s3Key")) {
			AttributeValue reportUrlValue = stringAttributeValueMap.get("s3Key");
			if (reportUrlValue != null && reportUrlValue.s() != null) {
				item.setS3Key(reportUrlValue.s());
			}
		}

		boolean isLocked = stringAttributeValueMap.containsKey(KEY_LOCKED)
				&& stringAttributeValueMap.get(KEY_LOCKED) != null
				&& stringAttributeValueMap.get(KEY_LOCKED).bool() != null
						? stringAttributeValueMap.get(KEY_LOCKED).bool() : false;
		item.setLocked(isLocked);
		log.info("BatchOutput item: {}", item);
		return item;
	}

}
