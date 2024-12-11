package org.iprosoft.trademarks.aws.artefacts.service.database;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactsDTO;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactNotesEntity;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactStatusEnum;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactsEntity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class DatabaseServiceImpl implements DatabaseService {

	private final RestTemplate restTemplate;

	@Override
	public List<ArtefactsEntity> filterArtefacts(ArtefactStatusEnum status, String mirisDocId) {
		String baseUrl = SystemEnvironmentVariables.Aws_CORE_DB_INIT_ACCESS_URL;

		// Build the URL with actual values
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + "/api/v1/artefacts/filter")
			.queryParam("status", status)
			.queryParam("mirisDocId", mirisDocId);

		String finalUrl = builder.toUriString();
		return restTemplate
			.exchange(finalUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<ArtefactsEntity>>() {
			})
			.getBody();
	}

	@Override
	public ArtefactsDTO createArtefact(ArtefactsDTO artefactsEntity) {
		String baseUrl = SystemEnvironmentVariables.Aws_CORE_DB_INIT_ACCESS_URL;

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + "/api/v1/artefacts");
		String finalUrl = builder.toUriString();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		try {
			RequestEntity<ArtefactsDTO> request = new RequestEntity<>(artefactsEntity, headers, HttpMethod.POST,
					new URI(finalUrl));
			return restTemplate.exchange(request, ArtefactsDTO.class).getBody();
		}
		catch (RestClientException | URISyntaxException e) {
			log.error("Unable to create the Artefact", e);
			throw new RuntimeException(e);

		}
	}

	@Override
	public ArtefactNotesEntity createArtefactNote(ArtefactNotesEntity artefactNotesEntity) {
		try {
			return restTemplate
				.exchange(RequestEntity
					.post(new URI(SystemEnvironmentVariables.Aws_CORE_DB_INIT_ACCESS_URL + "/api/v1/artefact-notes"))
					.body(artefactNotesEntity), ArtefactNotesEntity.class)
				.getBody();
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<ArtefactNotesEntity> filterArtefactNotes(String mirisDocId) {
		String baseUrl = SystemEnvironmentVariables.Aws_CORE_DB_INIT_ACCESS_URL;

		// Build the URL with actual values
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + "/api/v1/artefact-notes/filter")
			.queryParam("mirisDocId", mirisDocId);

		String finalUrl = builder.toUriString();
		return restTemplate
			.exchange(finalUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<ArtefactNotesEntity>>() {
			})
			.getBody();
	}

}
