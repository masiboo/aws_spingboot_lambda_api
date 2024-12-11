package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.indexartefact;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactIndexDto;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactBatch;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.service.batch.BatchService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactStatus;
import org.iprosoft.trademarks.aws.artefacts.util.ExtractApiParameterHttpMethodUtil;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.http.HttpStatusCode;
import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;

import java.io.IOException;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class IndexArtefact implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final ArtefactService artefactService;

	private final BatchService batchService;

	@Override
	@SneakyThrows
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		log.info("APIGatewayV2HTTPEvent: {}", event);
		try {
			String artefactId = ExtractApiParameterHttpMethodUtil.extractFirstParameterWithoutName(event, false);
			if (StringUtils.hasText(artefactId)) {
				if (!StringUtils.hasText(event.getBody())) {
					return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
							"Empty request body", true);
				}
				ArtefactIndexDto artefactIndexDto;
				try {
					artefactIndexDto = new ObjectMapper().readValue(event.getBody(), ArtefactIndexDto.class);
				}
				catch (IOException e) {
					return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
							"Failed to parse mirisDocId from request body", true);
				}

				if (Boolean.parseBoolean(SystemEnvironmentVariables.MIRIS_CHECK_ENABLED)
						&& !artefactService.isDocIdValid(artefactIndexDto.getMirisDocId())) {
					return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
							"The given mirisDocId is invalid: " + artefactIndexDto.getMirisDocId(), true);
				}

				Artefact artefact = artefactService.getArtefactById(artefactId);
				if (artefact == null) {
					return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED,
							"The artefact is not found with id: " + artefactId);
				}
				// VALIDATION If the artefact is already INDEXED or DELETED
				if (ArtefactStatus.INDEXED.getStatus().equalsIgnoreCase(artefact.getStatus())) {
					return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
							"Artefact is already in INDEXED mirisDocId " + artefact.getMirisDocId(), true);
				}
				if (ArtefactStatus.DELETED.getStatus().equalsIgnoreCase(artefact.getStatus())) {
					return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
							"Artefact status is DELETED and can not be INDEXED mirisDocId: " + artefact.getMirisDocId(),
							true);
				}
				// Change status of artefact to index
				artefactService.indexArtefact(artefactId, artefactIndexDto, ArtefactStatus.INDEXED);
				ArtefactBatch artefactBatch = artefactService.getArtectBatchById(artefactId);
				batchService.updateBatchIfAllIndexed(artefactBatch.getBatchSequence());
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK,
						"Successfully indexed the Artefact mirisDocId: " + artefact.getMirisDocId(), false);

			}
			else {
				log.warn("'artefactId' parameter is empty");
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
						"'artefactId' parameter is empty", true);
			}
		}
		catch (Exception e) {
			log.error("Internal server error occurred {}", e.getMessage());
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.INTERNAL_SERVER_ERROR,
					"Internal server error occurred: " + e.getMessage(), true);
		}
	}

}
