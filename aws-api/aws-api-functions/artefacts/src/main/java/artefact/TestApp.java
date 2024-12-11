package artefact;

import artefact.apigateway.ApiResponseStatus;
import artefact.dto.ArtefactsDTO;
import artefact.entity.Artefact;
import artefact.mapper.ArtefactDBToArtefactMapper;
import artefact.util.DebugInfoUtil;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static artefact.apigateway.ApiResponseStatus.SC_ERROR;
import static artefact.apigateway.ApiResponseStatus.SC_NOT_FOUND;

public class TestApp {
    public static void main(String[] args) {
        if("404".equalsIgnoreCase(Integer.toString(SC_NOT_FOUND.getStatusCode())) || "500".equalsIgnoreCase(Integer.toString(SC_NOT_FOUND.getStatusCode()))){
            System.out.println("ok");
        }

        List<Artefact> artefacts = new ArrayList<>();
        List<ArtefactsDTO> results = new ArrayList<>();

        // Create mock ArtefactsDTO object based on the given data
        ArtefactsDTO artefactDTO = new ArtefactsDTO();
        artefactDTO.setMirisDocId("1784147");
        artefactDTO.setArtefactClass("BWLOGO");
        artefactDTO.setIndexationDate(ZonedDateTime.parse("2024-10-24T06:15:35Z"));
        artefactDTO.setArtefactName(null);
        artefactDTO.setStatus("INDEXED");
        artefactDTO.setS3Bucket("Aws-registry-bucket-dev-eu-central-1-551493771163");
        artefactDTO.setArchiveDate(ZonedDateTime.parse("2024-10-24T06:15:49Z"));
        artefactDTO.setLastModificationDate(ZonedDateTime.parse("2024-10-24T06:15:51.302516Z"));
        artefactDTO.setLastModificationUser("system");
        artefactDTO.setDmapsVersion(null);
        artefactDTO.setImportedImapsError(null);
        artefactDTO.setImportedImapsDocId(null);
        artefactDTO.setArtefactUUID("2336a59f-c011-4e79-801a-76ae841f7af3");
        artefactDTO.setActiveArtefactItem(0);
        artefactDTO.setActiveJobId(null);
        artefactDTO.setLastError(null);
        artefactDTO.setErrorDate(null);
        artefactDTO.setBatch(null);
        artefactDTO.setArtefactItems(new ArrayList<>());
        artefactDTO.setArtefactTags(new ArrayList<>());
        artefactDTO.setArtefactNote(null);
        artefactDTO.setId(154172);
        // Add to the list
        results.add(artefactDTO);

        // Now you can run your code with this mocked list
       try {
           for (ArtefactsDTO result : results) {
               System.out.println(result.toString());  // Replace with logger if needed
               result = null;
               Artefact artefact = new ArtefactDBToArtefactMapper().apply(result);

               assert artefact != null;
               // Filter for indexed only
               if ("INDEXED".equals(artefact.getStatus()) &&
                       !"PART".equalsIgnoreCase(artefact.getArtefactClassType())) {
                   System.out.println("Eligible Artefact: " + artefact);
                   artefacts.add(artefact);
               }
           }
       }catch (Exception e){
           DebugInfoUtil.logExceptionDetails(e);
       }

    }
}