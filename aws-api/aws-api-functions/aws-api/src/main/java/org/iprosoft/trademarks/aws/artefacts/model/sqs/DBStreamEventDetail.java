package org.iprosoft.trademarks.aws.artefacts.model.sqs;

import lombok.Data;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactsEntity;

@Data
public class DBStreamEventDetail {

	private String artefactId;

	private String artefactType;

	private String eventType;

	private String eventId;

	private ArtefactsEntity artefact;

}
