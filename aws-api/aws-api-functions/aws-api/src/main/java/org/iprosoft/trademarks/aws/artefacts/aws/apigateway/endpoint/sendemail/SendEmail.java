package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.sendemail;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;
import org.iprosoft.trademarks.aws.artefacts.errorhandling.RestServiceFaultException;
import org.iprosoft.trademarks.aws.artefacts.model.dto.EmailDetails;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.http.HttpStatusCode;

import java.io.IOException;
import java.net.URI;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class SendEmail implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final ObjectMapper objectMapper;

	private final RestTemplate restTemplate;

	@Override
	@SneakyThrows
	@RegisterReflectionForBinding(EmailDetails.class)
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		if (event.getBody() == null || event.getBody().isEmpty()) {
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST, "Empty request body",
					true);
		}
		EmailDetails emailDetails;
		try {
			emailDetails = objectMapper.readValue(event.getBody(), EmailDetails.class);
		}
		catch (IOException e) {
			log.error(e.getMessage());
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
					"Failed to parse email details from request body " + e.getMessage(), true);
		}
		try {
			String emailSvcEndpoint = SystemEnvironmentVariables.Aws_CORE_EMAIL_SERVICE_API_URL + "/api/v1/email";
			restTemplate.exchange(RequestEntity.post(new URI(emailSvcEndpoint)).body(emailDetails), Void.class);
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK, "Email sent successfully.",
					false);
		}
		catch (Exception e) {
			log.error("SendEmail exception ", e);
			// TODO condition added to unblock UI since IAM for Email service is not
			// finalized - remove dummy OK response once sufficient IAM for Email service
			// is ready
			if ("true".equalsIgnoreCase(SystemEnvironmentVariables.UNBLOCK_UI)) {
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK, "", false);
			}
			else {
				try {
					RestServiceFaultException restServiceFaultException = objectMapper
						.readValue(e.getMessage().substring(7), RestServiceFaultException.class);
					return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(
							restServiceFaultException.getServiceFault().getCode(),
							restServiceFaultException.getServiceFault().getMessage(), true);
				}
				catch (Exception ex) {
					return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.INTERNAL_SERVER_ERROR,
							"Error: " + e.getMessage(), true);
				}
			}
		}
	}

}
