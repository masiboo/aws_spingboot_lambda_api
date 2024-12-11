package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.authorizer;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2CustomAuthorizerEvent;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.service.ssm.SSMParamStoreService;
import org.iprosoft.trademarks.aws.artefacts.util.AppConstants;
import org.iprosoft.trademarks.aws.artefacts.util.JWTUtils;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class Authorizer implements Function<APIGatewayV2CustomAuthorizerEvent, Map<String, Object>> {

	private static final String ALLOW = "Allow";

	private static final String DENY = "Deny";

	private static final String AUTHORIZATION = "authorization";

	private final SSMParamStoreService ssmParamStoreService;

	@Override
	@SneakyThrows
	public Map<String, Object> apply(APIGatewayV2CustomAuthorizerEvent event) {
		Map<String, Object> authResp = new LinkedHashMap<>();
		String effect = DENY;
		String routeArn = event.getRouteArn();
		if (CollectionUtils.isNotEmpty(event.getHeaders())
				&& StringUtils.hasText(event.getHeaders().get(AUTHORIZATION))) {
			String authToken = event.getHeaders().get(AUTHORIZATION);
			final String secretKey = ssmParamStoreService.getParamValue(AppConstants.JWT_SECRET_KEY);
			if (JWTUtils.verifyToken(authToken, secretKey)) {
				effect = ALLOW;
			}
		}

		authResp.put("principalId", "user");
		authResp.put("policyDocument", generatePolicyDocument(effect, routeArn));

		return authResp;
	}

	private Map<String, Object> generatePolicyDocument(String effect, String resource) {
		Map<String, Object> policyDoc = new HashMap<>();
		policyDoc.put("Version", "2012-10-27");
		Map<String, String> statement = new LinkedHashMap<>();
		statement.put("Action", "execute-api:Invoke");
		statement.put("Effect", effect);
		statement.put("Resources", resource);
		policyDoc.put("Statement", List.of(statement));
		return policyDoc;
	}

}
