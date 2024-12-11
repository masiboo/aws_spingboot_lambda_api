package artefact.usecase;

import java.util.Map;

import static artefact.util.AppConstants.METADATA_KEY_BATCH_SEQ;
import static artefact.util.AppConstants.METADATA_KEY_MIRIS_DOCID;
import static software.amazon.awssdk.utils.StringUtils.isNotBlank;

public class MetadataValidator {
    public boolean isValid(Map<String,String> metadataMap){
        return isValidKeysPresent(metadataMap);
    }

    private boolean isValidKeysPresent(Map<String,String> metadataMap){
        return isNotBlank(metadataMap.get(METADATA_KEY_MIRIS_DOCID))
                || isNotBlank(metadataMap.get(METADATA_KEY_BATCH_SEQ));
    }
}
