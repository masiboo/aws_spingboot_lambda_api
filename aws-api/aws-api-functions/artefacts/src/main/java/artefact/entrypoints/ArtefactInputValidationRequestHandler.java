package artefact.entrypoints;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import artefact.apigateway.*;
import artefact.aws.AwsServiceCache;
import artefact.dto.input.ArtefactInput;
import artefact.usecase.ArtefactServiceInterface;
import artefact.util.ApiGatewayResponseUtil;
import artefact.util.ConstantUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static artefact.apigateway.ApiResponseStatus.SC_BAD_REQUEST;
import static artefact.apigateway.ApiResponseStatus.SC_OK;

public class ArtefactInputValidationRequestHandler extends CoreRequestHandler
		implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse>, ApiRequestEventUtil {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private static final Logger logger = LoggerFactory.getLogger(ArtefactInputValidationRequestHandler.class);
	Gson gsonGlobal = new GsonBuilder().setPrettyPrinting().create();
	public ArtefactInputValidationRequestHandler() {
	}

	@Override
	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
		logger.info("Event json Dump: " + gsonGlobal.toJson(event));
		logger.info("Event Dump: " + event);

		try {

			if (event.getBody() == null || event.getBody().isEmpty()) {
				Map<String, Object> map = new HashMap<>();
				map.put("message", "Empty request body");
				ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_BAD_REQUEST, new ApiMapResponse(map));
				APIGatewayV2HTTPResponse gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
				return gatewayV2HTTPResponse;
			}

			String artefactId = UUID.randomUUID().toString(); // transaction id (since artefact id is generated at DB level)

			// TODO: validate JSON body input with JSON schema
			// TODO: business validation
			ArtefactServiceInterface service = getAwsServices().documentService();

			ArtefactInput artefact;
			try{
				artefact = objectMapper.readValue(event.getBody(), ArtefactInput.class);
			}catch (IOException e) {
				logger.error(e.getMessage());
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(ConstantUtil.STATUS_CODE_BAD_REQUEST,
										"Failed to parse product from request body "+e.getMessage(), true);
			}

			Map<String, String> inputValidation = service.validateInputDocument(artefact);
			Map<String, Object> outputmap = new HashMap<>();
			outputmap.put("artefactId", artefactId);
			outputmap.put("validation", inputValidation);
			APIGatewayV2HTTPResponse gatewayV2HTTPResponse;
			if(inputValidation.isEmpty()) {
				ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_OK, new ApiMapResponse(outputmap));
				gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
				
			}else {
				ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_BAD_REQUEST, new ApiMapResponse(outputmap));
				gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
			}
			return gatewayV2HTTPResponse;

		} catch (Exception e) {
			logger.error(e.getMessage());
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(ConstantUtil.STATUS_CODE_SERVER_ERROR,
													"Artefact can't be parsed error: "+e.getMessage(), true );
		}
	}


	private Optional<Long> calculateContentLength(final AwsServiceCache awsservice,
												  final Map<String, String> query) throws BadException {

		Long contentLength = query != null && query.containsKey("contentLength")
				? Long.valueOf(query.get("contentLength"))
				: null;

//		String value = this.restrictionMaxContentLength.getValue(awsservice);
//		if (value != null
//				&& this.restrictionMaxContentLength.enforced(awsservice, value, contentLength)) {
//
//			if (contentLength == null) {
//				throw new BadException("'contentLength' is required");
//			}
//
//			String maxContentLengthBytes =
//					this.restrictionMaxContentLength.getValue(awsservice;
//			throw new BadException("'contentLength' cannot exceed " + maxContentLengthBytes + " bytes");
//		}

		return contentLength != null ? Optional.of(contentLength) : Optional.empty();
	}
}
