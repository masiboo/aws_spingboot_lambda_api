package org.iprosoft.trademarks.aws.artefacts.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactInput;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchInputDynamoDb;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactBatch;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ScannedAppType;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class JsonConverterUtil {

	public static List<BatchInputDynamoDb> extractDistinctBatchSequences(List<ArtefactBatch> artefactBatchList) {
		return artefactBatchList.stream().filter(Objects::nonNull).map(artefactBatch -> {
			ScannedAppType scanType;
			if (artefactBatch.getType() == null) {
				throw new IllegalArgumentException("Batch type is null");
			}
			if (artefactBatch.getType().equalsIgnoreCase("new")
					|| artefactBatch.getType().equalsIgnoreCase("new_request")) {
				scanType = ScannedAppType.NEW_REQUEST;
			}
			else if (artefactBatch.getType().equalsIgnoreCase("Addendum")) {
				scanType = ScannedAppType.ADDENDUM;
			}
			else {
				throw new IllegalArgumentException("Unknown type " + artefactBatch.getType());
			}
			BatchInputDynamoDb batch = new BatchInputDynamoDb();
			batch.setRequestType(artefactBatch.getType());
			batch.setScannedType(scanType);
			batch.setBatchSequence(artefactBatch.getBatchSequence());
			batch.setCreationDate(DateUtils.getCurrentDatetimeUtc());
			return batch;
		})
			.collect(Collectors.groupingBy(BatchInputDynamoDb::getBatchSequence))
			.values()
			.stream()
			.map(list -> list.get(0))
			.collect(Collectors.toList());
	}

	private static ObjectMapper getObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
		return objectMapper;
	}

	public static List<ArtefactBatch> getArtefactBatchesFromJson(String jsonString) {
		List<ArtefactBatch> artefactBatchList;
		TypeReference<List<ArtefactBatch>> jacksonTypeReference = new TypeReference<>() {
		};
		try {
			artefactBatchList = getObjectMapper().readValue(jsonString, jacksonTypeReference);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		if (artefactBatchList != null) {
			artefactBatchList.forEach(artefactBatch -> {
				if (artefactBatch.getCreationDate() == null) {
					artefactBatch.setCreationDate(DateUtils.getCurrentDatetimeUtcStr());
				}
				if (artefactBatch.getIndexationDate() == null) {
					artefactBatch.setIndexationDate(DateUtils.getCurrentDatetimeUtc());
				}
				if (artefactBatch.getArchiveDate() == null) {
					artefactBatch.setArchiveDate(DateUtils.getCurrentDatetimeUtc());
				}
			});
		}
		return artefactBatchList;
	}

	public static ArtefactInput getArtefactInputFromJson(String jsonString) {
		ArtefactInput artefactInput;
		try {
			artefactInput = getObjectMapper().readValue(jsonString, ArtefactInput.class);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return artefactInput;
	}

	public static <T> List<T> getObjectListFromJson(String jsonString, Class<T> clazz) {
		List<T> objectList;
		try {
			ObjectMapper objectMapper = getObjectMapper();
			objectList = objectMapper.readValue(jsonString,
					objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return objectList;
	}

	public static <T> String getStringFromObject(T object) {
		try {
			ObjectMapper objectMapper = getObjectMapper();
			return objectMapper.writeValueAsString(object);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
