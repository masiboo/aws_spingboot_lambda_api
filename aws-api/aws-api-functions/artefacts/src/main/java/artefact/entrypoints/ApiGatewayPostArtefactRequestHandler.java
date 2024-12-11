// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package artefact.entrypoints;

import artefact.entity.Artefact;
import artefact.util.ApiGatewayResponseUtil;
import artefact.util.ConstantUtil;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import artefact.adapter.ArtefactStore;
import artefact.adapter.dynamodb.DynamoDbArtefactStore;
import artefact.usecase.ArtefactValidation;
import artefact.usecase.ArtefactValidationInterface;

import java.io.IOException;

public class ApiGatewayPostArtefactRequestHandler
		implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private static final Logger logger = LoggerFactory.getLogger(ApiGatewayPostArtefactRequestHandler.class);
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final ArtefactStore artefactStore;
	private ArtefactValidationInterface validator  = new ArtefactValidation();

	public ApiGatewayPostArtefactRequestHandler() {
		this(new DynamoDbArtefactStore());
	}

	public ApiGatewayPostArtefactRequestHandler(ArtefactStore artefactStore) {
		this.artefactStore = artefactStore;
	}

	@Override
	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
		logger.info("Event body: " + event.getBody());

		if (event.getBody() == null || event.getBody().isEmpty()) {
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(ConstantUtil.STATUS_CODE_BAD_REQUEST,
																		"Event has empty request body", true);
		}

		Artefact artefact;
		try {
			artefact = objectMapper.readValue(event.getBody(), Artefact.class);
		} catch (IOException e) {
			logger.error(e.getMessage());
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(ConstantUtil.STATUS_CODE_BAD_REQUEST,
										"Failed to parse product from request body "+e.getMessage(), true);
		}
		logger.info("Parsed: " + artefact);

		if (!validator.validateArtefact(artefact)) {
			logger.error("failed to validate the Artefact");
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(ConstantUtil.STATUS_CODE_BAD_REQUEST,
					"Artefact validation error mirisDocId: "+artefact.getMirisDocId() != null?  artefact.getMirisDocId() : null,
					true);
		}

		try {
			artefactStore.postArtefact(artefact);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(ConstantUtil.STATUS_CODE_SERVER_ERROR,
										"Error during artefactStore.postArtefact: "+e.getMessage(), true);
		}

		return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(ConstantUtil.STATUS_CODE_CREATED,
										"Product created with mirisDocId: "+artefact.getMirisDocId(), false);
	}
}
