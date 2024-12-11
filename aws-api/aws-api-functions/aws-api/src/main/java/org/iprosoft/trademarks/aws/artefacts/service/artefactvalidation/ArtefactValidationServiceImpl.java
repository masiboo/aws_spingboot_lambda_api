package org.iprosoft.trademarks.aws.artefacts.service.artefactvalidation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;

import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
public class ArtefactValidationServiceImpl implements ArtefactValidationService {

	@Override
	public boolean validateLength(Artefact a) {
		return false;
	}

	@Override
	public boolean validateName(Artefact a) {
		return a.getArtefactName().matches("[A-Z][a-z]*");
	}

	@Override
	public boolean validateSize(Artefact a) {
		return false;
	}

	@Override
	public boolean validateType(Artefact a) {
		for (ArtefactClassType type : ArtefactClassType.values()) {
			if (type.name().equalsIgnoreCase(a.getArtefactClassType())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean validateMaxNumber(Artefact a) {
		return false;
	}

	public boolean validateArtefact(Artefact a) {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<Artefact>> violations = validator.validate(a);
		for (ConstraintViolation<Artefact> violation : violations) {
			log.info(violation.getPropertyPath() + " : " + violation.getMessage());
			return false;
		}
		if (!validateType(a)) {
			return false;
		}
		return validateStatus(a);
	}

	@Override
	public boolean validateStatus(Artefact a) {
		for (Status status : Status.values()) {
			if (status.name().equalsIgnoreCase(a.getStatus())) {
				return true;
			}
		}
		return false;
	}

	public enum ArtefactClassType {

		DOCUMENT, BWLOGO, COLORLOGO, SOUNDFILE, MULTIMEDIAFILE, CERTIFICATE, ADDON

	}

	public enum Status {

		INSERTED, INDEXED, DELETED

	}

}
