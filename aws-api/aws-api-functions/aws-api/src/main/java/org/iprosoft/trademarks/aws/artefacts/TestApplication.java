package org.iprosoft.trademarks.aws.artefacts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.iprosoft.trademarks.aws.artefacts.util.JsonConverterUtil;
import org.springframework.http.HttpStatus;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestApplication {

	public static void main(String[] args) throws JsonProcessingException {

		List<Map<String, Object>> dataList = new ArrayList<>();

		// Iterate 600 times to generate unique data
		for (int i = 1; i <= 1001; i++) {
			Map<String, Object> data = new HashMap<>();
			data.put("artefactName", String.format("14082024.%03d-0000D", i));
			data.put("mirisDocId", String.format("%07d", 1236547 + i));
			data.put("type", "ADDENDUM");
			data.put("batchSequence", "14082024.145");
			data.put("creationDate", "20240814");
			data.put("path", String.format("14082024.%03d", i));
			data.put("filename", "00000001.tiff");
			data.put("artefactItemFileName", String.format("14082024.%03d-00000000.tiff", i));
			data.put("page", i);
			data.put("artefactClassType", "DOCUMENT");
			data.put("contentType", "TIFF");
			data.put("user", "xxxx");

			// Add the generated data to the list
			dataList.add(data);
		}

		// Use Jackson ObjectMapper to convert the list to a JSON string
		ObjectMapper objectMapper = new ObjectMapper();
		String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dataList);

		// Print the JSON string
		System.out.println(jsonString);

		String validation = "ERROR: filename=must be present, mirisDocId=mirisDocId must be between 5 to 8 digits and no space allowed";

		// Check if the validation message contains "mirisDocId"
		if (validation.contains("mirisDocId")) {
			// Use replaceAll to remove the specific mirisDocId part
			validation = validation
				.replaceAll("mirisDocId=mirisDocId must be between 5 to 8 digits and no space allowed,? ?", "");
		}

		System.out.println(validation);

		List<String> list = new ArrayList<>();
		list.add("06062024.1");
		list.add("06062024.2");
		list.add("06062024.3");

		String v = """
						[
						   "221122.1",
						   "221122.2",
						   "221122.3"
						]
				""";
		List<String> lll = JsonConverterUtil.getObjectListFromJson(v, String.class);
		System.out.println(lll);

		System.out.println(HttpStatus.INTERNAL_SERVER_ERROR.toString());
		System.out.println("stop");

		GetObjectResponse getObjectResponse = GetObjectResponse.builder()
			.contentLength(1024L) // Example content length
			.build();
		byte[] data2 = "Dummy data for S3 object".getBytes();
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data2);
		ResponseInputStream<GetObjectResponse> s3Object = new ResponseInputStream<>(getObjectResponse,
				byteArrayInputStream);
		System.out.println("Content Length: " + s3Object.response().contentLength());

		// Example of reading the input stream
		byte[] buffer = new byte[data2.length];
		try {
			s3Object.read(buffer);
			System.out.println("Data: " + new String(buffer));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			// Always close the stream
			try {
				s3Object.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
