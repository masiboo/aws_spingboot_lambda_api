package org.iprosoft.trademarks.aws.artefacts.aws.sqs;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactsDTO;
import org.iprosoft.trademarks.aws.artefacts.model.dto.DDBEventArtefact;
import org.iprosoft.trademarks.aws.artefacts.model.dto.DDBEventDetail;
import org.iprosoft.trademarks.aws.artefacts.model.dto.DynamoDBEventDTO;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactStatusEnum;
import org.iprosoft.trademarks.aws.artefacts.model.mapper.DDBEventArtefactToArtefactEntityMapper;
import org.iprosoft.trademarks.aws.artefacts.service.database.DatabaseService;

import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class SQSDBEventHandler implements Function<SQSEvent, String> {

	private final ObjectMapper objectMapper;

	private final DatabaseService databaseService;

	@Override
	public String apply(SQSEvent sqsEvent) {

		try {

			for (SQSEvent.SQSMessage message : sqsEvent.getRecords()) {

				String messageBody = message.getBody();
				log.info(messageBody);

				DynamoDBEventDTO sqsDDBEventData = objectMapper.readValue(messageBody, DynamoDBEventDTO.class);
				DDBEventDetail ddbEventDetail = sqsDDBEventData.getDetail();

				// ArtefactClassEnum artefactClassType = ArtefactClassTypeDetector
				// .detectByType(ddbEventDetail.getArtefactType());
				DDBEventArtefact eventArtefact = ddbEventDetail.getArtefact();

				ArtefactsDTO artefactsEntity = new DDBEventArtefactToArtefactEntityMapper().apply(eventArtefact);
				// artefactsEntity.setArtefactClass(artefactClassType);

				if (ArtefactStatusEnum.INDEXED.toString().equalsIgnoreCase(artefactsEntity.getStatus().toString())) {
					log.info("artefact creation requested {}", artefactsEntity);
					ArtefactsDTO savedArtefact = databaseService.createArtefact(artefactsEntity);
					log.info("artefact created successfully {}", savedArtefact);
				}

			}
		}
		catch (JsonProcessingException e) {
			log.error("Unable to parse the SQSEvent details ", e);
			throw new RuntimeException("Artefact Creation Failed", e);
		}
		return "";
	}

}
