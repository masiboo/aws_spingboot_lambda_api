package org.iprosoft.trademarks.aws.artefacts.service.artefactjob;

import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactJob;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ArtefactJobService {

	void saveJob(ArtefactJob job);

	void saveJobAtomic(Collection<ArtefactJob> job);

	void updateJob(ArtefactJob job);

	UpdateItemResponse updateJobWithStatus(String jobID, String status);

	ArtefactJob getJobStatus(String jobId);

	Map<String, Object> getAllJobStatusByRequestId(String requestId);

	Map<String, Object> getAllBatchStatusByRequestId(String requestId);

	Map<String, Object> getAllBulkJobStatusByRequestId(String requestId);

	List<ArtefactJob> getAllJobs(String date, String status);

}
