package org.wipo.trademarks.Aws.artefacts.util;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExtractApiParameterHttpMethodUtilTest {

	@Test
	public void testExtractParametersPathParametersAllParametersPresent() {
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		event.setPathParameters(Map.of("param1", "value1", "param2", "value2"));

		List<String> result = ExtractApiParameterHttpMethodUtil.extractParameters(event, true);
		assertEquals(2, result.size());
	}

	@Test
	public void testExtractParametersPathParametersSomeParametersMissing() {
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		event.setPathParameters(Map.of("param1", "value1"));

		List<String> result = ExtractApiParameterHttpMethodUtil.extractParameters(event, true);

		assertEquals(1, result.size());
		assertEquals("value1", result.get(0));
	}

	@Test
	public void testExtractParametersPathParametersNoParametersPresent() {
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		event.setPathParameters(Map.of());

		List<String> result = ExtractApiParameterHttpMethodUtil.extractParameters(event, true);

		assertEquals(0, result.size());
	}

	@Test
	public void testExtractParametersPathParametersNullParameters() {
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		event.setPathParameters(null);

		List<String> result = ExtractApiParameterHttpMethodUtil.extractParameters(event, true);

		assertEquals(0, result.size());
	}

	@Test
	public void testExtractParametersPathParametersEmptyParameters() {
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		event.setPathParameters(Map.of("param1", "", "param2", " "));

		List<String> result = ExtractApiParameterHttpMethodUtil.extractParameters(event, true);

		assertEquals(0, result.size());
	}

	@Test
	public void testExtractParametersPathParametersWhenNoPathParameters() {
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();

		List<String> result = ExtractApiParameterHttpMethodUtil.extractParameters(event, true);

		assertEquals(0, result.size());
	}

	@Test
	public void testExtractParametersQueryParametersAllParametersPresent() {
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		event.setQueryStringParameters(Map.of("param1", "value1", "param2", "value2"));

		List<String> result = ExtractApiParameterHttpMethodUtil.extractParameters(event, false);

		assertEquals(2, result.size());
	}

	@Test
	public void testExtractParametersQueryParametersSomeParametersMissing() {
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		event.setQueryStringParameters(Map.of("param1", "value1"));

		List<String> result = ExtractApiParameterHttpMethodUtil.extractParameters(event, false);

		assertEquals(1, result.size());
		assertEquals("value1", result.get(0));
	}

	@Test
	public void testExtractParametersQueryParametersNoParametersPresent() {
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		event.setQueryStringParameters(Map.of());

		List<String> result = ExtractApiParameterHttpMethodUtil.extractParameters(event, false);

		assertEquals(0, result.size());
	}

	@Test
	public void testExtractParametersQueryParametersNullParameters() {
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		event.setQueryStringParameters(null);

		List<String> result = ExtractApiParameterHttpMethodUtil.extractParameters(event, false);

		assertEquals(0, result.size());
	}

	@Test
	public void testExtractParametersQueryParametersEmptyParameters() {
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		event.setQueryStringParameters(Map.of("param1", "", "param2", " "));

		List<String> result = ExtractApiParameterHttpMethodUtil.extractParameters(event, false);

		assertEquals(0, result.size());
	}

	@Test
	public void testExtractParametersQueryParametersWhenNoPathParameters() {
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();

		List<String> result = ExtractApiParameterHttpMethodUtil.extractParameters(event, false);

		assertEquals(0, result.size());
	}

	@Test
	public void testExtractHttpMethodPost() {
		APIGatewayV2HTTPEvent event = createEventWithHttpMethod("post");

		String result = ExtractApiParameterHttpMethodUtil.extractHttpMethod(event);

		assertEquals("post", result);
	}

	@Test
	public void testExtractHttpMethodGet() {
		APIGatewayV2HTTPEvent event = createEventWithHttpMethod("get");

		String result = ExtractApiParameterHttpMethodUtil.extractHttpMethod(event);

		assertEquals("get", result);
	}

	@Test
	public void testExtractHttpMethodWhenRequestContextNull() {
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();

		String result = ExtractApiParameterHttpMethodUtil.extractHttpMethod(event);

		assertNull(result);
	}

	@Test
	public void testExtractHttpMethodWhenHttpMethodNull() {
		APIGatewayV2HTTPEvent event = createEventWithHttpMethod(null);

		String result = ExtractApiParameterHttpMethodUtil.extractHttpMethod(event);

		assertNull(result);
	}

	@Test
	public void testExtractFirstParametersIgnoreCasePathParameters() {
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		event.setPathParameters(Map.of("parMa1", "value1"));

		String result = ExtractApiParameterHttpMethodUtil.extractFirstParameterWithoutName(event, true);

		assertEquals("value1", result);
	}

	@Test
	public void testExtractFirstParametersIgnoreCaseQueryStringParameters() {
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		event.setQueryStringParameters(Map.of("parMa1", "value1"));

		String result = ExtractApiParameterHttpMethodUtil.extractFirstParameterWithoutName(event, false);

		assertEquals("value1", result);
	}

	private APIGatewayV2HTTPEvent createEventWithHttpMethod(String httpMethod) {
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();

		APIGatewayV2HTTPEvent.RequestContext requestContext = new APIGatewayV2HTTPEvent.RequestContext();
		APIGatewayV2HTTPEvent.RequestContext.Http http = new APIGatewayV2HTTPEvent.RequestContext.Http();
		http.setMethod(httpMethod);
		requestContext.setHttp(http);
		event.setRequestContext(requestContext);

		return event;
	}

}