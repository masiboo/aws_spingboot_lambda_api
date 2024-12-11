package org.iprosoft.trademarks.aws.artefacts.service.mediaprocessing;

import org.iprosoft.trademarks.aws.artefacts.errorhandling.MetadataExtractionFailureException;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ConvertImageRequest;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ConvertImageResponse;
import org.iprosoft.trademarks.aws.artefacts.model.dto.MergeFilesRequest;
import org.iprosoft.trademarks.aws.artefacts.model.dto.MultimediaFileResponse;

import java.util.Map;

public interface MediaProcessingService {

	MultimediaFileResponse insertMultimediaMetadata(Map<String, String> bucketDtlMap)
			throws MetadataExtractionFailureException, MetadataExtractionFailureException;

	String mergeFiles(MergeFilesRequest mergeFilesRequest);

	String convertGIFToJPG(ConvertImageRequest request);

	ConvertImageResponse convertResizeImageToTif(ConvertImageRequest request, String resolutionInDpi);

}
