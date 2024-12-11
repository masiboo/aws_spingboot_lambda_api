package artefact.usecase;

import artefact.entity.ArtefactJob;

import java.util.List;
import java.util.Map;

public interface ArtefactJobServiceInterface {

    void saveJob(ArtefactJob job);

    void updateJob(ArtefactJob job);

    void updateJobWithStatus(String jobID, String status);

    ArtefactJob getJobStatus(String jobId);

    Map<String,Object> getAllJobStatusByRequestId(String requestId);

    List<ArtefactJob> getAllJobs(String date, String status);
}
