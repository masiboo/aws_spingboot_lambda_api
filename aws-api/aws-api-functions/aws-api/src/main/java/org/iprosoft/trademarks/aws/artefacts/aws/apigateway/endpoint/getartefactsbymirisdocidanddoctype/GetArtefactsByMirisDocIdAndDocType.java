package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getartefactsbymirisdocidanddoctype;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class GetArtefactsByMirisDocIdAndDocType implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final ObjectMapper objectMapper;

	private final ArtefactService artefactService;

	@Override
	@SneakyThrows
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		if (CollectionUtils.isNotEmpty(event.getQueryStringParameters())
				&& event.getQueryStringParameters().containsKey("mirisDocId")) {
			String mirisDocId = event.getQueryStringParameters().get("mirisDocId");
			String docType = event.getQueryStringParameters() != null ? event.getQueryStringParameters().get("docType")
					: null;
			List<Artefact> artefacts;
			if (StringUtils.hasText(docType)) {
				List<String> classTypeList = Arrays.asList(docType.split("\\s*,\\s*"));
				for (String classType : classTypeList) {
					if (!artefactService.isValidClassType(classType)) {
						String errorMsg = "Invalid 'docType' provided :'" + classType + "'  and allowed values are "
								+ artefactService.getAllClassTypes();
						log.error(errorMsg);
						return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST, errorMsg,
								true);
					}
				}
				artefacts = artefactService.getArtefactbyMirisDocIdAndType(mirisDocId, classTypeList);
			}
			else {
				artefacts = artefactService.getArtefactbyMirisDocId(mirisDocId);
			}
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK,
					objectMapper.writeValueAsString(artefacts), false);
		}
		else {
			log.warn("Missing 'mirisDocId' parameter in path");
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
					"Missing 'mirisDocId' parameter in path", true);
		}
	}

}
