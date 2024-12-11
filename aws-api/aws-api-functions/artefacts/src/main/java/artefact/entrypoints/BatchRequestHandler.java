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
import artefact.dto.output.BatchOutput;
import artefact.usecase.BatchServiceInterface;
import artefact.util.BatchStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static artefact.apigateway.ApiResponseStatus.SC_NOT_FOUND;
import static artefact.apigateway.ApiResponseStatus.SC_OK;

// Aws-get-all-batch-function-devbuild
// GET /api/batches
public class BatchRequestHandler extends CoreRequestHandler
        implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayPostArtefactRequestHandler.class);

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent input, Context context) {

        try {

            BatchServiceInterface service = getAwsServices().batchService();
            // Only retrieve those unindexed- but inserted batches
            List<BatchOutput> batches = service.getAllBatchByStatus(BatchStatus.INSERTED.getStatus());

            // filter for addendum only

            // FIXME: return appropiate error with 500
            if (batches == null) {
                Map<String, Object> map = new HashMap<>();
                ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_NOT_FOUND, new ApiMapResponse(map));
                APIGatewayV2HTTPResponse gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
                return gatewayV2HTTPResponse;
            }

            List<BatchOutput> out =  batches;

            Map<String, Object> map = new HashMap<>();
            map.put("batches", out);

            ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_OK, new ApiMapResponse(map));
            APIGatewayV2HTTPResponse gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
            return gatewayV2HTTPResponse;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
