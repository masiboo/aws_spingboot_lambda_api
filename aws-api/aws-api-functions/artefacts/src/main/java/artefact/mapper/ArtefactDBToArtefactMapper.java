package artefact.mapper;

import artefact.dto.ArtefactItemDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import artefact.dto.ArtefactsDTO;
import artefact.entity.Artefact;
import artefact.entity.ArtefactBuilder;
import artefact.usecase.ArtefactDBAccess;
import artefact.usecase.ArtefactItemDBAccess;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Function;

import static artefact.util.AppConstants.DATETIME_FORMAT;

public class ArtefactDBToArtefactMapper implements Function<ArtefactsDTO, Artefact> {

    private static final Logger logger = LoggerFactory.getLogger(AttributeValueToArtefactMapper.class);
    private DateTimeFormatter yyyymmddFormatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);

    @Override
    public Artefact apply(ArtefactsDTO artefactDBAccess) {

       ArtefactItemDTO artefactItemDBAccess = artefactDBAccess.getArtefactItems().get(0);

        Artefact artefact = new ArtefactBuilder()
                .setId(String.valueOf(artefactDBAccess.getId()))
                .setArtefactClassType(artefactDBAccess.getArtefactClass())
                .createArtefact();

        artefact.setArtefactName(artefactDBAccess.getArtefactName());
        artefact.setS3Bucket(artefactDBAccess.getS3Bucket());
        artefact.setS3Key(artefactItemDBAccess.getS3Key());
        artefact.setStatus (artefactDBAccess.getStatus());
        artefact.setMirisDocId (artefactDBAccess.getMirisDocId());
        artefact.setInsertedDate(artefactDBAccess.getIndexationDate().format(yyyymmddFormatter));
        artefact.setContentLength(String.valueOf(artefactItemDBAccess.getContentLength()));
        artefact.setSizeWarning(false);

        return artefact;
    }


}
