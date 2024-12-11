package artefact.entrypoints;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import artefact.apigateway.ApiMapResponse;
import artefact.apigateway.ApiRequestHandlerResponse;
import artefact.apigateway.CoreRequestHandler;
import artefact.aws.dynamodb.DocumentServiceImpl;
import artefact.dto.output.ArtefactOutput;
import artefact.dto.output.BatchOutput;
import artefact.entity.Artefact;
import artefact.mapper.ArtefactToArtefactOutputMapper;
import artefact.usecase.ArtefactServiceInterface;
import artefact.usecase.BatchServiceInterface;
import artefact.util.ArtefactStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static artefact.apigateway.ApiResponseStatus.SC_BAD_REQUEST;
import static artefact.apigateway.ApiResponseStatus.SC_OK;

public class BatchDetailRequestHandler extends CoreRequestHandler
        implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayPostArtefactRequestHandler.class);

    Gson gsonGlobal = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {

        logger.info("Event json Dump: " + gsonGlobal.toJson(event));
        logger.info("Event Dump: " + event);

        try {

            String id = event.getPathParameters().get("batchIdPathParam");
            if (id == null) {
                logger.warn("Missing 'batchIdPathParam' parameter in path");
                Map<String, Object> map = new HashMap<>();
                map.put("message", "Missing 'batchIdPathParam' parameter in path");
                ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_BAD_REQUEST, new ApiMapResponse(map));
                APIGatewayV2HTTPResponse gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
                return gatewayV2HTTPResponse;
            }

            BatchServiceInterface service = getAwsServices().batchService();
            ArtefactServiceInterface docService = getAwsServices().documentService();

            // get batch detail
            BatchOutput bo = service.getBatchDetail(id);

            // get a list of artefactIds
            List<ArtefactOutput> allArtefacts = service.getAllArtefactsForBatch(id, "artefact");

            // filter the Artefact which are eligible for indexation
            Predicate<Artefact> eligibleForIndexation = artefact -> ArtefactStatus.INSERTED.getStatus().equalsIgnoreCase(artefact.getStatus())
                    && !DocumentServiceImpl.classType.PART.name().equalsIgnoreCase(artefact.getArtefactClassType());
            List<ArtefactOutput> indexEligibleArtefacts = allArtefacts.stream().map(x-> docService.getArtefactById(x.getId()))
                    .filter(eligibleForIndexation)
                    .map(new ArtefactToArtefactOutputMapper()).collect(Collectors.toList());

            logger.info("allArtefacts");
            logger.info(allArtefacts.toString());
            logger.info("indexEligibleArtefacts");
            logger.info(indexEligibleArtefacts.toString());

            bo.setArtefacts(indexEligibleArtefacts);

            // get each artefact detail, create a list of artefacts

//            if (batches == null) {
//                Map<String, Object> map = new HashMap<>();
//                ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_NOT_FOUND, new ApiMapResponse(map));
//                APIGatewayV2HTTPResponse gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
//            List<BatchOutput> out =  null;

            Map<String, Object> map = new HashMap<>();
            map.put("data", bo);

            ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_OK, new ApiMapResponse(map));
            APIGatewayV2HTTPResponse gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
            return gatewayV2HTTPResponse;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
