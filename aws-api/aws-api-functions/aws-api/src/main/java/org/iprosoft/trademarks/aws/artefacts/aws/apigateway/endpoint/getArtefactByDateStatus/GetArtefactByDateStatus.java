package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getArtefactByDateStatus;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.HttpResponseConstant;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactStatus;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactUploadRequestUtil;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.utils.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiResponseStatus.SC_OK;

@AllArgsConstructor
@Slf4j
public class GetArtefactByDateStatus implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final ArtefactService artefactService;

	@Override
	@SneakyThrows
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		log.info("APIGatewayV2HTTPEvent: {}", event);
		try {
			String date = null;
			String status = null;
			if (event.getQueryStringParameters() != null && !event.getQueryStringParameters().isEmpty()) {
				date = event.getQueryStringParameters().get("date");
				status = event.getQueryStringParameters().get("status");
				Map<String, Object> validationErrors = validateQueryParams(date, status);
				if (!validationErrors.isEmpty()) {
					log.error("Query params validate error {}",
							ArtefactUploadRequestUtil.objectMapToString(validationErrors));
					return ArtefactUploadRequestUtil.createErrorResponse(validationErrors);
				}
			}

			List<Artefact> artefacts = artefactService.getAllArtefacts(date, status);
			log.info("Found  artefacts {}, size {}", artefacts.toString(), artefacts.size());
			if (artefacts.isEmpty()) {
				return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED, "No artefacts found");
			}

			Map<String, Object> outMap = new HashMap<>();
			outMap.put("artefacts", artefacts);

			ObjectMapper objectMapper = new ObjectMapper();
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(SC_OK.getStatusCode(),
					objectMapper.writeValueAsString(outMap), false);

		}
		catch (Exception e) {
			log.error(e.getMessage());
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpResponseConstant.STATUS_CODE_SERVER_ERROR,
					e.getMessage(), true);
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
				validationErrors.put("status",
						"Invalid status given allowed values are " + Arrays.toString(ArtefactStatus.values()));
			}
		}
		else {
			validationErrors.put("status",
					"Status is null/empty. Status should have a value of " + Arrays.toString(ArtefactStatus.values()));
		}
		return validationErrors;
	}

}
