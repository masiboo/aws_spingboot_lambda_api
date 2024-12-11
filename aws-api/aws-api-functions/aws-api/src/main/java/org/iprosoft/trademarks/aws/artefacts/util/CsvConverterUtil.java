package org.iprosoft.trademarks.aws.artefacts.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchInputDynamoDb;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactBatch;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ScannedAppType;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public final class CsvConverterUtil {

	public static <T> List<T> convertCSVClass(String input, Class<T> type) throws IOException {
		CsvMapper csvMapper = new CsvMapper();
		CsvSchema schema = CsvSchema.emptySchema().withHeader().withLineSeparator("\n").withColumnSeparator(',');

		csvMapper.enable(com.fasterxml.jackson.dataformat.csv.CsvParser.Feature.TRIM_SPACES);
		csvMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		ObjectReader oReader = csvMapper.readerFor(type).with(schema);

		try (StringReader reader = new StringReader(input)) {
			MappingIterator<T> mappingIterator = oReader.readValues(reader);
			return mappingIterator.readAll();
		}
		catch (IOException e) {
			throw new IOException("Error occurred while reading CSV data", e);
		}
	}

	public static String convertToCSV(List<Map<String, Object>> listOfMap) throws IOException {

		CsvSchema schema = null;
		CsvSchema.Builder schemaBuilder = CsvSchema.builder();
		if (listOfMap != null && !listOfMap.isEmpty()) {
			for (String col : listOfMap.get(0).keySet()) {
				schemaBuilder.addColumn(col);
			}
			schema = schemaBuilder.build().withLineSeparator(System.lineSeparator()).withHeader();
		}

		CsvMapper csvMapper = new CsvMapper();

		Writer writer = new StringWriter();
		csvMapper.writer(schema).writeValues(writer).writeAll(listOfMap);
		String output = writer.toString();
		writer.flush();
		writer.close();
		return output;
	}

	public static List<BatchInputDynamoDb> extractDistinctBatchSequences(String csvContent) {
		BufferedReader reader = new BufferedReader(new StringReader(csvContent));

		return reader.lines()
			.skip(1) // Skip header
			.map(line -> line.split(","))
			.filter(parts -> parts.length >= 11)
			.map(parts -> {
				ScannedAppType scanType;
				if (parts[0].equalsIgnoreCase("new") || (parts[0].equalsIgnoreCase("new_request"))) {
					scanType = ScannedAppType.NEW_REQUEST;
				}
				else if (parts[0].equalsIgnoreCase("Addendum")) {
					scanType = ScannedAppType.ADDENDUM;
				}
				else {
					throw new IllegalArgumentException("Unknown type " + parts[0]);
				}
				BatchInputDynamoDb batch = new BatchInputDynamoDb();
				batch.setRequestType(parts[0]);
				batch.setScannedType(scanType);
				batch.setBatchSequence(parts[2]);
				batch.setCreationDate(DateUtils.getCurrentDatetimeUtc());
				return batch;
			})
			.collect(Collectors.groupingBy(BatchInputDynamoDb::getBatchSequence))
			.values()
			.stream()
			.map(list -> list.get(0))
			.collect(Collectors.toList());
	}

	public static List<ArtefactBatch> getArtefactBatchesForCsv(String jsonString) {
		List<ArtefactBatch> artefactBatchList;
		try {
			artefactBatchList = (List<ArtefactBatch>) (Object) CsvConverterUtil.convertCSVClass(jsonString,
					ArtefactBatch.class);
			artefactBatchList = addDateTime(artefactBatchList);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		return artefactBatchList;
	}

	public static List<ArtefactBatch> addDateTime(List<ArtefactBatch> artefactBatchList) {
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

}
