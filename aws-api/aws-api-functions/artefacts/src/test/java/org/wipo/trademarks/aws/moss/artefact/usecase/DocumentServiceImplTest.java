package org.wipo.trademarks.aws.moss.artefact.usecase;

import artefact.aws.dynamodb.DocumentServiceImpl;
import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import artefact.aws.dynamodb.DDBHelper;
import artefact.aws.dynamodb.DynamoDbConnectionBuilder;
import artefact.dto.ArtefactIndexDto;
import artefact.entity.Artefact;
import artefact.util.ArtefactStatus;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class DocumentServiceImplTest {

    @Mock
    private DynamoDbClient dynamoDB;

    @Mock
    private DynamoDbConnectionBuilder dbConnectionBuilder;

    private static final String DOCUMENT_TABLENAME = "mocktablename";

    private DocumentServiceImpl documentService;

    @BeforeEach
    void setUp() {
        when(dbConnectionBuilder.build()).thenReturn(dynamoDB);
        documentService = new DocumentServiceImpl(dbConnectionBuilder, DOCUMENT_TABLENAME);
    }


    @Test
    public void softDeleteArtefact_success() {
        Gson gson = new Gson();
        String mockIArtefactId = "mockId";
        GetItemResponse getItemResponse = mock(GetItemResponse.class);
        when(dynamoDB.getItem(any(GetItemRequest.class))).thenReturn(getItemResponse);
        when(getItemResponse.item()).thenReturn(Map.of("artefactId", AttributeValue.builder().s(mockIArtefactId).build(),
                "type", AttributeValue.builder().s("ARTEFACT#DOCUMENT").build(),
                "status", AttributeValue.builder().s("INDEXED").build(),
                "mirisDocId", AttributeValue.builder().s("mirisDocId").build()));

        //mocking db updates
        when(dynamoDB.updateItem(any(UpdateItemRequest.class))).thenReturn(mock(UpdateItemResponse.class));

        //when
        documentService.softDeleteArtefactById(mockIArtefactId);

        //then
        verify(dynamoDB,times(1)).getItem(any(GetItemRequest.class));
        verify(dynamoDB,atLeastOnce()).updateItem(any(UpdateItemRequest.class));
    }

    @Test
    public void testIndexArtefact() {
        String mockIArtefactId = "mockId";
        ArtefactIndexDto dto = new ArtefactIndexDto();
        dto.setMirisDocId("mockedMirisDocId");
        try (MockedStatic<DDBHelper> mocked = mockStatic(DDBHelper.class)) {

            documentService.indexArtefact(mockIArtefactId, dto, ArtefactStatus.INDEXED);

            mocked.verify(()-> DDBHelper.buildAttrValueUpdate(ArtefactStatus.INDEXED.getStatus(), AttributeAction.PUT),times(1));
            mocked.verify(()-> DDBHelper.buildAttrValueUpdate(dto.getMirisDocId(),AttributeAction.PUT),times(1));
            mocked.verify(()-> DDBHelper.updateAttributes(any(DynamoDbClient.class),anyString(),anyMap(),anyMap()),times(1));
        }
    }

    @Test
    void testHasFileWithSameDocId() {
        String mirisDocId = "12237";
        // verify the existing artefact
        Artefact artefact = new Artefact();
        artefact.setArtefactClassType(DocumentServiceImpl.classType.BWLOGO.name());
        artefact.setMirisDocId(mirisDocId);
        DocumentServiceImpl mockDocumentService = mock(DocumentServiceImpl.class);
        List<Artefact> existingArtefacts = List.of(artefact);
        when(mockDocumentService.getArtefactbyMirisDocId(mirisDocId)).thenReturn(existingArtefacts);
        List<Artefact> existingArtefactList = mockDocumentService.getArtefactbyMirisDocId(mirisDocId);
        Assertions.assertNotNull(existingArtefactList);

        Assertions.assertFalse(documentService.hasFileWithSameDocId(mirisDocId, DocumentServiceImpl.classType.COLOURLOGO.name()));

        // Get again deleted classType items from database, it should be deleted and return null.
        Mockito.reset(mockDocumentService);
        when(mockDocumentService.getArtefactbyMirisDocId(anyString())).thenReturn(null);
        List<Artefact> reCheckExistingArtefactList = mockDocumentService.getArtefactbyMirisDocId(mirisDocId);
        if( reCheckExistingArtefactList != null ){
            Artefact reCheckExistingArtefact = reCheckExistingArtefactList.stream().findFirst().get();
            Assertions.assertNull(reCheckExistingArtefact.getItems());
        }else{
            Assertions.assertFalse(false, "The item is not exist anymore");
        }
    }
}
