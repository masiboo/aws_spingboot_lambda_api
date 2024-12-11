package org.wipo.trademarks.aws.Aws.artefact.usecase;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.wipo.trademarks.aws.Aws.artefact.util.AppConstants.METADATA_KEY_BATCH_SEQ;
import static org.wipo.trademarks.aws.Aws.artefact.util.AppConstants.METADATA_KEY_MIRIS_DOCID;

public class MetadataValidatorTest {

    private static MetadataValidator validator;

    @BeforeAll
    public static void setUp(){
        validator = new MetadataValidator();
    }
    @Test
    public void validateMetadata_when_docId_BatchSeq_Null(){
        Map<String,String> metadata = Map.of();
        assertFalse(validator.isValid(metadata));
    }

    @Test
    public void validateMetadata_when_docId_present_BatchSeq_Null(){
        Map<String,String> metadata = Map.of(METADATA_KEY_MIRIS_DOCID,"mockDocId");
        assertTrue(validator.isValid(metadata));
    }

    @Test
    public void validateMetadata_when_docId_present_BatchSeq_present(){
        Map<String,String> metadata = Map.of(METADATA_KEY_MIRIS_DOCID,"mockDocId",METADATA_KEY_BATCH_SEQ,"mockBatchId");
        assertTrue(validator.isValid(metadata));
    }




}
