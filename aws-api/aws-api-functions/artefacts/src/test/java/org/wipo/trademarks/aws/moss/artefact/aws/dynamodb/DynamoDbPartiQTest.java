package org.wipo.trademarks.aws.moss.artefact.aws.dynamodb;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import artefact.entity.Artefact;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
class DynamoDbPartiQTest {

    @Mock
    private DynamoDbClient dynamoDB;

    private String documentTableName = "mocktablename";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void executeStatementRequest() {
    }

    @Test
    void getAllArtefacts() {
        assertNotNull(dynamoDB);
        List<Artefact> artefacts = DynamoDbPartiQ.getAllArtefacts(dynamoDB, documentTableName, Map.of());
    }
}
