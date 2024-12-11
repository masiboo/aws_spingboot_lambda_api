package org.iprosoft.trademarks.aws.artefacts.aws.dynamodb;

import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchOutput;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactClassType;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactJob;
import org.iprosoft.trademarks.aws.artefacts.model.entity.PagedArtefactJobs;
import org.iprosoft.trademarks.aws.artefacts.model.mapper.AttributeValueToArtefactJobMapper;
import org.iprosoft.trademarks.aws.artefacts.model.mapper.AttributeValueToArtefactMapper;
import org.iprosoft.trademarks.aws.artefacts.model.mapper.AttributeValueToBatchOutputMapper;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DynamoDbPartiQ {

	public static ExecuteStatementResponse executeStatementRequest(DynamoDbClient ddb, String statement,
			List<AttributeValue> parameters) {
		ExecuteStatementRequest request = ExecuteStatementRequest.builder()
			.statement(statement)
			.parameters(parameters)
			.build();
		return ddb.executeStatement(request);
	}

	public static ExecuteStatementResponse executeStatementRequestWithLimit(DynamoDbClient ddb, String statement,
			List<AttributeValue> parameters, String limit, String nextToken) {
		ExecuteStatementRequest request = ExecuteStatementRequest.builder()
			.statement(statement)
			.parameters(parameters)
			.limit(Integer.valueOf(limit))
			.nextToken(nextToken)
			.build();
		return ddb.executeStatement(request);
	}

	public static List<Artefact> getAllArtefacts(DynamoDbClient ddb, String tableName, Map<String, String> conditions) {
		List<Artefact> artefacts = new ArrayList<>();
		StringBuilder query = new StringBuilder("SELECT * FROM ");
		query.append("\"")
			.append(tableName)
			.append("\"")
			.append(".")
			.append("\"GSI-Artefact-1\"")
			.append(" WHERE 1=1 ");
		if (StringUtils.hasText(conditions.get("status"))) {
			query.append(" AND status='").append(conditions.get("status")).append("'");
		}
		if (StringUtils.hasText(conditions.get("date"))) {
			query.append(" AND begins_with(insertedDate , '").append(conditions.get("date")).append("')");
		}
		log.info("DynamoDb PartiQL query: {}", query);
		try {
			ExecuteStatementResponse response = executeStatementRequest(ddb, query.toString(), null);
			if (response != null) {
				log.info("ExecuteStatement successful: {}", response);
				List<Map<String, AttributeValue>> results = response.items();

				for (Map<String, AttributeValue> result : results) {
					Artefact artefact = new AttributeValueToArtefactMapper().apply(result);
					assert artefact != null;
					artefacts.add(artefact);
				}
			}
		}
		catch (DynamoDbException e) {
			log.error("DynamoDbException while getAllArtefacts : {}", e.getMessage());
		}
		return artefacts;
	}

	public static List<Artefact> getAllArtefactsByInterval(DynamoDbClient ddb, String tableName,
			Map<String, String> conditions) {
		List<Artefact> artefacts = new ArrayList<>();
		StringBuilder query = new StringBuilder("SELECT * FROM ");
		query.append("\"")
			.append(tableName)
			.append("\"")
			.append(".")
			.append("\"GSI-Artefact-1\"")
			.append(" WHERE 1=1 ");
		if (StringUtils.hasText(conditions.get("status"))) {
			query.append(" AND status='").append(conditions.get("status")).append("'");
		}
		if (StringUtils.hasText(conditions.get("fromDate")) && StringUtils.hasText(conditions.get("untilDate"))) {
			query.append(" AND insertedDate BETWEEN '")
				.append(conditions.get("fromDate"))
				.append("'")
				.append(" AND '")
				.append(conditions.get("untilDate"))
				.append("'");
		}
		log.info("DynamoDb PartiQL query: {}", query);
		try {
			ExecuteStatementResponse response = executeStatementRequest(ddb, query.toString(), null);
			if (response != null) {
				log.info("ExecuteStatement successful: {}", response);
				List<Map<String, AttributeValue>> results = response.items();

				for (Map<String, AttributeValue> result : results) {
					Artefact artefact = new AttributeValueToArtefactMapper().apply(result);
					assert artefact != null;
					artefacts.add(artefact);
				}
			}
		}
		catch (DynamoDbException e) {
			log.error("DynamoDbException while getAllArtefactsByInterval : {}", e.getMessage());
		}
		return artefacts;
	}

	public static List<Artefact> getArtefactsByMirisDocidAndType(DynamoDbClient ddb, String tableName, String docId,
			List<String> types) {
		List<Artefact> artefacts = null;
		String tableIndexName = "\"" + tableName + "\"" + "." + "\"GSI-Artefact-2\" ";
		try {
			List<AttributeValue> parameters = new ArrayList<>();
			parameters.add(AttributeValue.builder().s(docId).build());
			for (String artefactClassType : types) {
				parameters.add(AttributeValue.builder().s(artefactClassType).build());
			}
			String[] placeholders = new String[parameters.size() - 1]; // exclude docId
																		// PlaceHolder
			Arrays.fill(placeholders, "?");
			String phWithSeperator = String.join(",", placeholders);
			String sqlState = " SELECT * FROM " + tableIndexName + " where \"mirisDocId\"  = ? and \"type\" in("
					+ phWithSeperator + ")";
			log.info("sqlState :" + sqlState);
			// Get items in the table and write out the ID value.
			// https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/example_dynamodb_Scenario_PartiQLSingle_section.html
			ExecuteStatementResponse response = executeStatementRequest(ddb, sqlState, parameters);
			if (response != null) {
				log.info("ExecuteStatement successful: " + response);
				List<Map<String, AttributeValue>> results = response.items();
				artefacts = results.stream()
					.map(new AttributeValueToArtefactMapper())
					.filter(Objects::nonNull)
					.filter(artefact -> ArtefactStatus.INDEXED.getStatus().equalsIgnoreCase(artefact.getStatus())
							&& !ArtefactClassType.PART.name().equalsIgnoreCase(artefact.getArtefactClassType()))
					.collect(Collectors.toList());
			}
		}
		catch (DynamoDbException e) {
			log.error("DynamoDbException while getArtefactsByMirisDocidAndType {}", e.getMessage());
		}
		return artefacts;
	}

	public static List<Artefact> getArtefactsByMirisDocid(DynamoDbClient ddb, String tableName, String docId) {
		List<Artefact> artefacts = new ArrayList<>();
		String tableIndexName = "\"" + tableName + "\"" + "." + "\"GSI-Artefact-2\" ";
		try {
			List<AttributeValue> parameters = new ArrayList<>();
			AttributeValue att1 = AttributeValue.builder().s(docId).build();
			parameters.add(att1);
			String[] placeholders = new String[parameters.size()];
			Arrays.fill(placeholders, "?");
			String commalist = String.join(",", placeholders);
			String sqlState = " SELECT * FROM " + tableIndexName + " where \"mirisDocId\"  in (" + commalist + ") ";
			// Get items in the table and write out the ID value.
			// https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/example_dynamodb_Scenario_PartiQLSingle_section.html
			ExecuteStatementResponse response = executeStatementRequest(ddb, sqlState, parameters);
			if (response != null) {
				log.info("ExecuteStatement successful: " + response);
				List<Map<String, AttributeValue>> results = response.items();

				for (Map<String, AttributeValue> result : results) {
					Artefact artefact = new AttributeValueToArtefactMapper().apply(result);
					log.info("DB  response to artefact map artefact {}", artefact);
					assert artefact != null;
					// Filter for indexed only
					if (Objects.equals(artefact.getStatus(), ArtefactStatus.INDEXED.getStatus())
							&& !ArtefactClassType.PART.name().equalsIgnoreCase(artefact.getArtefactClassType())) {
						artefacts.add(artefact);
					}
					else {
						log.info("artefact is not added in the artefact list coz status is not INDEXED status: {}",
								artefact.getStatus());
					}
				}
			}
		}
		catch (DynamoDbException e) {
			log.error("Message - error message start");
			log.error(e.getMessage());
		}
		return artefacts;
	}

	public static List<BatchOutput> getAllUnindexedBatches(DynamoDbClient ddb, String tableName, String batchStatus) {
		List<BatchOutput> batches = new ArrayList<>();
		String tableIndexName = "\"" + tableName + "\"" + "." + "\"GSI-Artefact-4\" ";
		try {
			List<AttributeValue> parameters = new ArrayList<>();
			AttributeValue att1 = AttributeValue.builder().s(batchStatus).build();
			parameters.add(att1);
			String[] placeholders = new String[parameters.size()];
			Arrays.fill(placeholders, "?");
			String commalist = String.join(",", placeholders);
			String sqlState = " SELECT * FROM " + tableIndexName + " where \"batchStatus\"  in (" + commalist + ") ";
			// https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/example_dynamodb_Scenario_PartiQLSingle_section.html
			ExecuteStatementResponse response = executeStatementRequest(ddb, sqlState, parameters);
			if (response != null) {
				log.info("ExecuteStatement successful: " + response);
				List<Map<String, AttributeValue>> results = response.items();

				for (Map<String, AttributeValue> result : results) {
					BatchOutput batchOutput = new AttributeValueToBatchOutputMapper().apply(result);
					assert batchOutput != null;
					batches.add(batchOutput);
				}
			}
		}
		catch (DynamoDbException e) {
			log.error("Message - error message start");
			log.error(e.getMessage());
		}
		return batches;
	}

	public static List<ArtefactJob> getAllArtefactJobsByRequestId(DynamoDbClient ddb, String tableName,
			String requestId) {
		List<ArtefactJob> artefactJobs = Collections.emptyList();
		String reqIdIndexName = "\"" + tableName + "\"" + "." + "\"GSI-Artefact-3\" ";
		String sqlStatement = " SELECT * FROM " + reqIdIndexName + " where \"type\" = 'job' and \"requestId\" = ?";
		log.info("Query {}", sqlStatement);
		AttributeValue attrValue = AttributeValue.builder().s(requestId).build();
		try {
			ExecuteStatementResponse response = executeStatementRequest(ddb, sqlStatement, List.of(attrValue));
			if (response != null) {
				log.info("ExecuteStatement successful getAllArtefactJobsByRequestId: {}", response);
				List<Map<String, AttributeValue>> results = response.items();
				AttributeValueToArtefactJobMapper artefactJobMapper = new AttributeValueToArtefactJobMapper();
				artefactJobs = results.stream()
					.map(artefactJobMapper)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			}
		}
		catch (DynamoDbException e) {
			log.error("DynamoDbException - when fetching the job status by requestId", e);
		}
		return artefactJobs;
	}

	// public static PagedArtefactJobs getAllArtefactJobsByRequestIdParti(DynamoDbClient
	// ddb, String tableName,
	// String requestId, String pageSize, String lastEvaluatedKey) {
	// List<ArtefactJob> artefactJobs = new ArrayList<>();
	// String reqIdIndexName = "\"" + tableName + "\"" + "." + "\"GSI-Artefact-3\" ";
	// String sqlStatement = "SELECT * FROM " + reqIdIndexName + " WHERE \"type\" = 'job'
	// AND \"requestId\" = ?";
	//
	// log.info("Query {}", sqlStatement);
	//
	// List<AttributeValue> parameters = new ArrayList<>();
	// parameters.add(AttributeValue.builder().s(requestId).build());
	//
	// String newLastEvaluatedKey = null;
	// boolean hasMorePages = false;
	//
	// try {
	// ExecuteStatementResponse response = executeStatementRequestWithLimit(ddb,
	// sqlStatement, parameters,
	// pageSize, lastEvaluatedKey);
	// if (response != null) {
	// log.info("ExecuteStatement successful getAllArtefactJobsByRequestId: {}",
	// response);
	// List<Map<String, AttributeValue>> results = response.items();
	//
	// AttributeValueToArtefactJobMapper artefactJobMapper = new
	// AttributeValueToArtefactJobMapper();
	// artefactJobs = results.stream()
	// .map(artefactJobMapper)
	// .filter(Objects::nonNull)
	// .collect(Collectors.toList());
	//
	// log.info("no next page availble 1");
	// // If there are more results, the last item's id is the new
	// // lastEvaluatedKey
	// if (response.nextToken() != null && !results.isEmpty()) {
	// log.info("no next page availble 2");
	// // newLastEvaluatedKey = results.get(results.size() -
	// // 1).get("jobId").s();
	// newLastEvaluatedKey = response.nextToken();
	// hasMorePages = true;
	// log.info("Next page available. Last evaluated key: {}", newLastEvaluatedKey);
	// }
	// log.info("no next page availbe");
	// }
	// }
	// catch (DynamoDbException e) {
	// log.error("DynamoDbException - when fetching the job status by requestId", e);
	// }
	//
	// return new PagedArtefactJobs(artefactJobs, newLastEvaluatedKey, hasMorePages);
	// }

	public static void getAllArtefactJobsByRequestId(DynamoDbClient ddb, String tableName, String requestId,
			int pageSize, String nextToken, PagedArtefactJobs pagedArtefactJobs) {
		Set<ArtefactJob> artefactJobs;
		String reqIdIndexName = "\"" + tableName + "\"" + "." + "\"GSI-Artefact-3\" ";

		String sqlStatement = "SELECT * FROM " + reqIdIndexName + " WHERE \"type\" = 'job' AND \"requestId\" = ?";

		log.info("Executing PartiQL statement: {}", sqlStatement);

		AttributeValue requestIdAttrValue = AttributeValue.builder().s(requestId).build();

		ExecuteStatementRequest.Builder statementRequestBuilder = ExecuteStatementRequest.builder()
			.statement(sqlStatement)
			.parameters(List.of(requestIdAttrValue))
			.limit(pageSize);

		if (nextToken != null && !nextToken.isEmpty()) {
			log.info("Next token {} provided, continuing pagination...", nextToken);
			statementRequestBuilder.nextToken(nextToken);
		}
		else {
			log.info("No next token provided, fetching first page...");
		}

		ExecuteStatementRequest statementRequest = statementRequestBuilder.build();

		try {
			ExecuteStatementResponse response = ddb.executeStatement(statementRequest);
			log.info("Query executed successfully, response received: {}", response);
			List<Map<String, AttributeValue>> items = response.items();
			log.info("Number of items retrieved: {}", items.size());
			AttributeValueToArtefactJobMapper artefactJobMapper = new AttributeValueToArtefactJobMapper();
			artefactJobs = items.stream().map(artefactJobMapper).filter(Objects::nonNull).collect(Collectors.toSet());
			log.info("Mapped {} items to ArtefactJob objects.", artefactJobs.size());

			String newNextToken = response.nextToken();
			log.info("newNextToken {}", newNextToken);
			boolean hasMorePages = newNextToken != null;
			pagedArtefactJobs.appendItems(artefactJobs);
			pagedArtefactJobs.setNextToken(newNextToken);
			pagedArtefactJobs.setHasMorePages(hasMorePages);
		}
		catch (DynamoDbException e) {
			log.error("DynamoDbException - when fetching the job status by requestId reason: {}", e.getMessage());
		}
		finally {
			log.info("Finished executing getAllArtefactJobsByRequestId for requestId: {}", requestId);
		}
	}

	public static List<BatchOutput> getAllBatchesForRequestId(DynamoDbClient ddb, String tableName, String requestId) {
		List<BatchOutput> batch = Collections.emptyList();
		String reqIdIndexName = "\"" + tableName + "\"" + "." + "\"GSI-Artefact-3\" ";
		String sqlStatement = " SELECT * FROM " + reqIdIndexName + " where \"type\" = 'batch' and \"requestId\" = ?";
		log.info("Query {}", sqlStatement);
		AttributeValue attrValue = AttributeValue.builder().s(requestId).build();

		try {
			ExecuteStatementResponse response = executeStatementRequest(ddb, sqlStatement, List.of(attrValue));
			if (response != null) {
				log.info("ExecuteStatement successful getAllBatchesForRequestId: {}", response);
				List<Map<String, AttributeValue>> results = response.items();

				AttributeValueToBatchOutputMapper batchMapper = new AttributeValueToBatchOutputMapper();
				batch = results.stream().map(batchMapper).filter(Objects::nonNull).collect(Collectors.toList());

			}
		}
		catch (DynamoDbException e) {
			log.error("DynamoDbException - when fetching the job status by requestId", e);
		}

		return batch;
	}

	public static void scanTable(DynamoDbClient ddb, String tableName) {
		try {
			ScanRequest scanRequest = ScanRequest.builder().tableName(tableName).build();

			ScanResponse response = ddb.scan(scanRequest);
			List<Map<String, AttributeValue>> items = response.items();

			// Print the retrieved items
			for (Map<String, AttributeValue> item : items) {
				System.out.println(item);
			}

			System.out.printf("Scan succeeded. Count: %d\n", response.count());
		}
		catch (DynamoDbException e) {
			System.err.println("Scan failed: " + e.getMessage());
		}
	}

	public static List<ArtefactJob> getAllJobs(DynamoDbClient ddb, String tableName, Map<String, String> conditions) {
		List<ArtefactJob> artefactJobs = Collections.emptyList();
		StringBuilder query = new StringBuilder("SELECT * FROM ");
		query.append("\"").append(tableName).append("\"").append(" WHERE \"type\"='job' ");

		if (StringUtils.hasText(conditions.get("status"))) {
			query.append(" AND jobStatus='").append(conditions.get("status")).append("'");
		}

		if (StringUtils.hasText(conditions.get("date"))) {
			query.append(" AND begins_with(insertedDate , '").append(conditions.get("date")).append("')");
		}
		log.info("GetAllJobQuery : {}", query);
		try {
			ExecuteStatementResponse response = executeStatementRequest(ddb, query.toString(), null);
			if (response != null) {
				List<Map<String, AttributeValue>> results = response.items();
				artefactJobs = results.stream()
					.map(new AttributeValueToArtefactJobMapper())
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			}
		}
		catch (DynamoDbException e) {
			log.error("Message - error message start");
			log.error(e.getMessage());
		}

		return artefactJobs;
	}

}
