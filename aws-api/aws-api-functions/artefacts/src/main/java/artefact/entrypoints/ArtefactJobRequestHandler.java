package artefact.entrypoints;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import artefact.apigateway.ApiMapResponse;
import artefact.apigateway.ApiRequestHandlerResponse;
import artefact.apigateway.CoreRequestHandler;
import artefact.entity.ArtefactJob;
import artefact.usecase.ArtefactJobServiceInterface;
import artefact.util.ApiGatewayResponseUtil;
import artefact.util.ArtefactStatus;
import artefact.util.ConstantUtil;
import artefact.util.DateUtils;
import software.amazon.awssdk.utils.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static artefact.apigateway.ApiResponseStatus.SC_NOT_FOUND;
import static artefact.apigateway.ApiResponseStatus.SC_OK;

public class ArtefactJobRequestHandler extends CoreRequestHandler
        implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final Logger logger = LoggerFactory.getLogger(ArtefactJobRequestHandler.class);
    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        try {
            String date = null;
            String status = null;
            if (event.getQueryStringParameters() != null && !event.getQueryStringParameters().isEmpty()) {
                date = event.getQueryStringParameters().get("date");
                status = event.getQueryStringParameters().get("status");
                Map<String, Object> validationErrors = validateQueryParams(date, status);
                if (validationErrors != null && !validationErrors.isEmpty()) {
                    ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_OK, new ApiMapResponse(validationErrors));
                    return buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
                }
            }

            ArtefactJobServiceInterface service = getAwsServices().jobService();
            List<ArtefactJob> artefacts = service.getAllJobs(date, status);

            if (artefacts == null) {
                Map<String, Object> map = new HashMap<>();
                ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_NOT_FOUND, new ApiMapResponse(map));
                APIGatewayV2HTTPResponse gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
                return gatewayV2HTTPResponse;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("jobs", artefacts);

            ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_OK, new ApiMapResponse(map));
            APIGatewayV2HTTPResponse gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
            return gatewayV2HTTPResponse;
        } catch (Exception e) {
            logger.error("Exception while uploading single artefact ", e);
            return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(ConstantUtil.STATUS_CODE_SERVER_ERROR,
                                        "Exception while uploading single artefact "+e.getMessage(), true );
        }
    }

    private Map<String, Object> validateQueryParams(String date, String status) {
        Map<String, Object> validationErrors = new HashMap<>();
        if (StringUtils.isNotBlank(date) && !DateUtils.isValidDate(date, DateUtils.getSimpleDateFormat())) {
            validationErrors.put("date", "Invalid dateFormat and valid format is yyyy-MM-dd");
        }
        if (StringUtils.isNotBlank(status)
                && !Arrays.stream(ArtefactStatus.values()).anyMatch(val -> val.toString().equalsIgnoreCase(status))) {
            validationErrors.put("status",
                    "Invalid status given and allowed types are " + Arrays.toString(ArtefactStatus.values()));
        }
        return validationErrors;
    }
}
