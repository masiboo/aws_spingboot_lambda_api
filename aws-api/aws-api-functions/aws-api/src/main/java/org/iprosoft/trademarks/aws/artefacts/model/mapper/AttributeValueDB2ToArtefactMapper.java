package org.iprosoft.trademarks.aws.artefacts.model.mapper;

import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactOutput;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.function.Function;

@Slf4j
public class AttributeValueDB2ToArtefactMapper implements Function<Map<String, AttributeValue>, ArtefactOutput> {

	@Override
	public ArtefactOutput apply(Map<String, AttributeValue> stringAttributeValueMap) {
		if (stringAttributeValueMap == null) {
			throw new IllegalArgumentException("stringAttributeValueMap cannot be null");
		}
		log.info("AttributeValueDB2ToArtefactMapper input stringAttributeValueMap: {}", stringAttributeValueMap);
		AttributeValue artefactIdValue = stringAttributeValueMap.get("artefactId");
		String id = (artefactIdValue != null && artefactIdValue.s() != null) ? artefactIdValue.s() : "";

		ArtefactOutput item = new ArtefactOutput().withId(id);

		if (stringAttributeValueMap.containsKey("batchSequence")) {
			AttributeValue batchSequenceValue = stringAttributeValueMap.get("batchSequence");
			if (batchSequenceValue != null && batchSequenceValue.s() != null) {
				item.setBatchSequence(batchSequenceValue.s());
			}
		}

		if (stringAttributeValueMap.containsKey("artefactContainerName")) {
			AttributeValue artefactContainerName = stringAttributeValueMap.get("artefactContainerName");
			if (artefactContainerName != null && artefactContainerName.s() != null) {
				item.setArtefactContainer(artefactContainerName.s());
			}
		}

		log.info("ArtefactOutput item: {}", item);
		return item;
	}

}
