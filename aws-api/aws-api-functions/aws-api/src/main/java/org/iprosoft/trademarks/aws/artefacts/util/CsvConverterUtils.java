package org.iprosoft.trademarks.aws.artefacts.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

public final class CsvConverterUtils {

	public static List<Object> convertCSVClass(String input, Class<?> type) throws IOException {
		CsvMapper csvMapper = new CsvMapper();
		CsvSchema schema = CsvSchema.emptySchema().withHeader().withLineSeparator("\n").withColumnSeparator(',');

		csvMapper.enable(CsvParser.Feature.TRIM_SPACES);
		csvMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		ObjectReader oReader = csvMapper.readerFor(type).with(schema);

		StringReader reader = new StringReader(input);

		return oReader.readValues(reader).readAll();
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

}
