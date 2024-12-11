package artefact.usecase;

import artefact.entity.Artefact;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

public class ArtefactValidation implements ArtefactValidationInterface {

	int maxLength = 30;
	String regexName = "[A-Z][a-z]*";
	int maxSize = 1000;
	int maxNumber = 10;
	
	public enum ArtefactClassType{
		DOCUMENT,
		BWLOGO,
		COLORLOGO,
		SOUNDFILE,
		MULTIMEDIAFILE,
		CERTIFICATE,
		ADDON
	}
	

	public enum Status{
		INSERTED, INDEXED, DELETED;
	}
	
	@Override
	public boolean validateLength(Artefact a) {
		return false;
		//return Integer.parseInt(a.getItems().)<maxLength;
	}

	@Override
	public boolean validateName(Artefact a) {
		return a.getArtefactName().matches(regexName);
	}

	@Override
	public boolean validateSize(Artefact a) {
		return false;
		//return Integer.parseInt(a.getItems())<maxSize;
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
		//return a.getMaxNumber()<maxNumber;
	}

	public boolean validateArtefact(Artefact a) {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<Artefact>> violations = validator.validate(a);
		for (ConstraintViolation<Artefact> violation : violations) {
		    System.out.println(violation.getPropertyPath()+ " : "+violation.getMessage()); 
		    return false;
		}
		
		if(!validateType(a)) {
			return false;
		}
		
		if(!validateStatus(a)) {
			return false;
		}
		return true;
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
	
}
