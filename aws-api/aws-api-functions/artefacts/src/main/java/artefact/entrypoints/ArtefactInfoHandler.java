package artefact.entrypoints;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import artefact.apigateway.ApiMapResponse;
import artefact.apigateway.CoreRequestHandler;
import artefact.dto.output.ArtefactInfo;
import artefact.usecase.ArtefactServiceInterface;
import artefact.util.ApiGatewayResponseUtil;
import artefact.util.ConstantUtil;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static artefact.apigateway.ApiResponseStatus.*;
import static artefact.util.AppConstants.KEY_MESSAGE;

public class ArtefactInfoHandler extends CoreRequestHandler
        implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final Logger logger = LoggerFactory.getLogger(ArtefactInfoHandler.class);

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        Map<String,String> pathParamMap = Optional.ofNullable(event.getPathParameters()).orElse(Map.of());
        String artefactId = pathParamMap.get("artefactId");
        try{
            if (artefactId == null) {
                logger.warn("Missing 'artefactId' parameter in path");
                return buildResponse(logger, SC_BAD_REQUEST, new ApiMapResponse(Map.of(KEY_MESSAGE, "Missing 'artefactId' parameter in path")));
            }
            ArtefactServiceInterface documentService = getAwsServices().documentService();
            ArtefactInfo artefactInfo = documentService.getArtefactInfoById(artefactId);
            if(artefactInfo == null){
                String errorMessage = "The artefact is not found with id " + artefactId;
                logger.error(errorMessage);
                return buildResponse(logger,SC_NOT_FOUND,new ApiMapResponse(Map.of(KEY_MESSAGE, errorMessage)));
            }
            return buildResponse(logger,SC_OK,new ApiMapResponse(Map.of("artefactInfo",artefactInfo)));
        } catch (IOException e) {
            String errorMessage = "Error updated the artefact status ";
            logger.error(errorMessage,e);
            return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse( ConstantUtil.STATUS_CODE_SERVER_ERROR,
                                                                          errorMessage+e.getMessage(), true);
        }
    }
}
