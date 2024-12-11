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
import artefact.apigateway.ApiRequestEventUtil;
import artefact.apigateway.CoreRequestHandler;
import artefact.entity.ArtefactTag;
import artefact.usecase.ArtefactServiceInterface;
import artefact.util.ApiGatewayResponseUtil;
import artefact.util.ConstantUtil;

import java.util.List;

public class ApiGatewayGetDocumentByTagsRequestHandler extends CoreRequestHandler
		implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse>, ApiRequestEventUtil {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private static final Logger logger = LoggerFactory.getLogger(ApiGatewayGetDocumentByTagsRequestHandler.class);
	Gson gsonGlobal = new GsonBuilder().setPrettyPrinting().create();
	public ApiGatewayGetDocumentByTagsRequestHandler() {
	}

	@Override
	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
		logger.info("Event json Dump: " + gsonGlobal.toJson(event));
		logger.info("Event Dump: " + event);
    	String artefactId = null;
		try {
			artefactId = event.getQueryStringParameters().get("artefactId");
		} catch (Exception e) {
    		logger.error("error getting QUERY STRING PARAMETER MAP", e);
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(400,
													"Missing 'id' parameter in path "+e.getMessage(), true);
    	}
		
		try {
			/*
			if (event.getBody() == null || event.getBody().isEmpty()) {

				Map<String, Object> map = new HashMap<>();
				map.put("message", "Empty request body");
				ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_BAD_REQUEST, new ApiMapResponse(map));
				APIGatewayV2HTTPResponse gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
				return gatewayV2HTTPResponse;
			}
			
			Date date = new Date();
			String username = getCallingCognitoUsername(event);
			
			Map<String, String> query = event.getQueryStringParameters();
						
			// TODO: validate JSON body input with JSON schema
			// TODO: business validation
			
			Artefact artefact;
			try{
				artefact = objectMapper.readValue(event.getBody(), Artefact.class);
			}catch (IOException e) {
				logger.error(e.getMessage());
				return APIGatewayV2HTTPResponse.builder()
						.withBody("{\"message\": \"Failed to parse product from request body\"}")
						.withStatusCode(ConstantUtil.STATUS_CODE_BAD_REQUEST).build();
			}
			*/
			/*
			List<ArtefactTag> listItemTags = new ArrayList<ArtefactTag>();
			 
			listItemTags = artefact.getArtefactItemTags();
			
			Artefact resultArtefact = service.getArtefactByTags(listItemTags);
			*/
			ArtefactServiceInterface service = getAwsServices().documentService();

			List<List<ArtefactTag>> listArtefactItemTags = service.getAllArtefactItemTags();
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(ConstantUtil.STATUS_CODE_SUCCESS,
																	new Gson().toJson(listArtefactItemTags), false);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(ConstantUtil.STATUS_CODE_SERVER_ERROR,
																			"Error: "+e.getMessage(), true);
		}
	}
	private String getCallingCognitoUsername(APIGatewayV2HTTPEvent event) {
		return "Anonymous";
	}
}
