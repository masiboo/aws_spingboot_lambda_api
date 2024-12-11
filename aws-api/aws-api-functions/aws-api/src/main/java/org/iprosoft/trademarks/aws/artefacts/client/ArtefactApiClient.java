package org.iprosoft.trademarks.aws.artefacts.client;

import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactsEntityDTO;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class ArtefactApiClient {

	private final RestTemplate restTemplate;

	private final String baseUrl;

	public ArtefactApiClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
		this.baseUrl = SystemEnvironmentVariables.Aws_CORE_DB_INIT_ACCESS_URL;
	}

	/**
	 * Creates an artefact using the provided ArtefactsEntity object.
	 * @param artefact The ArtefactsEntity object created using the Builder pattern
	 * @return The created ArtefactsEntity object returned by the API
	 */
	public ArtefactsEntityDTO createArtefact(ArtefactsEntityDTO artefact) {
		String url = baseUrl + "/api/v1/artefacts";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<ArtefactsEntityDTO> request = new HttpEntity<>(artefact, headers);
		log.warn("Artefact 1.1");
		ResponseEntity<ArtefactsEntityDTO> response = restTemplate.exchange(url, HttpMethod.POST, request,
				ArtefactsEntityDTO.class);
		log.warn("Artefact 1.2- response: {}", response);
		return response.getBody();
	}

}
