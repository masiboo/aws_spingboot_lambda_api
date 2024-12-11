package org.wipo.trademarks.aws.moss.artefact.entrypoints;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import artefact.entity.ArtefactBatch;
import artefact.util.GsonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static artefact.entrypoints.ArtefactBatchUploadURLRequestHandler.getArtefactBatchesForJson;

class ArtefactBatchUploadURLRequestHandlerTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    public String toRequestEvent(final String filename) throws IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStream in = classloader.getResourceAsStream(filename)) {
            return GsonUtil.getInstance().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8),
                    String.class);
        }
    }


    @Test
    void getArtefactBatchesForCsvTest() {


    }

    @Test
    void getArtefactBatchesForJsonTest() throws IOException {

        List<ArtefactBatch> artefactBatchList = new ArrayList<ArtefactBatch>();

        String jsonString = "[\r\n  {\r\n    \"type\": \"Addendum\",\r\n  " +
                "\"filename\": \"00000000.TIF\",\r\n   " +
                "\"artefactItemFileName\": \"77889924.051-00000000.TIF\",\r\n   " +
                " \"batchSequence\": \"77889924.051\",\r\n   " +
                " \"artefactName\": \"77889924.051-0000D\",\r\n   " +
                " \"artefactClassType\": \"DOCUMENT\",\r\n\r\n   " +
                " \"creationDate\": \"20221123\",\r\n  " +
                "  \"requestType\": \"BC1.1\",\r\n\r\n   " +
                " \"path\": \"77889924.051\",\r\n   " +
                " \"contentType\": \"TIFF\",\r\n   " +
                " \"user\": \"GregOr\",\r\n   " +
                " \"mirisDocId\": \"16532350\",\r\n  " +
                "  \"page\": 1\r\n  }\r\n]";
        artefactBatchList = getArtefactBatchesForJson(jsonString);

        ArtefactBatch batch = artefactBatchList.get(0);

        assertEquals("16532350", String.valueOf(artefactBatchList.get(0).getMirisDocId()));
        assertEquals("77889924.051-0000D", String.valueOf(artefactBatchList.get(0).getArtefactMergeId()));

    }
}