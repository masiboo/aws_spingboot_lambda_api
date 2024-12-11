package org.iprosoft.trademarks.aws.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.function.adapter.aws.FunctionInvoker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class FunctionInvokeHelper {

	public <T, R> R invoke(T t, Class<R> resultTypeClass) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			InputStream targetStream = new ByteArrayInputStream(objectMapper.writeValueAsBytes(t));
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			FunctionInvoker invoker = new FunctionInvoker();
			invoker.handleRequest(targetStream, output, null);
			return objectMapper.readValue(output.toByteArray(), resultTypeClass);
		}
		catch (IOException e) {
			log.error("Exception while invoking spring cloud functions", e);
			throw new RuntimeException(e);
		}
	}

}
