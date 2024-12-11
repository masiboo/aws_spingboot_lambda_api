package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getconsolidatedartefactsdocuments;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.dto.MergeFilesRequest;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.service.mediaprocessing.MediaProcessingService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class GetConsolidatedArtefactsDocuments implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final ArtefactService artefactService;

	private final MediaProcessingService mediaProcessingService;

	@Override
	@SneakyThrows
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		log.info("APIGatewayV2HTTPEvent: " + event);
		if (CollectionUtils.isNotEmpty(event.getQueryStringParameters())
				&& event.getQueryStringParameters().containsKey("artefactIds")) {
			String artefactIds = event.getQueryStringParameters().get("artefactIds");
			List<String> artefactIdList = Arrays.stream(artefactIds.split(",")).toList();
			// A map containing artefactId as key
			// and as a value it would be another key value pair of s3 bucket and s3 key
			MergeFilesRequest request = new MergeFilesRequest();
			List<MergeFilesRequest.KeyValuePair> keyValuePairList = new ArrayList<>();
			for (String artefactId : artefactIdList) {
				Artefact artefact = artefactService.getArtefactById(artefactId);
				if (artefact != null) {
					MergeFilesRequest.KeyValuePair keyValuePair = new MergeFilesRequest.KeyValuePair();
					keyValuePair.setBucket(artefact.getS3Bucket());
					keyValuePair.setKey(artefact.getS3Key());
					keyValuePairList.add(keyValuePair);
				}
			}
			request.setObjects(keyValuePairList);
			if (request.getObjects().isEmpty()) {
				return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED, "No artefacts found");
			}
			else {
				// Invoke the service that fetches and merges s3 objects
				try {
					String signedS3Url = mediaProcessingService.mergeFiles(request);
					return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK,
							"signedS3Url: " + signedS3Url, false);
				}
				catch (Exception e) {
					log.info(e.getMessage());
					return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.INTERNAL_SERVER_ERROR,
							"exception: " + e.getMessage(), true);
				}
			}
		}
		else {
			log.warn("Missing 'artefactIds' parameter in path");
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
					"Missing 'artefactIds' parameter in path", true);
		}
	}

}