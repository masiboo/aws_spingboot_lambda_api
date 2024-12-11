package org.iprosoft.trademarks.aws.artefacts.service.artefactvalidation;

import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;

public interface ArtefactValidationService {

	boolean validateLength(Artefact a);

	boolean validateName(Artefact a);

	boolean validateSize(Artefact a);

	boolean validateType(Artefact a);

	boolean validateMaxNumber(Artefact a);

	boolean validateArtefact(Artefact a);

	boolean validateStatus(Artefact a);

}
