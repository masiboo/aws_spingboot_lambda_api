package org.wipo.trademarks.aws.moss.artefact.aws.dynamodb;

import artefact.aws.dynamodb.ArtefactJobServiceImpl;
import artefact.aws.dynamodb.DynamoDbConnectionBuilder;
import artefact.aws.dynamodb.DynamoDbPartiQ;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import artefact.entity.ArtefactJob;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ArtefactJobServiceIntefaceTest {

    public static final String REQUEST_ID = "requestId";
    private static final String REQ_ID ="123456";

    public static final String BATCH_SEQUENCE = "seq1";
    public static final String INIT = "INIT";
    public static final String BATCH_SEQUENCE_KEY = "batchSequence";
    public static final String BATCH_STATUS_KEY = "batchStatus";
    private static final String DELETED = "DELETED";
    private static final String INDEXED = "INDEXED";
    @Mock
    private DynamoDbClient dynamoDB;

    @Mock
    private DynamoDbConnectionBuilder dbConnectionBuilder;

    private static final String DOCUMENT_TABLENAME = "mocktablename";



    private ArtefactJobServiceImpl artefactJobService;

    @BeforeEach
    void setUp() {
        when(dbConnectionBuilder.build()).thenReturn(dynamoDB);
        artefactJobService = new ArtefactJobServiceImpl(dbConnectionBuilder,DOCUMENT_TABLENAME);
    }

    @Test
    public void getAllJobStatusByRequestIdTest_When_no_jobs_found(){

        try (MockedStatic<DynamoDbPartiQ> mocked = mockStatic(DynamoDbPartiQ.class)) {
            when(DynamoDbPartiQ.getAllArtefactJobsByRequestId(any(DynamoDbClient.class),anyString(),anyString())).thenReturn(null);
            assertNull(artefactJobService.getAllJobStatusByRequestId(REQ_ID));

            mocked.verify(() -> DynamoDbPartiQ.getAllArtefactJobsByRequestId(dynamoDB,DOCUMENT_TABLENAME,REQ_ID));
        }
    }

    @Test
    public void getAllJobStatusByRequestIdTest_When_there_Jobs_found(){
        ArtefactJob job1 = new ArtefactJob().withId("j1")
                .withArtefactId("a1")
                .withRequestId(REQ_ID)
                .withBatchSequence(BATCH_SEQUENCE)
                .withStatus(INIT);

        ArtefactJob job2 = new ArtefactJob().withId("j1")
                .withArtefactId("a1")
                .withRequestId(REQ_ID)
                .withBatchSequence(BATCH_SEQUENCE)
                .withStatus(INIT);

        Map<String,Object> expectedJobStatusMap = new HashMap<>();
        expectedJobStatusMap.put(REQUEST_ID,REQ_ID);
        expectedJobStatusMap.put(BATCH_SEQUENCE_KEY,BATCH_SEQUENCE);
        expectedJobStatusMap.put(BATCH_STATUS_KEY,INIT);

        try (MockedStatic<DynamoDbPartiQ> mocked = mockStatic(DynamoDbPartiQ.class)) {

            Map<String, Object> allJobStatusByRequestId = mockAndFetchResponse(job1, job2);
            assertResponse(expectedJobStatusMap,allJobStatusByRequestId);

            //when one of artefact job status is DELETED then overall batch status should be DELETED
            job2.setStatus(DELETED);
            expectedJobStatusMap.put(BATCH_STATUS_KEY,DELETED);
            Map<String, Object> deletedJobStatusResponse = mockAndFetchResponse(job1, job2);
            assertResponse(expectedJobStatusMap,deletedJobStatusResponse);

            //when all of them are INDEXED then overall batch status should be INDEXED
            job1.setStatus(INDEXED);
            job2.setStatus(INDEXED);
            expectedJobStatusMap.put(BATCH_STATUS_KEY,INDEXED);
            Map<String, Object> indexedJobStatusResponse = mockAndFetchResponse(job1, job2);
            assertResponse(expectedJobStatusMap,indexedJobStatusResponse);


            mocked.verify(() -> DynamoDbPartiQ.getAllArtefactJobsByRequestId(dynamoDB,DOCUMENT_TABLENAME,REQ_ID),times(3));
        }
    }

    private Map<String, Object> mockAndFetchResponse(ArtefactJob...job2) {
        when(DynamoDbPartiQ.getAllArtefactJobsByRequestId(any(DynamoDbClient.class),anyString(),anyString())).thenReturn(List.of(job2));
        return artefactJobService.getAllJobStatusByRequestId(REQ_ID);
    }

    private static void assertResponse(Map<String, Object> expectedJobReportMap, Map<String, Object> actualJobReportMap) {
        assertNotNull(actualJobReportMap);
        assertEquals(expectedJobReportMap.get(REQUEST_ID), actualJobReportMap.get(REQUEST_ID));
        assertEquals(expectedJobReportMap.get(BATCH_SEQUENCE_KEY), actualJobReportMap.get(BATCH_SEQUENCE_KEY));
        assertEquals(expectedJobReportMap.get(BATCH_STATUS_KEY), actualJobReportMap.get(BATCH_STATUS_KEY));
    }


}
