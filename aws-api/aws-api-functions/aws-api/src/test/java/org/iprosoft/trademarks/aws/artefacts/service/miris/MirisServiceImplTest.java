package org.wipo.trademarks.Aws.artefacts.service.miris;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

// we have to set environment variable Aws_CORE_MIRIS_PROXY_API_URL = http://localhost:8080 or any valid root url
@SpringBootTest
@ExtendWith(MockitoExtension.class)
class MirisServiceImplTest {

	@MockBean
	HttpClient httpClient;

	@Autowired
	private MirisServiceImpl mirisServiceImpl;

	@BeforeAll
	static void setUp() {
		MockedStatic<HttpClient> mockedHttpClient = Mockito.mockStatic(HttpClient.class);
	}

	// we have to set environment variable Aws_CORE_MIRIS_PROXY_API_URL =
	// http://localhost:8080 or any valid root url
	@Test
	void testMirsDocIdLengthSuccess() throws IOException, InterruptedException {
		// Arrange
		String validMirsDocId = "12345678";
		HttpResponse<String> mockedResponse = mock(HttpResponse.class);
		when(HttpClient.newHttpClient()).thenReturn(httpClient);
		when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
			.thenReturn(mockedResponse);
		when(mockedResponse.body()).thenReturn("true");

		// Act
		boolean result = mirisServiceImpl.isDocIdValid(validMirsDocId);

		// Assert
		assertTrue(result);
	}

	// we have to set environment variable Aws_CORE_MIRIS_PROXY_API_URL =
	// http://localhost:8080/ or any valid root url
	@Test
	void testMirsDocIdLengthSmallerFail() throws IOException, InterruptedException {
		// Arrange
		String invalidMirsDocId = "123";
		HttpResponse<String> mockedResponse = mock(HttpResponse.class);
		when(HttpClient.newHttpClient()).thenReturn(httpClient);
		when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
			.thenReturn(mockedResponse);

		// Act
		boolean result = mirisServiceImpl.isDocIdValid(invalidMirsDocId);

		// Assert
		assertFalse(result);
	}

	// we have to set environment variable Aws_CORE_MIRIS_PROXY_API_URL =
	// http://localhost:8080/ or any valid root url
	@Test
	void testMirsDocIdLengthGraterFail() throws IOException, InterruptedException {
		// Arrange
		String invalidMirsDocId = "1234567890";
		HttpResponse<String> mockedResponse = mock(HttpResponse.class);
		when(HttpClient.newHttpClient()).thenReturn(httpClient);
		when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
			.thenReturn(mockedResponse);

		// Act
		boolean result = mirisServiceImpl.isDocIdValid(invalidMirsDocId);

		// Assert
		assertFalse(result);
	}

}