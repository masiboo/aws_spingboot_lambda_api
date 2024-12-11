package org.iprosoft.trademarks.aws.artefacts.model.mapper;

import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchOutput;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Operator;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class AttributeValueToBatchOutputMapper implements Function<Map<String, AttributeValue>, BatchOutput> {

	/**
	 * Applies this function to the given argument.
	 * @param map the function argument
	 * @return the function result
	 */
	@Override
	public BatchOutput apply(Map<String, AttributeValue> map) {

		if (map == null) {
			throw new IllegalArgumentException("map cannot be null");
		}
		log.info("AttributeValueToBatchOutputMapper input map: {}", map);

		BatchOutput batchOutput = new BatchOutput();

		if (map.containsKey("id")) {
			AttributeValue idValue = map.get("id");
			if (idValue != null && idValue.s() != null) {
				batchOutput.setId(idValue.s());
			}
		}

		if (map.containsKey("batchSequence")) {
			AttributeValue batchSequenceValue = map.get("batchSequence");
			if (batchSequenceValue != null && batchSequenceValue.s() != null) {
				batchOutput.setBatchSequence(batchSequenceValue.s());
			}
		}

		if (map.containsKey("batchStatus")) {
			AttributeValue batchStatusValue = map.get("batchStatus");
			if (batchStatusValue != null && batchStatusValue.s() != null) {
				batchOutput.setStatus(batchStatusValue.s());
			}
		}

		if (map.containsKey("operator")) {
			AttributeValue operatorValue = map.get("operator");
			if (operatorValue != null && operatorValue.s() != null) {
				batchOutput.setOperator(new Operator().withUsername(operatorValue.s()));
			}
		}

		if (map.containsKey("requestType")) {
			AttributeValue requestTypeValue = map.get("requestType");
			if (requestTypeValue != null && requestTypeValue.s() != null) {
				batchOutput.setRequestType(requestTypeValue.s());
			}
		}

		if (map.containsKey("requestId")) {
			AttributeValue requestTypeValue = map.get("requestId");
			if (requestTypeValue != null && requestTypeValue.s() != null) {
				batchOutput.setRequestId(requestTypeValue.s());
			}
		}

		if (map.containsKey("locked")) {
			AttributeValue lockedValue = map.get("locked");
			if (lockedValue != null && lockedValue.bool() != null) {
				batchOutput.setLocked(lockedValue.bool());
			}
			else {
				batchOutput.setLocked(false);
			}
		}

		if (map.containsKey("jobs")) {
			AttributeValue jobs = map.get("jobs");
			if (jobs != null && jobs.hasSs()) {
				batchOutput.setJobIds(new ArrayList<>(jobs.ss()));
			}
		}
		log.info("BatchOutput batchOutput: {}", batchOutput);
		return batchOutput;
	}

}
