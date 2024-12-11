package org.iprosoft.trademarks.aws.artefacts.util;

import org.iprosoft.trademarks.aws.artefacts.aws.dynamodb.DbKeys;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactFilterCriteria;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class QueryRequestBuilder {

	public QueryRequest.Builder prepareQueryByCriteria(ArtefactFilterCriteria criteria) {
		String indexName;
		String keyCondtionExpression;
		String filterCondition;
		if (StringUtils.hasText(criteria.getMirisDocId())) {
			indexName = AppConstants.INDEX_MIRIS_DOC_ID;
			keyCondtionExpression = "mirisDocId = :mirisDocId";
			filterCondition = constructFilterCondition(getDateFilterExpression(criteria),
					getTypeFilterExpression(criteria));

		}
		else if (StringUtils.hasText(criteria.getDocType())) {
			indexName = AppConstants.INDEX_DOC_TYPE;
			keyCondtionExpression = "type = :type";
			filterCondition = constructFilterCondition(getDateFilterExpression(criteria),
					getDocIdFilterExpression(criteria));
		}
		else if (StringUtils.hasText(criteria.getInsertedDate())) {
			indexName = AppConstants.INDEX_INSERTED_DATE;
			keyCondtionExpression = "insertedDate = :insertedDate";
			filterCondition = constructFilterCondition(getDocIdFilterExpression(criteria),
					getTypeFilterExpression(criteria));
		}
		else {
			throw new RuntimeException("Invalid filter condition provided");
		}
		QueryRequest.Builder qrBuilder = QueryRequest.builder()
			.tableName(SystemEnvironmentVariables.REGISTRY_TABLE_NAME)
			.indexName(indexName)
			.keyConditionExpression(keyCondtionExpression)
			.expressionAttributeValues(getExpressionAttributeValues(criteria));
		if (StringUtils.hasText(filterCondition))
			qrBuilder.filterExpression(filterCondition);
		return qrBuilder;

	}

	private String constructFilterCondition(String expression, String anotherExpression) {
		return Stream.of(expression, anotherExpression)
			.filter(Objects::nonNull)
			.map(Object::toString)
			.collect(Collectors.joining(" AND "));
	}

	private Map<String, AttributeValue> getExpressionAttributeValues(ArtefactFilterCriteria filterCriteria) {
		Map<String, AttributeValue> expressionValues = new HashMap<>();
		if (StringUtils.hasText(filterCriteria.getInsertedDate())) {
			expressionValues.put(":insertedDate", AttributeValue.builder().s(filterCriteria.getInsertedDate()).build());
		}
		else if (StringUtils.hasText(filterCriteria.getDateFrom()) && StringUtils.hasText(filterCriteria.getDateTo())) {
			expressionValues.put(":startDate", AttributeValue.builder().s(filterCriteria.getDateFrom()).build());
			expressionValues.put(":endDate", AttributeValue.builder().s(filterCriteria.getDateTo()).build());
		}

		if (StringUtils.hasText(filterCriteria.getMirisDocId())) {
			expressionValues.put(":mirisDocId", AttributeValue.builder().s(filterCriteria.getMirisDocId()).build());
		}

		if (StringUtils.hasText(filterCriteria.getDocType())) {
			expressionValues.put(":type",
					AttributeValue.builder().s(DbKeys.getTypeKey(filterCriteria.getDocType())).build());
		}
		return expressionValues;
	}

	private String getTypeFilterExpression(ArtefactFilterCriteria criteria) {
		String filterExpression = null;
		if (StringUtils.hasText(criteria.getDocType())) {
			filterExpression = "type = :type";
		}
		return filterExpression;

	}

	private String getDocIdFilterExpression(ArtefactFilterCriteria criteria) {
		String filterExpression = null;
		if (StringUtils.hasText(criteria.getDocType())) {
			filterExpression = "mirisDocId = :mirisDocId";
		}
		return filterExpression;

	}

	private String getBatchStatusFilterExpression(ArtefactFilterCriteria criteria) {
		String filterExpression = null;
		if (StringUtils.hasText(criteria.getBatchStatus())) {
			filterExpression = "batchStatus = :batchStatus";
		}
		return filterExpression;

	}

	private String getDateFilterExpression(ArtefactFilterCriteria filterCriteria) {
		String filterExpression = null;
		if (StringUtils.hasText(filterCriteria.getInsertedDate())) {
			filterExpression = "insertedDate = :insertedDate";
		}
		else if (StringUtils.hasText(filterCriteria.getDateFrom()) && StringUtils.hasText(filterCriteria.getDateTo())) {
			filterExpression = "insertedDate BETWEEN :startDate AND :endDate";
		}
		return filterExpression;
	}

	private String getReportDateFilterExpression(ArtefactFilterCriteria filterCriteria) {
		String filterExpression = null;
		if (StringUtils.hasText(filterCriteria.getInsertedDate())) {
			filterExpression = "reportDate < :insertedDate";
		}
		else if (StringUtils.hasText(filterCriteria.getDateFrom()) && StringUtils.hasText(filterCriteria.getDateTo())) {
			filterExpression = "reportDate BETWEEN :reportDate < :insertedDate";
		}
		return filterExpression;
	}

}
