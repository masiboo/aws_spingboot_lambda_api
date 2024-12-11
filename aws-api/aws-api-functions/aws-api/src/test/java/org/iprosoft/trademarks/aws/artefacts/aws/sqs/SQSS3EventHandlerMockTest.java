package org.iprosoft.trademarks.aws.artefacts.aws.sqs;

import org.iprosoft.trademarks.aws.artefacts.aws.s3.S3Service;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactInput;
import org.iprosoft.trademarks.aws.artefacts.model.dto.MultimediaFileResponse;
import org.iprosoft.trademarks.aws.artefacts.model.dto.S3ObjectMetadata;
import org.iprosoft.trademarks.aws.artefacts.model.dto.S3ObjectTags;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.service.mediaprocessing.MediaProcessingService;
import org.iprosoft.trademarks.aws.artefacts.util.AppConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.wipo.trademarks.Aws.artefacts.errorhandling.MetadataExtractionFailureException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class SQSS3EventHandlerMockTest {

	@MockBean
	private MediaProcessingService mediaProcessingService;

	@MockBean
	private S3Service s3Service;

	@MockBean
	private ArtefactService artefactService;

	@Autowired
	private SQSS3EventHandler sqss3EventHandler;

	@BeforeEach
	void setUp() {
	}

	@AfterEach
	void tearDown() {
	}

	@Test
	void updateArtefactAndTags() throws IOException, InterruptedException, MetadataExtractionFailureException {
		// Arrange
		S3ObjectMetadata s3ObjectMetadata = new S3ObjectMetadata();
		s3ObjectMetadata.setContentType("audio/wav");
		s3ObjectMetadata.setContentLength(9L);
		Map<String, String> metadata = new HashMap<>();
		metadata.put(AppConstants.METADATA_KEY_ARTEFACT_ID, "12345678");
		s3ObjectMetadata.setMetadata(metadata);
		Map<String, String> bucketDtlMap = Map.of("bucket", "unit-test-bucket", "key", "2023/123456/15.wav");

		MultimediaFileResponse mediaProcessResp = new MultimediaFileResponse();
		S3ObjectTags s3ObjectTags = new S3ObjectTags();
		s3ObjectTags.setBitDepth("16");
		s3ObjectTags.setFormat("Wav");
		s3ObjectTags.setSamplingFrequency("44100");
		s3ObjectTags.setTotalDuration("9");
		s3ObjectTags.setCodec("pcm_s16le");
		mediaProcessResp.setS3ObjectTags(s3ObjectTags);

		when(mediaProcessingService.insertMultimediaMetadata(anyMap())).thenReturn(mediaProcessResp);
		doNothing().when(s3Service).setObjectTag(anyString(), anyString(), anyMap());
		doNothing().when(artefactService).updateArtefact(anyString(), anyMap());

		// Act
		boolean result = sqss3EventHandler.updateArtefactAndTags(s3ObjectMetadata, bucketDtlMap,
				ArtefactInput.classType.SOUND.name());

		// Assert
		Assertions.assertTrue(result);
	}

}
