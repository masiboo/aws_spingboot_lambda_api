package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.convertresizeimagetotif;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.aws.s3.S3Service;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactMetadata;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ConvertImageRequest;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ConvertImageResponse;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ImageToTifResponse;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactClassType;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.service.mediaprocessing.MediaProcessingService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.iprosoft.trademarks.aws.artefacts.util.GsonUtil;
import org.iprosoft.trademarks.aws.artefacts.util.SafeParserUtil;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class ConvertResizeImageToTif implements Function<InputStream, APIGatewayV2HTTPResponse> {

	private final MediaProcessingService mediaProcessingService;

	private final ArtefactService artefactService;

	private final S3Service s3Service;

	private final ObjectMapper objectMapper;

	@Override
	@SneakyThrows
	public APIGatewayV2HTTPResponse apply(InputStream in) {
		log.info("InputStream Dump: {}", in);

		APIGatewayV2HTTPEvent event = GsonUtil.getInstance()
			.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), APIGatewayV2HTTPEvent.class);

		log.info("APIGatewayV2HTTPEvent: {}", event);

		Map<String, String> pathParameters = event.getPathParameters();

		if (pathParameters != null && pathParameters.containsKey("mirisDocId")
				&& StringUtils.hasText(pathParameters.get("mirisDocId"))) {
			String mirisDocId = pathParameters.get("mirisDocId");
			log.info("Path parameter mirisDocId: {}", mirisDocId);
			List<Artefact> logoArtefacts;
			List<Artefact> multimediaArtefacts;
			List<ImageToTifResponse> convertImageRequests = new ArrayList<>();
			List<Artefact> artefacts = artefactService.getArtefactbyMirisDocId(mirisDocId);
			log.info("After artefactService.getArtefactbyMirisDocId(mirisDocId) found  artefacts {}", artefacts);
			if (artefacts != null && !artefacts.isEmpty()) {
				logoArtefacts = getFilteredLogoArtefacts(artefacts);
				multimediaArtefacts = getFilteredMultimediaArtefacts(artefacts);
				log.info("After getFilteredLogoArtefacts(artefacts) found  logoArtefacts {}", logoArtefacts);
				log.info("After getFilteredMultimediaArtefacts(artefacts) found  multimediaArtefacts {}",
						multimediaArtefacts);
				if (logoArtefacts != null && !logoArtefacts.isEmpty()) {
					List<ImageToTifResponse> imageToTifResponses = getImageToTifResponse(logoArtefacts);
					convertImageRequests.addAll(imageToTifResponses);
					log.info("After  getImageToTifResponse(logoArtefacts) convertImageRequests {}, size {}",
							convertImageRequests, convertImageRequests.size());
				}
				if (multimediaArtefacts != null && !multimediaArtefacts.isEmpty()) {
					List<ImageToTifResponse> multimediaResponseResponses = getMultimediaResponse(multimediaArtefacts);
					convertImageRequests.addAll(multimediaResponseResponses);
					log.info("After  getMultimediaResponse(multimediaArtefacts) convertImageRequests {}, size {}",
							convertImageRequests, convertImageRequests.size());
				}
			}
			else {
				log.error("Artefact not found by mirisDocId: {}", mirisDocId);
				return getErrorResponse("Artefact not found by mirisDocId: " + mirisDocId);
			}

			if (!Objects.requireNonNull(convertImageRequests).isEmpty()) {
				log.info("Return success convertImageRequests {}", convertImageRequests);
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.CREATED,
						objectMapper.writeValueAsString(convertImageRequests), false);
			}
			else {
				String errorMessage = makeErrorMessage(mirisDocId, artefacts, logoArtefacts, convertImageRequests);
				log.error("Return errorMessage: {}", errorMessage);
				return getErrorResponse(errorMessage);
			}
		}
		else {
			log.warn("Missing 'mirisDocId' parameter in path");
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
					"'mirisDocId' must be present with non-empty values.", true);
		}
	}

	private List<ImageToTifResponse> getMultimediaResponse(List<Artefact> multimediaArtefacts) {
		List<ImageToTifResponse> imageToTifResponses = new ArrayList<>();
		for (Artefact artefact : multimediaArtefacts) {
			String signedS3Url = "";
			String statusCode;
			long size = 0;
			try {
				signedS3Url = s3Service
					.presignGetUrl(artefact.getS3Bucket(), artefact.getS3Key(), Duration.ofHours(1), null)
					.toString();
				statusCode = HttpStatus.CREATED.toString();
				log.info("signedS3Url {}", signedS3Url);
				ResponseInputStream<GetObjectResponse> s3Object = s3Service.getObject(artefact.getS3Bucket(),
						artefact.getS3Key());
				if (s3Object != null) {
					size = s3Object.response().contentLength();
					log.info("s3Object != null {} objectSize {}", s3Object, size);
				}
				else {
					log.info("s3Object == null {} objectSize {}", s3Object, size);
				}
			}
			catch (Exception e) {
				log.info("Exception when s3Service.presignGetUrl {}", e.getMessage());
				statusCode = HttpStatus.INTERNAL_SERVER_ERROR.toString();
			}
			ArtefactMetadata artefactMetadata = artefactService.getArtefactInfoById(artefact.getId());
			log.info("found metaData: {}", artefactMetadata);
			if (artefactMetadata.getSize() == null || SafeParserUtil.safeParseLong(artefactMetadata.getSize()) <= 0) {
				artefactMetadata.setSize(String.valueOf(size));
			}
			artefactMetadata.setClassType(artefact.getArtefactClassType());
			imageToTifResponses.add(getMultimediaResponse(signedS3Url, artefactMetadata, statusCode));
		}
		log.info("All found MultimediaResponse list: {} size: {}", imageToTifResponses, imageToTifResponses.size());
		return imageToTifResponses;
	}

	private List<ImageToTifResponse> getImageToTifResponse(List<Artefact> logoArtefacts) {
		List<ImageToTifResponse> imageToTifResponses = new ArrayList<>();
		for (Artefact artefact : logoArtefacts) {
			ConvertImageRequest convertImageRequest = new ConvertImageRequest();
			convertImageRequest.setBucket(artefact.getS3Bucket());
			convertImageRequest.setKey(artefact.getS3Key());
			ArtefactMetadata artefactMetadata = artefactService.getArtefactInfoById(artefact.getId());
			artefactMetadata.setClassType(artefact.getArtefactClassType());
			log.info("ArtefactMetadata: {}", artefactMetadata);
			String dpi = artefactMetadata.getResolutionInDpi();
			if (dpi == null || dpi.isBlank()) {
				// if no DPI found we set default dpi
				dpi = "266";
			}
			ConvertImageResponse convertImageResponse = new ConvertImageResponse();
			try {
				convertImageResponse = mediaProcessingService.convertResizeImageToTif(convertImageRequest, dpi);
				if (convertImageResponse.getHttpStatus() == null) {
					convertImageResponse.setHttpStatus("201 CREATED");
				}
				log.info("By mirisDocId: {} Found signedS3Url: {} found metaData {}, HttpStatusCode {}",
						artefact.getMirisDocId(), convertImageResponse.getSignedS3Url(),
						convertImageResponse.getMetaData(), convertImageResponse.getHttpStatus());
				log.info("Found	convertImageResponse: {}", convertImageResponse);
			}
			catch (Exception e) {
				log.error("Exception in mediaProcessingService.convertResizeImageToTif: {}", e.getMessage());
				convertImageResponse.setHttpStatus("500 INTERNAL_SERVER_ERROR");
			}
			imageToTifResponses.add(getImageToTifResponse(convertImageResponse));
		}
		log.info("All found convertImageResponse list {} size: {}", imageToTifResponses, imageToTifResponses.size());
		return imageToTifResponses;
	}

	private ImageToTifResponse getImageToTifResponse(ConvertImageResponse convertImageResponse) {
		log.info("convertImageResponse metaData: {}", convertImageResponse.getMetaData());
		return ImageToTifResponse.builder()
			.metaData(convertImageResponse.getMetaData())
			.signedS3Url(convertImageResponse.getSignedS3Url())
			.errorMessage(convertImageResponse.getErrorMessage())
			.statusCode(convertImageResponse.getHttpStatus())
			.build();
	}

	private ImageToTifResponse getMultimediaResponse(String signedS3Url, ArtefactMetadata artefactMetadata,
			String statusCode) {
		Map<String, String> metadata = metadataToMap(artefactMetadata);
		log.info("metadataToMap: {}", metadata);
		return ImageToTifResponse.builder()
			.metaData(metadata)
			.signedS3Url(signedS3Url)
			.errorMessage(null)
			.statusCode(statusCode)
			.build();
	}

	private List<Artefact> getFilteredLogoArtefacts(List<Artefact> artefacts) {
		return artefacts.stream().filter(artefact -> {
			String artefactClassType = artefact.getArtefactClassType().toLowerCase();
			return artefactClassType.contains(ArtefactClassType.BWLOGO.name().toLowerCase())
					|| artefactClassType.contains(ArtefactClassType.COLOURLOGO.name().toLowerCase());
		}).collect(Collectors.toList());
	}

	private List<Artefact> getFilteredMultimediaArtefacts(List<Artefact> artefacts) {
		return artefacts.stream().filter(artefact -> {
			String artefactClassType = artefact.getArtefactClassType().toLowerCase();
			return artefactClassType.contains(ArtefactClassType.SOUND.name().toLowerCase())
					|| artefactClassType.contains(ArtefactClassType.MULTIMEDIA.name().toLowerCase());
		}).collect(Collectors.toList());
	}

	APIGatewayV2HTTPResponse getErrorResponse(String errorMessage) {
		return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED, errorMessage);
	}

	private String makeErrorMessage(String mirisDocId, List<Artefact> artefacts, List<Artefact> logoArtefacts,
			List<ImageToTifResponse> convertImageRequests) {
		StringBuilder sb = new StringBuilder();
		if (mirisDocId == null) {
			sb.append("mirisDocId missing in path parameter.");
		}
		if (artefacts == null) {
			sb.append(" Artefact not found by mirisDocId: ").append(mirisDocId);
		}
		if (logoArtefacts == null) {
			sb.append(" No BWLOGO or COLOURLOGO artefact found by mirisDocId: ").append(mirisDocId);
		}
		if (convertImageRequests == null) {
			sb.append(" Failed to get expected response from media-processing-svc");
		}
		return sb.toString();
	}

	public static Map<String, String> metadataToMap(ArtefactMetadata metadata) {
		Map<String, String> metadataMap = new HashMap<>();
		try {
			for (Field field : metadata.getClass().getDeclaredFields()) {
				field.setAccessible(true);
				log.info("metadata field {} ", field);
				Object value = field.get(metadata);
				log.info("metadata value {} ", value);
				if (value != null) {
					metadataMap.put(field.getName(), value.toString());
				}
			}
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException("Failed to convert ArtefactMetadata to map", e);
		}
		return metadataMap;
	}

}
