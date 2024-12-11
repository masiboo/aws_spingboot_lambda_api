package artefact.entrypoints;

import artefact.apigateway.ApiMapResponse;
import artefact.apigateway.ApiRequestHandlerResponse;
import artefact.entity.Artefact;
import artefact.util.ApiGatewayResponseUtil;
import artefact.util.ConstantUtil;
import artefact.util.DebugInfoUtil;
import artefact.util.S3Util;
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
import artefact.apigateway.CoreRequestHandler;
import artefact.usecase.ArtefactCachedService;
import artefact.usecase.ArtefactServiceInterface;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static artefact.apigateway.ApiResponseStatus.*;


public class ArtefactDownloadUrlByArtefactId extends CoreRequestHandler
		implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final Logger logger = LoggerFactory.getLogger(ArtefactDownloadUrlByArtefactId.class);
    Gson gsonGlobal = new GsonBuilder().setPrettyPrinting().create();
    private static final int DEFAULT_DURATION_HOURS = 1;
    private final ArtefactStore artefactStore;

    public ArtefactDownloadUrlByArtefactId() {
        this(new DynamoDbArtefactStore());
    }

    public ArtefactDownloadUrlByArtefactId(ArtefactStore artefactStore) {
        this.artefactStore = artefactStore;
    }


    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        logger.info("Event json Dump: " + gsonGlobal.toJson(event));
        logger.info("Event Dump: " + event);
        Artefact artefact = null;
        try {
            String id = event.getPathParameters().get("artefactId");
            if (id == null) {
                logger.warn("Missing 'id' parameter in path");
				Map<String, Object> map = new HashMap<>();
				map.put("message", "Missing 'id' parameter in path");
				ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_BAD_REQUEST, new ApiMapResponse(map));
				APIGatewayV2HTTPResponse gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
				return gatewayV2HTTPResponse;
            }


            try {
                ArtefactServiceInterface service = getAwsServices().documentService();
                ArtefactCachedService cachedService = new ArtefactCachedService();
//                artefact = service.getArtefactById(id);
                artefact = cachedService.getArtefactById(id, getAwsServices());

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
				Map<String, Object> map = new HashMap<>();
				map.put("message", "Failed to get artefact");
				ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_ERROR, new ApiMapResponse(map));
				APIGatewayV2HTTPResponse gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
				return gatewayV2HTTPResponse;
            }

            if (artefact == null) {
                logger.error("Artefact is null");

				Map<String, Object> map = new HashMap<>();
				map.put("message", "Artefact Not found");
				ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_NOT_FOUND, new ApiMapResponse(map));
				APIGatewayV2HTTPResponse gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
				return gatewayV2HTTPResponse;
            }

			int hours = S3Util.getDurationHours(event);
			Duration duration = Duration.ofHours(hours);
            String urlstring = S3Util.generatePresignedUrl(getAwsServices(), artefact, duration, null);
            if(!S3Util.verifyS3UrlForObjectExist(urlstring)){
                logger.warn("File doesn't exit in S3");
                return  ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(SC_NOT_FOUND.getStatusCode(),
                        "File doesn't exit in S3", true);
            }
            Map<String, Object> outputmap = new HashMap<>();
            outputmap.put("artefactId", id);
            outputmap.put("signedS3Url", urlstring);

            ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_CREATED, new ApiMapResponse(outputmap));
            APIGatewayV2HTTPResponse gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
            return gatewayV2HTTPResponse;

        } catch (Exception e) {
           logger.error(e.getMessage());
            DebugInfoUtil.logExceptionDetails(e);
           return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(ConstantUtil.STATUS_CODE_SERVER_ERROR,
                            "Error for artefact mirisDocId:  "+artefact != null ? artefact.getMirisDocId() : null +" " +
                                    e.getMessage(), true);
        }
    }
}
