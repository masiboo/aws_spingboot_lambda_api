package org.iprosoft.trademarks.aws.artefacts;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.lang.Nullable;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import org.springframework.aot.hint.TypeReference;

public class AwsRuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
		// Register httpapi serialization
		hints.serialization().registerType(TypeReference.of(APIGatewayV2HTTPEvent.class));
		hints.serialization().registerType(TypeReference.of(APIGatewayV2HTTPEvent.RequestContext.class));
		hints.serialization().registerType(TypeReference.of(APIGatewayV2HTTPEvent.RequestContext.Authorizer.class));
		hints.serialization().registerType(TypeReference.of(APIGatewayV2HTTPEvent.RequestContext.Http.class));
		hints.serialization().registerType(TypeReference.of(APIGatewayV2HTTPEvent.RequestContext.IAM.class));
		hints.serialization()
			.registerType(TypeReference.of(APIGatewayV2HTTPEvent.RequestContext.CognitoIdentity.class));
		// APIGatewayProxyRequestEvent
		hints.serialization().registerType(TypeReference.of(APIGatewayProxyRequestEvent.class));

	}

}
