package org.wipo.trademarks.aws.Aws.integration.util;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class APIGatewayUtil {

    public static APIGatewayV2HTTPEvent createGatewayRequestEvent(String rawPath,String routeKey){
        APIGatewayV2HTTPEvent apiRequestEvent = new APIGatewayV2HTTPEvent();
        apiRequestEvent.setRawPath(rawPath);
        apiRequestEvent.setRouteKey(routeKey);
        return apiRequestEvent;
    }

    public static APIGatewayV2HTTPResponse buildGatewayResponse(Integer statusCode, String body) {
        return APIGatewayV2HTTPResponse.builder()
                .withStatusCode(statusCode)
                .withBody(body)
                .build();
    }

    public static void assertApiResponseEquals(APIGatewayV2HTTPResponse expectedApiResponse, APIGatewayV2HTTPResponse actualApiResponse) {
        System.out.println(actualApiResponse.getStatusCode() + " : " + actualApiResponse.getBody());
        assertEquals(expectedApiResponse.getStatusCode(),actualApiResponse.getStatusCode());
        assertEquals(expectedApiResponse.getBody(),actualApiResponse.getBody());
    }
}
