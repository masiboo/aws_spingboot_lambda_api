package org.iprosoft.trademarks.aws.artefacts.model.mapper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactTag;
import org.iprosoft.trademarks.aws.artefacts.model.entity.DocumentTagType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.iprosoft.trademarks.aws.artefacts.aws.dynamodb.DbKeys.*;
import static org.iprosoft.trademarks.aws.artefacts.aws.dynamodb.SiteIdKeyGenerator.createDatabaseKey;

@Slf4j
@AllArgsConstructor
public class DocumentTagToAttributeValueMapper implements Function<ArtefactTag, List<Map<String, AttributeValue>>> {

	private final String document;

	private final SimpleDateFormat df;

	private final String keyPrefix;

	@Override
	public List<Map<String, AttributeValue>> apply(final ArtefactTag tag) {
		log.info("ArtefactTag tag: {}", tag);
		String site = null;
		List<Map<String, AttributeValue>> list = new ArrayList<>();
		if (isValueList(tag)) {
			int idx = -1;
			for (String tagValue : tag.getValues()) {
				Map<String, AttributeValue> pkValues = buildTagAttributeValue(site, this.document, tag, tagValue, idx);
				list.add(pkValues);
				idx++;
				if (idx == 0) {
					idx++;
				}
			}

		}
		else {
			Map<String, AttributeValue> pkValues = buildTagAttributeValue(site, this.document, tag, tag.getValue(), -1);
			list.add(pkValues);
		}
		log.info("Output list: {}", list);
		return list;
	}

	private Map<String, AttributeValue> buildTagAttributeValue(final String siteId, final String documentId,
			final ArtefactTag tag, final String tagValue, final int tagValueIndex) {
		log.info("siteId: {}, documentId: {}, tag: {}, tagValue: {}, tagValueIndex: {}", siteId, documentId, tag,
				tagValue, tagValueIndex);
		String tagKey = tag.getKey();
		String fullDate = this.df.format(tag.getInsertedDate());

		DocumentTagType type = tag.getDocumentTagType() != null ? tag.getDocumentTagType()
				: DocumentTagType.USERDEFINED;

		Map<String, AttributeValue> pkValues = new HashMap<>();

		String pk = createDatabaseKey(this.keyPrefix + documentId);
		String sk = tagValueIndex > -1 ? PREFIX_TAGS + tagKey + TAG_DELIMINATOR + "idx" + tagValueIndex
				: PREFIX_TAGS + tagKey;

		pkValues.put(PK, AttributeValue.builder().s(pk).build());
		pkValues.put(SK, AttributeValue.builder().s(sk).build());

		pkValues.put(GSI1_PK,
				AttributeValue.builder()
					.s(createDatabaseKey(PREFIX_TAG + tagKey + TAG_DELIMINATOR + tagValue))
					.build());
		pkValues.put(GSI1_SK, AttributeValue.builder().s(fullDate + TAG_DELIMINATOR + documentId).build());

		pkValues.put(GSI2_PK, AttributeValue.builder().s(createDatabaseKey(PREFIX_TAG + tagKey)).build());
		pkValues.put(GSI2_SK,
				AttributeValue.builder()
					.s(tagValue + TAG_DELIMINATOR + fullDate + TAG_DELIMINATOR + documentId)
					.build());

		pkValues.put("documentId", AttributeValue.builder().s(documentId).build());

		pkValues.put("type", AttributeValue.builder().s(type.name()).build());

		if (tagKey != null) {
			pkValues.put("tagKey", AttributeValue.builder().s(tagKey).build());
		}

		if (tagValue != null) {
			pkValues.put("tagValue", AttributeValue.builder().s(tagValue).build());
		}

		if (tag.getValues() != null) {
			List<AttributeValue> values = tag.getValues()
				.stream()
				.map(s -> AttributeValue.builder().s(s).build())
				.collect(Collectors.toList());
			pkValues.put("tagValues", AttributeValue.builder().l(values).build());
		}

		if (tag.getUserId() != null) {
			pkValues.put("userId", AttributeValue.builder().s(tag.getUserId()).build());
		}
		pkValues.put("inserteddate", AttributeValue.builder().s(fullDate).build());
		log.info("pkValues: {}", pkValues);
		return pkValues;
	}

	private boolean isValueList(final ArtefactTag tag) {
		return tag.getValues() != null && !tag.getValues().isEmpty();
	}

}
