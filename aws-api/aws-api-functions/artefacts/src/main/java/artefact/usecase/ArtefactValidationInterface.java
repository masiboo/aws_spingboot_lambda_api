package artefact.usecase;

import artefact.entity.Artefact;

public interface ArtefactValidationInterface {
	 boolean validateLength(Artefact a) ;
	 boolean validateName(Artefact a);
	 boolean validateSize(Artefact a);
	 boolean validateType(Artefact a);
	 boolean validateMaxNumber(Artefact a);
	 boolean validateArtefact(Artefact a);
	 boolean validateStatus(Artefact a);
}
