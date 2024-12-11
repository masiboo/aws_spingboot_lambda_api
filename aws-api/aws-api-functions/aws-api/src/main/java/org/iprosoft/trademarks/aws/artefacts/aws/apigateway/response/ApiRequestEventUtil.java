package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.iprosoft.trademarks.aws.artefacts.util.GsonUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public interface ApiRequestEventUtil {

	Gson GSON = GsonUtil.getInstance();

	// static final JsonValidator VALIDATOR
	// = JsonSchemaFactory.byDefault().getValidator();
	// static final JsonNodeReader NODE_READER
	// = new JsonNodeReader();

	default Map fromBodyToMap(final Logger logger, final APIGatewayV2HTTPEvent event) throws BadException {
		return fromBodyToObject(logger, event, Map.class);
	}

	default <T> T fromBodyToObject(final Logger logger, final APIGatewayV2HTTPEvent event, final Class<T> classOfT)
			throws BadException {

		String body = event.getBody();
		if (body == null) {
			throw new BadException("request body is required");
		}

		byte[] data = event.getBody().getBytes(StandardCharsets.UTF_8);

		if (Boolean.TRUE.equals(event.getIsBase64Encoded())) {
			data = Base64.getDecoder().decode(body);
		}

		Reader reader = new InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.UTF_8);
		try {
			return GSON.fromJson(reader, classOfT);
		}
		catch (JsonSyntaxException e) {
			throw new BadException("invalid JSON body");
		}
		finally {
			try {
				reader.close();
			}
			catch (IOException e) {
				logger.error("Cannot close : " + e.getMessage());
			}
		}
	}

	// private JsonNode validateJsonInputBody(final String rawSchema,
	// final String rawData)
	// throws IOException
	// {
	// final ObjectNode ret = JsonNodeFactory.instance.objectNode();

	// final boolean invalidSchema = fillWithData(ret,"input", "input-invalid",
	// rawSchema);
	// final boolean invalidData = fillWithData(ret, "input2", "input-invalid",
	// rawData);

	// final JsonNode schemaNode = ret.remove("input");
	// final JsonNode data = ret.remove("input-invalid");

	// final ProcessingReport report
	// = VALIDATOR.validateUnchecked(schemaNode, data);

	// final boolean success = report.isSuccess();
	// ret.put("valid", success);
	// final JsonNode node = ((AsJson) report).asJson();
	// ret.put("results", JacksonUtils.prettyPrint(node));
	// return ret;
	// }

	// private boolean fillWithData(final ObjectNode node, final String onSuccess, final
	// String onFailure,
	// final String raw)
	// throws IOException
	// {
	// try {
	// node.put(onSuccess, NODE_READER.fromReader(new StringReader(raw)));
	// return false;
	// } catch (JsonProcessingException e) {
	// // node.put(onFailure, ParseError.build(e, raw.contains("\r\n")));
	// return true;
	// }
	// }

}
