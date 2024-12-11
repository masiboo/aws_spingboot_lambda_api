package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import org.iprosoft.trademarks.aws.artefacts.util.CsvConverterUtil;
import org.iprosoft.trademarks.aws.artefacts.util.DocumentFormatType;
import org.iprosoft.trademarks.aws.artefacts.util.GsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiResponseStatus.SC_FOUND;

public abstract class AbstractRequestHandler {

	private static final Logger logger = LoggerFactory.getLogger(AbstractRequestHandler.class);

	private Gson gson = GsonUtil.getInstance();

	protected APIGatewayV2HTTPResponse buildResponse(final Logger logger, final ApiResponseStatus responseStatus,
			final ApiResponse apiResponse) throws IOException {
		ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(responseStatus, apiResponse);
		return buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
	}

	protected APIGatewayV2HTTPResponse buildResponse(final Logger logger, final ApiResponseStatus status,
			final Map<String, String> headers, final ApiResponse apiResponse) throws IOException {

		String gatewayResponse = "";
		Map<String, Object> response = new HashMap<>();
		response.put("statusCode", status.getStatusCode());

		if (apiResponse instanceof ApiRedirectResponse) { // http redirect
			headers.clear();
			headers.put("Location", ((ApiRedirectResponse) apiResponse).getRedirectUri());
		}
		else if (status.getStatusCode() == SC_FOUND.getStatusCode() && apiResponse instanceof ApiMessageResponse) { // string
																													// response
			headers.clear();
			headers.put("Location", ((ApiMessageResponse) apiResponse).getMessage());
		}
		else if (apiResponse instanceof ApiMapResponse) { // json response
			gatewayResponse = this.gson.toJson(((ApiMapResponse) apiResponse).getMap());
			response.put("body", gatewayResponse);
		}
		else if (apiResponse instanceof ApiMapBatchResponse) { // json response
			if (((ApiMapBatchResponse) apiResponse).getFormat().equals(DocumentFormatType.CSV)) {
				gatewayResponse = CsvConverterUtil.convertToCSV(((ApiMapBatchResponse) apiResponse).getMapList());
			}
			else if (((ApiMapBatchResponse) apiResponse).getFormat().equals(DocumentFormatType.JSON)) {
				gatewayResponse = this.gson.toJson(((ApiMapBatchResponse) apiResponse).getMapList());
			}
			response.put("body", gatewayResponse);
		}
		else {
			gatewayResponse = this.gson.toJson(apiResponse);
			response.put("body", gatewayResponse);
		}

		Map<String, String> jsonheaders = createJsonHeaders();
		jsonheaders.putAll(headers);
		response.put("headers", jsonheaders);

		APIGatewayV2HTTPResponse gatewayV2HTTPResponse = APIGatewayV2HTTPResponse.builder()
			.withStatusCode(status.getStatusCode())
			.withHeaders(jsonheaders)
			.withBody(gatewayResponse)
			.build();

		return gatewayV2HTTPResponse;
	}

	private Map<String, String> createJsonHeaders() {
		Map<String, String> headers = new HashMap<>();
		headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key");
		headers.put("Access-Control-Allow-Methods", "*");
		headers.put("Access-Control-Allow-Origin", "*");
		headers.put("Content-Type", "application/json");
		return headers;
	}

	private void logError(final LambdaLogger logger, final Exception e) {
		e.printStackTrace();
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		logger.log(sw.toString());
	}

}
