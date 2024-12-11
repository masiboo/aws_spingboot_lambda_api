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
import artefact.util.ConstantUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static artefact.apigateway.ApiResponseStatus.SC_OK;

public class ArtefactJobStatusCheck extends CoreRequestHandler
        implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayPostArtefactRequestHandler.class);

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent input, Context context) {

        try {

            logger.info("dump event : " + input);
            String id;
            try {
                id = input.getPathParameters().get("jobid");
            } catch (Exception e) {
                logger.error("error getting  STRING PARAMETER MAP", e);
                return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(400,
                                                    "Missing 'id' parameter in path "+e.getMessage(), true);
            }

            ArtefactJobServiceInterface jobService = getAwsServices().jobService();
            ArtefactJob job = jobService.getJobStatus(id);
            if(job == null){
                logger.error("error job is not found by jobId: {}", id);
                return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(404,
                        "error job is not found by jobId: "+id, true);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("jobStatus", job.getStatus());
            map.put("id", job.getId());
            map.put("artefactId", job.getArtefactId());

            ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_OK, new ApiMapResponse(map));
            APIGatewayV2HTTPResponse gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
            return gatewayV2HTTPResponse;

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(ConstantUtil.STATUS_CODE_SERVER_ERROR,
                                                                            "Error: "+e.getMessage(), true);
        }
    }
}
