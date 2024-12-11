package artefact.util;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import java.util.Map;

import static software.amazon.awssdk.http.Header.CONTENT_TYPE;

public class ApiGatewayResponseUtil {

    public static APIGatewayV2HTTPResponse getAPIGatewayV2HTTPResponse(int statusCode, String body, boolean isError){
        String responseBody;
        if(isError){
           responseBody = String.format("{\"errorMessage\": \"%s\"}", body);
       }else{
           responseBody = body;
       }

        return APIGatewayV2HTTPResponse.builder()
                .withStatusCode(statusCode)
                .withHeaders(Map.of(CONTENT_TYPE, "application/json"))
                .withBody(responseBody)
                .build();
    }

}
