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
import artefact.dto.output.ArtefactOutput;
import artefact.entity.Artefact;
import artefact.mapper.ArtefactToArtefactOutputMapper;
import artefact.usecase.ArtefactServiceInterface;
import artefact.util.ArtefactStatus;
import software.amazon.awssdk.utils.StringUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static artefact.apigateway.ApiResponseStatus.SC_NOT_FOUND;
import static artefact.apigateway.ApiResponseStatus.SC_OK;

public class ArtefactRequestHandler extends CoreRequestHandler
        implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayPostArtefactRequestHandler.class);

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent input, Context context) {

        try {
            String date = null;
            String status = null;
            if (input.getQueryStringParameters() != null && !input.getQueryStringParameters().isEmpty()) {
                date = input.getQueryStringParameters().get("date");
                status = input.getQueryStringParameters().get("status");
                Map<String, Object> validationErrors = validateQueryParams(date, status);
                if (validationErrors != null && !validationErrors.isEmpty()) {
                    ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_OK, new ApiMapResponse(validationErrors));
                    return buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
                }
            }

            ArtefactServiceInterface service = getAwsServices().documentService();
            List<Artefact> artefacts = service.getAllArtefacts(date,status);

            List<ArtefactOutput> out =  artefacts.stream()
                    .map(m -> new ArtefactToArtefactOutputMapper().apply(m)).collect(Collectors.toList());

            logger.info("artefact-output " + out.toString());

            if (artefacts == null) {
                Map<String, Object> map = new HashMap<>();
                ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_NOT_FOUND, new ApiMapResponse(map));
                APIGatewayV2HTTPResponse gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
                return gatewayV2HTTPResponse;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("artefacts", out);

            ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_OK, new ApiMapResponse(map));
            APIGatewayV2HTTPResponse gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
            return gatewayV2HTTPResponse;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> validateQueryParams(String date, String status) {
        Map<String, Object> validationErrors = new HashMap<>();
        if (StringUtils.isNotBlank(date)) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateFormat.setLenient(false);
                dateFormat.parse(date);
            }
            catch (ParseException e) {
                validationErrors.put("date", "Invalid dateFormat and valid format is yyyy-MM-dd");
            }
        }
        if (StringUtils.isNotBlank(status)) {
            try {
                ArtefactStatus.valueOf(status);
            }
            catch (IllegalArgumentException e) {
                validationErrors.put("date",
                        "Invalid status givenand allowed values are " + Arrays.toString(ArtefactStatus.values()));
            }
        }
        return validationErrors;
    }
}
