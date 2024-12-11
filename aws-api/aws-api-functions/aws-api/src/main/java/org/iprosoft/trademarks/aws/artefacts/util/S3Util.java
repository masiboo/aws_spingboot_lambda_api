package org.iprosoft.trademarks.aws.artefacts.util;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.aws.s3.S3Service;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class S3Util {

	public static String generatePresignedUrl(S3Service s3Service, Artefact artefact, Duration duration,
			String version) {
		URL url = s3Service.presignGetUrl(artefact.getS3Bucket(), artefact.getS3Key(), duration, version);
		String urlstring = url.toString();
		return urlstring;
	}

	public static int getDurationHours(final APIGatewayV2HTTPEvent event) {
		final int defaultDurationHours = 1;

		Map<String, String> map = event.getQueryStringParameters() != null ? event.getQueryStringParameters()
				: new HashMap<>();
		String durationHours = map.getOrDefault("duration", "" + defaultDurationHours);

		try {
			return Integer.parseInt(durationHours);
		}
		catch (NumberFormatException e) {
			return defaultDurationHours;
		}
	}

	public static boolean verifyS3UrlForObjectExist(String s3url) {
		try {
			URL url = new URL(s3url);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(5000);
			int responseCode = connection.getResponseCode();
			return responseCode == HttpURLConnection.HTTP_OK;
		}
		catch (IOException e) {
			log.warn("IOException {}", e.getMessage());
			return false;
		}
	}

}
