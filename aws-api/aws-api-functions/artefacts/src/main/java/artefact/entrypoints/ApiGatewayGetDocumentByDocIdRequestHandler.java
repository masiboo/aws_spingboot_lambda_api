// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package artefact.entrypoints;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import artefact.adapter.ArtefactStore;
import artefact.adapter.dynamodb.DynamoDbArtefactStore;
import artefact.entity.Artefact;
import artefact.util.ApiGatewayResponseUtil;

public class ApiGatewayGetDocumentByDocIdRequestHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayGetDocumentByDocIdRequestHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ArtefactStore artefactStore;

    public ApiGatewayGetDocumentByDocIdRequestHandler() {
        this(new DynamoDbArtefactStore());
    }

    public ApiGatewayGetDocumentByDocIdRequestHandler(ArtefactStore artefactStore) {
        this.artefactStore = artefactStore;
    }

    @Override
        public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
    	logger.info("dump event : " + event);
    	String id = null;
		try {
			id = event.getQueryStringParameters().get("id");
		} catch (Exception e) {
    		logger.error("error getting QUERY STRING PARAMETER MAP", e);
            return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(400,
                                                    "Missing 'id' parameter in path "+e.getMessage(), true);
        }
		logger.info("id : " + id);

        Artefact artefact;
        try {
        	artefact = artefactStore.getArtefactByDocId(id);
        } catch (Exception e) {
            logger.error("error getting artefact with id= "+ id, e);
            return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(404,
                                            "error getting artefact with id= "+ id +" "+e.getMessage(), true);
        }

        try {
            return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(200,
                                                                objectMapper.writeValueAsString(artefact), false);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
            return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(500,
                                                        "Failed to get artefact: " + e.getMessage(), true);
        }
    }
}
