package artefact.entrypoints;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import artefact.apigateway.ApiMapResponse;
import artefact.apigateway.CoreRequestHandler;
import artefact.dto.ArtefactIndexDto;
import artefact.entity.Artefact;
import artefact.usecase.ArtefactServiceInterface;
import artefact.util.ApiGatewayResponseUtil;
import artefact.util.ArtefactStatus;
import artefact.util.ConstantUtil;
import software.amazon.awssdk.utils.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static artefact.apigateway.ApiResponseStatus.*;
import static artefact.util.AppConstants.KEY_MESSAGE;

public class IndexArtefactByIdHandler extends CoreRequestHandler
        implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final Logger logger = LoggerFactory.getLogger(IndexArtefactByIdHandler.class);
    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        Map<String,String> pathParamMap = Optional.ofNullable(event.getPathParameters()).orElse(Map.of());
        String artefactId = pathParamMap.get("artefactId");
        try{
            if (artefactId == null) {
                logger.warn("Missing 'artefactId' parameter in path");
                return buildResponse(logger, SC_BAD_REQUEST, new ApiMapResponse(Map.of(KEY_MESSAGE, "Missing 'artefactId' parameter in path")));
            }

            String eventBody = event.getBody();
            if (StringUtils.isBlank(eventBody)) {
                return buildResponse(logger, SC_BAD_REQUEST, new ApiMapResponse(Map.of(KEY_MESSAGE, "Empty request body")));
            }

            ArtefactIndexDto artefactIndexDto;
            try {
                artefactIndexDto = new ObjectMapper().readValue(event.getBody(), ArtefactIndexDto.class);
            } catch (IOException e) {
                logger.error(e.getMessage());
                return buildResponse(logger, SC_BAD_REQUEST, new ApiMapResponse(Map.of(KEY_MESSAGE, "Failed to parse mirisDocId from request body")));
            }

            ArtefactServiceInterface documentService = getAwsServices().documentService();
            Artefact artefact = documentService.getArtefactById(artefactId);
            if(artefact == null){
                String errorMessage = "The artefact is not found with id " + artefactId;
                logger.error(errorMessage);
                return buildResponse(logger,SC_NOT_FOUND,new ApiMapResponse(Map.of(KEY_MESSAGE, errorMessage)));
            }

            // VALIDATION If the artefact is already INDEXED or DELETED
            if(ArtefactStatus.INDEXED.getStatus().equalsIgnoreCase(artefact.getStatus())){
                return buildResponse(logger,SC_BAD_REQUEST,new ApiMapResponse(Map.of(KEY_MESSAGE, "Artefact is already in INDEXED")));
            }

            if(ArtefactStatus.DELETED.getStatus().equalsIgnoreCase(artefact.getStatus())){
                return buildResponse(logger,SC_BAD_REQUEST,new ApiMapResponse(Map.of(KEY_MESSAGE, "Artefact status is DELETED and can not be INDEXED")));
            }

            if(Boolean.parseBoolean(System.getenv().get("MIRIS_CHECK_ENABLED"))
                    && !documentService.isDocIdValid(artefactIndexDto.getMirisDocId())){
                return buildResponse(logger,SC_BAD_REQUEST,new ApiMapResponse(Map.of(KEY_MESSAGE, "The given mirisDocId is invalid")));
            }


            documentService.indexArtefact(artefactId,artefactIndexDto, ArtefactStatus.INDEXED);
            return buildResponse(logger,SC_OK,new ApiMapResponse(Map.of(KEY_MESSAGE,"Successfully indexed the Artefact")));

        } catch (IOException e) {
            String errorMessage = "Error updated the artefact status ";
            logger.error(errorMessage,e);
            return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(ConstantUtil.STATUS_CODE_SERVER_ERROR,
                                                                        errorMessage+e.getMessage(), true);
        }
    }
}
