package artefact.usecase;


import artefact.dto.input.BatchInputDynamoDb;
import artefact.dto.output.ArtefactOutput;
import artefact.dto.output.BatchOutput;

import java.util.List;

public interface BatchServiceInterface {


    void saveBatchSequence(BatchInputDynamoDb batch);

    void saveBatchSequenceWithChildren(BatchInputDynamoDb batch);

    void updateBatchSequence(BatchInputDynamoDb batch);


    void updateBatchWithStatus(String batchSequence, String status);

    BatchOutput getBatchDetail(String batchSequence);

//    Map<String,Object> getAllBatchByStatus(String status);


    List<BatchOutput> getAllArtefactsForBatch();

    List<ArtefactOutput> getAllArtefactsForBatch(String primary, String secondary);

    List<BatchOutput> getAllBatchByStatus(String status);

    void updateLockState(String batchSeq,boolean isLocked);
    String findStatusByRequestType(String batchSeq);
    void updateStatus(String batchSeq, String status);

}
