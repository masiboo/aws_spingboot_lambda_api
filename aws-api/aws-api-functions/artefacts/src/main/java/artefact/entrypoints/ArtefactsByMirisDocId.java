package artefact.entrypoints;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import artefact.adapter.ArtefactStore;
import artefact.adapter.dynamodb.DynamoDbArtefactStore;
import artefact.apigateway.ApiMapResponse;
import artefact.apigateway.ApiRequestHandlerResponse;
import artefact.apigateway.CoreRequestHandler;
import artefact.entity.Artefact;
import artefact.usecase.ArtefactCachedService;
import artefact.usecase.ArtefactServiceInterface;
import artefact.util.*;
import software.amazon.awssdk.utils.StringUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

import static artefact.apigateway.ApiResponseStatus.*;

public class ArtefactsByMirisDocId extends CoreRequestHandler
		implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final Logger logger = LoggerFactory.getLogger(ArtefactsByMirisDocId.class);
    Gson gsonGlobal = new GsonBuilder().setPrettyPrinting().create();
    private static final int DEFAULT_DURATION_HOURS = 1;
    private final ArtefactStore artefactStore;

    public ArtefactsByMirisDocId() {
        this(new DynamoDbArtefactStore());
    }

    public ArtefactsByMirisDocId(ArtefactStore artefactStore) {
        this.artefactStore = artefactStore;
    }


    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        logger.info("Event json Dump: " + gsonGlobal.toJson(event));
        logger.info("Event Dump: " + event);
        List<Artefact> artefact;
        try {
            String id = event.getPathParameters().get("mirisDocId");
            if (id == null) {
                logger.warn("Missing 'mirisDocId' parameter in path");
				Map<String, Object> map = new HashMap<>();
				map.put("message", "Missing 'mirisDocId' parameter in path");
				ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_BAD_REQUEST, new ApiMapResponse(map));
				APIGatewayV2HTTPResponse gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
				return gatewayV2HTTPResponse;
            }


            ArtefactServiceInterface service = getAwsServices().documentService();
            ArtefactCachedService cachedService = new ArtefactCachedService();

            String docType = event.getQueryStringParameters() != null
                    ? event.getQueryStringParameters().get("docType")
                    : null;
            if(StringUtils.isNotBlank(docType)) {
                List<String> classTypeList = Arrays.asList(docType.split("\\s*,\\s*"));
                for(String classType : classTypeList){
                    if (!service.isValidClassType(classType)) {
                        String errorMsg = "Invalid 'docType' provided :'" + classType + "'  and allowed values are " + service.getAllClassTypes();
                        logger.error(errorMsg);
                        return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(400, errorMsg, true);
                   }
                }

                // 1. ...
                // artefact = service.getArtefactbyMirisDocIdAndType(id,classTypeList);
                artefact = cachedService.getArtefactbyMirisDocIdAndType(id, classTypeList, getAwsServices());
                logger.info("found artefact {} by docType {}",artefact, id);
            } else {
                try {
                    logger.info("@@@ ARTEFACT artefact start");
                    artefact = cachedService.getArtefactbyMirisDocId(id, getAwsServices());
                    logger.info("@@@ ARTEFACT artefact fetch " + artefact.toString());

                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    DebugInfoUtil.logExceptionDetails(e);
                    Map<String, Object> map = new HashMap<>();
                    map.put("message", "Failed to get artefact");
                    ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_ERROR, new ApiMapResponse(map));
                    APIGatewayV2HTTPResponse gatewayV2HTTPResponse = null;
                    try {
                        gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    return gatewayV2HTTPResponse;
                }
            }

            Objects.requireNonNull(artefact, "artefact should not be null").forEach(item -> {
                int hours = S3Util.getDurationHours(event);
                Duration duration = Duration.ofHours(hours);
                String s3Url = S3Util.generatePresignedUrl(getAwsServices(), item, duration, null);
                if (!S3Util.verifyS3UrlForObjectExist(s3Url)) {
                    logger.warn("S3 object does not exist in bucket");
                    item.setError("S3 object does not exist in bucket");
                    item.setStatus(ArtefactStatus.ERROR.getStatus());
                }
            });

            Map<String, Object> outputmap = new HashMap<>();
            outputmap.put("artefacts", artefact);
            ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_OK, new ApiMapResponse(outputmap));
            APIGatewayV2HTTPResponse gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
            return gatewayV2HTTPResponse;
        } catch (Exception e) {
            logger.error(e.getMessage());
            DebugInfoUtil.logExceptionDetails(e);
            return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(ConstantUtil.STATUS_CODE_SERVER_ERROR,
                                                                            "Error: "+e.getMessage(), true);
        }
    }
}
