package artefact.aws.dynamodb;

import artefact.dto.output.BatchOutput;
import artefact.mapper.AttributeValueToArtefactJobMapper;
import artefact.mapper.AttributeValueToArtefactMapper;
import artefact.mapper.AttributeValueToBatchMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import artefact.entity.Artefact;
import artefact.entity.ArtefactJob;
import artefact.util.ArtefactStatus;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ExecuteStatementRequest;
import software.amazon.awssdk.services.dynamodb.model.ExecuteStatementResponse;
import software.amazon.awssdk.utils.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DynamoDbPartiQ {// PartiQL example

    private static final Logger logger = LoggerFactory.getLogger(DynamoDbPartiQ.class);
    static ExecuteStatementResponse executeStatementRequest(DynamoDbClient ddb, String statement, List<AttributeValue> parameters) {
        ExecuteStatementRequest request = ExecuteStatementRequest.builder()
                .statement(statement)
                .parameters(parameters)
                .build();

        return ddb.executeStatement(request);
    }

    static List<Artefact> getAllArtefacts(DynamoDbClient ddb, String tableName,Map<String, String> conditions) {
        List<Artefact> artefacts = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append("\"")
                .append(tableName)
                .append("\"")
                .append(".")
                .append("\"GSI-Artefact-1\"")
                .append(" WHERE 1=1 ");
        if (StringUtils.isNotBlank(conditions.get("status"))) {
            query.append(" AND status='").append(conditions.get("status")).append("'");
        }
        if (StringUtils.isNotBlank(conditions.get("date"))) {
            query.append(" AND begins_with(insertedDate , '").append(conditions.get("date")).append("')");
        }
      try {
          // Get items in the table and write out the ID value.
          // https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/example_dynamodb_Scenario_PartiQLSingle_section.html
          ExecuteStatementResponse response = executeStatementRequest(ddb, query.toString(), null);

          if (response != null) {
              logger.error("ExecuteStatement successful: "+ response.toString());
              List<Map<String, AttributeValue>> results = response.items();

              for (Map<String, AttributeValue> result : results) {
                  Artefact artefact = new AttributeValueToArtefactMapper().apply(result);
                  assert artefact != null;
                  artefacts.add(artefact);
              }
          }
      } catch (DynamoDbException e) {
          logger.error("Message - error message start");
          logger.error(e.getMessage());
      }

      return artefacts;
  }

    static List<Artefact> getArtefactsByMirisDocidAndType(DynamoDbClient ddb, String tableName, String docId,List<String> types) {
        List<Artefact> artefacts = null;
        String tableIndexName =  "\"" + tableName + "\"" + "." + "\"GSI-Artefact-2\" " ;
        try {

            List<AttributeValue> parameters = new ArrayList<>();
            parameters.add(AttributeValue.builder().s(docId).build());
            for(String artefactClassType : types){
                parameters.add(AttributeValue.builder().s(artefactClassType).build());
            }

            String[] placeholders = new String[parameters.size() - 1]; // exclude docId PlaceHolder
            Arrays.fill(placeholders, "?");
            String phWithSeperator = Arrays.stream(placeholders).collect(Collectors.joining(","));


            String sqlState = " SELECT * FROM " + tableIndexName + " where \"mirisDocId\"  = ? and \"type\" in("+phWithSeperator+")";
            logger.info("sqlState :" + sqlState);

            // Get items in the table and write out the ID value.
            // https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/example_dynamodb_Scenario_PartiQLSingle_section.html
            ExecuteStatementResponse response = executeStatementRequest(ddb, sqlState, parameters);

            if (response != null) {
                logger.info("ExecuteStatement successful: "+ response.toString());
                List<Map<String, AttributeValue>> results = response.items();
                artefacts = results.stream().map(new AttributeValueToArtefactMapper())
                                            .filter(Objects::nonNull)
                                            .filter(artefact -> ArtefactStatus.INDEXED.getStatus().equalsIgnoreCase(artefact.getStatus())
                                                     && !DocumentServiceImpl.classType.PART.name().equalsIgnoreCase(artefact.getArtefactClassType()))
                                            .collect(Collectors.toList());
            }

        } catch (DynamoDbException e) {
            logger.error("Exception while getArtefactsByMirisDocidAndType ",e);
        }

        return artefacts;
    }

    static List<Artefact> getArtefactsByMirisDocid(DynamoDbClient ddb, String tableName, String docId, String type) {
        List<Artefact> artefacts = new ArrayList<>();
        String tableIndexName =  "\"" + tableName + "\"" + "." + "\"GSI-Artefact-2\" " ;
        try {

            List<AttributeValue> parameters = new ArrayList<>();
            AttributeValue att1 = AttributeValue.builder()
                    .s(docId)
                    .build();
            parameters.add(att1);

          ExecuteStatementRequest request = ExecuteStatementRequest.builder().build();
          String[] placeholders = new String[parameters.size()];
          Arrays.fill(placeholders, "?");
          String commalist = Arrays.stream(placeholders).collect(Collectors.joining(","));

            String sqlState = " SELECT * FROM " + tableIndexName + " where \"mirisDocId\"  in ("+commalist+") ";

            // Get items in the table and write out the ID value.
            // https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/example_dynamodb_Scenario_PartiQLSingle_section.html
            ExecuteStatementResponse response = executeStatementRequest(ddb, sqlState, parameters);

            if (response != null) {
                logger.info("ExecuteStatement successful: "+ response.toString());
                List<Map<String, AttributeValue>> results = response.items();

                for (Map<String, AttributeValue> result : results) {
                    Artefact artefact = new AttributeValueToArtefactMapper().apply(result);
                    assert artefact != null;
                    // Filter for indexed only
                    if (Objects.equals(artefact.getStatus(), ArtefactStatus.INDEXED.getStatus())
                        && !DocumentServiceImpl.classType.PART.name().equalsIgnoreCase(artefact.getArtefactClassType())) {
                        artefacts.add(artefact);
                    }
                }
            }
        } catch (DynamoDbException e) {
            logger.error("Message - error message start");
            logger.error(e.getMessage());
        }

        return artefacts;
    }

    static List<BatchOutput> getAllUnindexedBatches(DynamoDbClient ddb, String tableName, String batchStatus, String type) {
        List<BatchOutput> batches = new ArrayList<>();

        String tableIndexName =  "\"" + tableName + "\"" + "." + "\"GSI-Artefact-4\" " ;
        try {

            List<AttributeValue> parameters = new ArrayList<>();
            AttributeValue att1 = AttributeValue.builder()
                    .s(batchStatus)
                    .build();
            parameters.add(att1);

            ExecuteStatementRequest request = ExecuteStatementRequest.builder().build();
            String[] placeholders = new String[parameters.size()];
            Arrays.fill(placeholders, "?");
            String commalist = Arrays.stream(placeholders).collect(Collectors.joining(","));

            String sqlState = " SELECT * FROM " + tableIndexName + " where \"batchStatus\"  in ("+commalist+") ";

            // https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/example_dynamodb_Scenario_PartiQLSingle_section.html
            ExecuteStatementResponse response = executeStatementRequest(ddb, sqlState, parameters);

            if (response != null) {
                logger.info("ExecuteStatement successful: "+ response.toString());
                List<Map<String, AttributeValue>> results = response.items();

                for (Map<String, AttributeValue> result : results) {
                    BatchOutput batchOutput = new AttributeValueToBatchMapper().apply(result);
                    assert batchOutput != null;
                    batches.add(batchOutput);
                }
            }
        } catch (DynamoDbException e) {
            logger.error("Message - error message start");
            logger.error(e.getMessage());
        }

        return batches;
    }

    static List<ArtefactJob> getAllArtefactJobsByRequestId(DynamoDbClient ddb, String tableName,String requestId) {
        List<ArtefactJob> artefactJobs = Collections.emptyList();

        String reqIdIndexName =  "\"" + tableName + "\"" + "." + "\"GSI-Artefact-3\" " ;
        String sqlStatement = " SELECT * FROM " + reqIdIndexName + " where \"type\" = 'job' and \"requestId\" = ?";
        logger.info("Query {}",sqlStatement);

        AttributeValue attrValue = AttributeValue.builder()
                .s(requestId)
                .build();
        try{
            ExecuteStatementResponse response = executeStatementRequest(ddb, sqlStatement, List.of(attrValue));
            if (response != null) {
                List<Map<String, AttributeValue>> results = response.items();
                AttributeValueToArtefactJobMapper artefactJobMapper = new AttributeValueToArtefactJobMapper();
                artefactJobs = results.stream()
                        .map(artefactJobMapper::apply)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
        } catch (DynamoDbException e) {
            logger.error("DynamoDbException - when fetching the job status by requestId",e);
        }

        return artefactJobs;
    }

    public static List<ArtefactJob> getAllJobs(DynamoDbClient ddb, String tableName, Map<String, String> conditions) {
        List<ArtefactJob> artefactJobs = Collections.emptyList();
        StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append("\"").append(tableName).append("\"").append(" WHERE \"type\"='job' ");

        if (StringUtils.isNotBlank(conditions.get("status"))) {
            query.append(" AND jobStatus='").append(conditions.get("status")).append("'");
        }

        if (StringUtils.isNotBlank(conditions.get("date"))) {
            query.append(" AND begins_with(insertedDate , '").append(conditions.get("date")).append("')");
        }
        logger.info("GetAllJobQuery : {}", query);
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
            logger.error("Message - error message start");
            logger.error(e.getMessage());
        }

        return artefactJobs;
    }
}
