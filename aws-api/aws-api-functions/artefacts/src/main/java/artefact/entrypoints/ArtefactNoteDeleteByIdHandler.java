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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static artefact.apigateway.ApiResponseStatus.SC_OK;

public class ArtefactNoteDeleteByIdHandler extends CoreRequestHandler
        implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final Logger logger = LoggerFactory.getLogger(ArtefactNoteDeleteByIdHandler.class);

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent input, Context context) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("status", "ok");
            ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_OK, new ApiMapResponse(map));
            APIGatewayV2HTTPResponse gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
            return gatewayV2HTTPResponse;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
