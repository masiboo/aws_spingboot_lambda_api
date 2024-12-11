package org.iprosoft.trademarks.aws.artefacts.client;

import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactsEntityDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EnabledIfSystemProperty(named = "test.integration", matches = "true")
public class ArtefactApiClientTest {

	private static final Logger logger = LoggerFactory.getLogger(ArtefactApiClientTest.class);

	private RestTemplate restTemplate;

	private ArtefactApiClient artefactApiClient;

	private static final String BASE_URL = System.getProperty("test.api.url", "http://localhost:8080");

	@BeforeEach
	public void setUp() {
		logger.info("Setting up ArtefactApiClientTest with BASE_URL: {}", BASE_URL);
		restTemplate = new RestTemplateBuilder().build();
		artefactApiClient = new ArtefactApiClient(restTemplate);
	}

	@Test
	public void testCreateArtefact() {
		logger.info("Running testCreateArtefact");
		ArtefactsEntityDTO artefact = createSampleArtefact("DOC123", "Sample Artefact", "INIT");

		try {
			ArtefactsEntityDTO createdArtefact = artefactApiClient.createArtefact(artefact);

			assertNotNull(createdArtefact, "Created artefact should not be null");
			assertNotNull(createdArtefact.getId(), "Created artefact should have an ID");
			assertEquals(artefact.getMirisDocId(), createdArtefact.getMirisDocId(), "MirisDocId should match");
			assertEquals(artefact.getArtefactClass(), createdArtefact.getArtefactClass(), "ArtefactClass should match");
			assertEquals(artefact.getArtefactName(), createdArtefact.getArtefactName(), "ArtefactName should match");
			assertEquals(artefact.getStatus(), createdArtefact.getStatus(), "Status should match");

			logger.info("Successfully created and verified artefact with ID: {}", createdArtefact.getId());
		}
		catch (Exception e) {
			logger.error("Error occurred while creating artefact", e);
			fail("Exception occurred: " + e.getMessage());
		}
	}

	@Test
	public void testCreateArtefactWithoutStatus() {
		logger.info("Running testCreateArtefactWithoutStatus");
		ArtefactsEntityDTO artefact = createSampleArtefact("DOC456", "Sample Artefact Without Status", null);

		try {
			ArtefactsEntityDTO createdArtefact = artefactApiClient.createArtefact(artefact);

			assertNotNull(createdArtefact, "Created artefact should not be null");
			assertNotNull(createdArtefact.getId(), "Created artefact should have an ID");
			assertEquals(artefact.getMirisDocId(), createdArtefact.getMirisDocId(), "MirisDocId should match");
			assertEquals(artefact.getArtefactClass(), createdArtefact.getArtefactClass(), "ArtefactClass should match");
			assertEquals(artefact.getArtefactName(), createdArtefact.getArtefactName(), "ArtefactName should match");
			assertNotNull(createdArtefact.getStatus(), "Status should not be null");

			logger.info("Successfully created and verified artefact without status, ID: {}", createdArtefact.getId());
		}
		catch (Exception e) {
			logger.error("Error occurred while creating artefact without status", e);
			fail("Exception occurred: " + e.getMessage());
		}
	}

	private ArtefactsEntityDTO createSampleArtefact(String mirisDocId, String artefactName, String status) {
		return ArtefactsEntityDTO.builder()
			.mirisDocId(mirisDocId)
			.artefactClass("DOCUMENT")
			.indexationDate(ZonedDateTime.now())
			.artefactName(artefactName)
			.status(status)
			.s3Bucket("sample-bucket")
			.archiveDate(ZonedDateTime.now())
			.lastModificationDate(ZonedDateTime.now())
			.lastModificationUser("testUser")
			.build();
	}

}
