package org.iprosoft.trademarks.aws.artefacts.service.batch;

import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactOutput;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchInputDynamoDb;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchOutput;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface BatchService {

	void saveBatchSequence(BatchInputDynamoDb batch);

	void saveBatchSequenceWithChildren(BatchInputDynamoDb batch);

	void saveBatchSequenceAtomic(Collection<BatchInputDynamoDb> batch);

	void saveBatchSequenceWithChildrenAtomic(Collection<BatchInputDynamoDb> batch);

	// void updateBatchSequence(BatchInputDynamoDb batch);

	void updateBatchWithStatus(String batchSequence, String status);

	BatchOutput getBatchDetail(String batchSequence);

	// List<BatchOutput> getAllArtefactsForBatch();

	List<ArtefactOutput> getAllArtefactsForBatch(String primary, String secondary);

	List<BatchOutput> getAllBatchByStatus(String status);

	void updateLockState(String batchSeq, boolean isLocked);

	String findStatusByRequestType(String batchSeq);

	void updateStatus(String batchSeq, String status);

	void updateBatchIfAllIndexed(String batchSeq);

	void updateBatchIfAllInserted(String batchSeq);

	Map<String, Object> getAllBatchesForRequestId(String requestId);

	Map<String, Object> getPagedBatchesForRequestId(String requestId);

}
