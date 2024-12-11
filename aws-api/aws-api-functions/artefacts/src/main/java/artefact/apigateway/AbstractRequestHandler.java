package artefact.apigateway;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import artefact.aws.AwsServiceCache;
import artefact.aws.dynamodb.DynamoDbConnectionBuilder;
import artefact.aws.s3.S3ConnectionBuilder;
import artefact.entity.DocumentFormatType;
import artefact.util.CsvConverterUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static artefact.apigateway.ApiResponseStatus.SC_FOUND;
import static artefact.util.DateUtils.createObjectMapper;

public abstract class AbstractRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(
            AbstractRequestHandler.class);

    public static AwsServiceCache awsServices;

    protected static void setAwsServiceCache(final Map<String, String> map,
                                             final DynamoDbConnectionBuilder builder, final S3ConnectionBuilder s3) {

        logger.info("setAwsServiceCache");
        logger.info("REGISTRY_TABLE_NAME: " + map.get("REGISTRY_TABLE_NAME"));
        logger.info("CACHE_TABLE: " +map.get("CACHE_TABLE"));
        logger.info("APP_ENVIRONMENT: " +map.get("APP_ENVIRONMENT"));
        logger.info("ARTEFACTS_S3_BUCKET: " +map.get("ARTEFACTS_S3_BUCKET"));

        awsServices = new AwsServiceCache()
                .dbConnection(builder, map.get("REGISTRY_TABLE_NAME"), map.get("CACHE_TABLE"))
                .s3Connection(s3)
                .debug("true".equals(map.get("DEBUG")))
                .appEnvironment(map.get("APP_ENVIRONMENT"))
                .artefactS3bucket(map.get("ARTEFACTS_S3_BUCKET"));

        awsServices.init();
    }

    public AwsServiceCache getAwsServices() {
        return awsServices;
    }

    protected APIGatewayV2HTTPResponse buildResponse(final Logger logger,
                                                     final ApiResponseStatus responseStatus,
                                                     final ApiResponse apiResponse) throws IOException {
        ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(responseStatus, apiResponse);
        return buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
    }

    protected APIGatewayV2HTTPResponse buildResponse(final Logger logger,
                                                     final ApiResponseStatus status, final Map<String, String> headers,
                                                     final ApiResponse apiResponse) throws IOException {

        String gatewayResponse = "";
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", status.getStatusCode());

        ObjectMapper objectMapper = createObjectMapper();

        if (apiResponse instanceof ApiRedirectResponse) { // http redirect
            headers.clear();
            headers.put("Location", ((ApiRedirectResponse) apiResponse).getRedirectUri());
        } else if (status.getStatusCode() == SC_FOUND.getStatusCode()
                && apiResponse instanceof ApiMessageResponse) {  // string response
            headers.clear();
            headers.put("Location", ((ApiMessageResponse) apiResponse).getMessage());
        } else if (apiResponse instanceof ApiMapResponse) { // json response
            gatewayResponse = objectMapper.writeValueAsString(((ApiMapResponse) apiResponse).getMap());
            response.put("body", gatewayResponse);
        } else if (apiResponse instanceof ApiMapBatchResponse) { // json response
            if(((ApiMapBatchResponse) apiResponse).getFormat().equals(DocumentFormatType.CSV)) {
                gatewayResponse = CsvConverterUtil.convertToCSV(((ApiMapBatchResponse) apiResponse).getMapList());
            } else if(((ApiMapBatchResponse) apiResponse).getFormat().equals(DocumentFormatType.JSON)) {
                gatewayResponse = objectMapper.writeValueAsString(((ApiMapBatchResponse) apiResponse).getMapList());
            }
            response.put("body", gatewayResponse);
        } else {
            gatewayResponse = objectMapper.writeValueAsString(apiResponse);
            response.put("body", gatewayResponse);
        }

        Map<String, String> jsonheaders = createJsonHeaders();
        jsonheaders.putAll(headers);
        response.put("headers", jsonheaders);

        APIGatewayV2HTTPResponse gatewayV2HTTPResponse = APIGatewayV2HTTPResponse
                .builder()
                .withStatusCode(status.getStatusCode())
                .withHeaders(jsonheaders)
                .withBody(gatewayResponse)
                .build();

        return gatewayV2HTTPResponse;
    }


    private Map<String, String> createJsonHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key");
        headers.put("Access-Control-Allow-Methods", "*");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Content-Type", "application/json");
        return headers;
    }


    private void logError(final LambdaLogger logger, final Exception e) {
        e.printStackTrace();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        logger.log(sw.toString());
    }

}
