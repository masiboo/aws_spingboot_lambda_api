package org.iprosoft.trademarks.aws.artefacts.service.miris;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;

@Service
@Slf4j
public class MirisServiceImpl implements MirisService {

	@Override
	public boolean isDocIdValid(String docId) {
		String mirisDocIdValidationPattern = "^[0-9]{5,8}$";
		if (!Pattern.matches(mirisDocIdValidationPattern, docId)) {
			return false;
		}

		String mirisCheckApiUrl = SystemEnvironmentVariables.Aws_CORE_MIRIS_CHECK_API_URL;
		try {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(mirisCheckApiUrl + "/api/v1/validate/documents/" + docId))
				.GET()
				.build();
			HttpResponse<String> validatorResp = client.send(request, HttpResponse.BodyHandlers.ofString());
			log.info("The given docId:{} is valid : {}", docId, validatorResp.body());
			return Boolean.parseBoolean(validatorResp.body());
		}
		catch (IOException | InterruptedException e) {
			log.error("Exception while validating the mirisDocId", e);
		}
		return false;
	}

}
