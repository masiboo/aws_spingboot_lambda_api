package artefact.util;

import artefact.aws.dynamodb.DocumentServiceImpl;

public class FileSizeValidator {

    private static final int BYTE_CONVERSION_FACTOR = (1024 * 1024);

    public String validate(Long contentLength, String classType) {
        String warningMessage = null;
        Double sizeLimitByType =  getSizeLimitByType(classType);
        double contentLengthInMB = (double) contentLength / BYTE_CONVERSION_FACTOR;

         //sizeLimit will be 0 except SOUND and MULTIMEDIA
        if (sizeLimitByType != 0 && contentLengthInMB > sizeLimitByType) {
            warningMessage = constructWarningMsg(sizeLimitByType.longValue());
        }
        return warningMessage;
    }

    private double getSizeLimitByType(String classType){
        double sizeLimitInMB = 0d;
        if(DocumentServiceImpl.classType.MULTIMEDIA.toString().equalsIgnoreCase(classType)){
            sizeLimitInMB = 20d;
        }else if (DocumentServiceImpl.classType.SOUND.toString().equalsIgnoreCase(classType)){
            sizeLimitInMB = 5d;
        }
        return sizeLimitInMB;
    }

    private String constructWarningMsg(Long artefactSizeLimit) {
        return String.format("ArtefactItem ContentLength should be less than or equal to %dMB", artefactSizeLimit);
    }

}
