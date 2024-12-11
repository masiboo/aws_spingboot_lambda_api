package org.iprosoft.trademarks.aws.artefacts.actuator.endpoints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Endpoint(id = "apiversion")
@Slf4j
public class ApiVersionEndpoint {

	@Value("${Aws-core.version}")
	private String coreVersion;

	@Value("${Aws-api.version}")
	private String apiVersion;

	@ReadOperation
	public Map<String, String> getApiVersion() {
		return Map.of("apiVersion", apiVersion, "coreVersion", coreVersion);
	}

}
