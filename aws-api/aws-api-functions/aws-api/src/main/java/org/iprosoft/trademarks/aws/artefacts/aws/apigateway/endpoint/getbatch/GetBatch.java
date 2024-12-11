package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getbatch;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactOutput;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchOutput;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactClassType;
import org.iprosoft.trademarks.aws.artefacts.model.mapper.ArtefactToArtefactOutputMapper;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.service.batch.BatchService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactStatus;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class GetBatch implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final ObjectMapper objectMapper;

	private final BatchService batchService;

	private final ArtefactService artefactService;

	@Override
	@SneakyThrows
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		log.info("APIGatewayV2HTTPEvent: " + event);
		if (CollectionUtils.isNotEmpty(event.getPathParameters())
				&& event.getPathParameters().containsKey("batchSeq")) {
			String batchSeq = event.getPathParameters().get("batchSeq");
			if (StringUtils.hasText(batchSeq)) {
				BatchOutput batchOutput = batchService.getBatchDetail(batchSeq);
				List<ArtefactOutput> allArtefacts = batchService.getAllArtefactsForBatch(batchSeq, "artefact");
				if (!allArtefacts.isEmpty()) {
					// filter the Artefact which are eligible for indexation
					Predicate<Artefact> eligibleForIndexation = artefact -> ArtefactStatus.INSERTED.getStatus()
						.equalsIgnoreCase(artefact.getStatus())
							&& !ArtefactClassType.PART.name().equalsIgnoreCase(artefact.getArtefactClassType());
					List<ArtefactOutput> indexEligibleArtefacts = allArtefacts.stream()
						.map(x -> artefactService.getArtefactById(x.getId()))
						.filter(eligibleForIndexation)
						.map(new ArtefactToArtefactOutputMapper())
						.collect(Collectors.toList());
					batchOutput.setArtefacts(indexEligibleArtefacts);
					return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK,
							objectMapper.writeValueAsString(batchOutput), false);
				}
				else {
					return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED, "No Batch found");
				}
			}
			else {
				log.warn("'batchSeq' parameter is empty");
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
						"'batchSeq' parameter is empty", true);
			}
		}
		else {
			log.warn("Missing 'batchSeq' parameter in path");
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
					"Missing 'batchSeq' parameter in path", true);
		}
	}

}
