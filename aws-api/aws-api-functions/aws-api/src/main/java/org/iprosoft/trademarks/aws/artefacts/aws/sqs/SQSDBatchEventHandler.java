package org.iprosoft.trademarks.aws.artefacts.aws.sqs;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.aws.audit.AuditEventService;
import org.iprosoft.trademarks.aws.artefacts.model.dto.DDBEventDetail;
import org.iprosoft.trademarks.aws.artefacts.model.dto.DynamoDBEventDTO;

import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class SQSDBatchEventHandler implements Function<SQSEvent, String> {

	private final static String audit_evnt_type = "SQSDBatchEventHandler";

	private final ObjectMapper objectMapper;

	private final AuditEventService auditEventService;

	public String apply(SQSEvent sqsEvent) {

		try {

			for (SQSEvent.SQSMessage message : sqsEvent.getRecords()) {

				String messageBody = message.getBody();
				log.info("Sqs MESSAGE BODY: {}", messageBody);

				DynamoDBEventDTO sqsDDBEventData = objectMapper.readValue(messageBody, DynamoDBEventDTO.class);
				DDBEventDetail ddbEventDetail = sqsDDBEventData.getDetail();
				log.info("Sqs ddbEventDetail BODY: {}", ddbEventDetail);

				auditEventService.saveAuditEvent(audit_evnt_type, messageBody, sqsDDBEventData.getId());
			}
		}
		catch (JsonProcessingException e) {
			log.error("Unable to parse the SQSEvent details ", e);
			throw new RuntimeException("Artefact Creation Failed", e);
		}

		return "";
	}

}
