package artefact.dto;

import com.google.gson.annotations.SerializedName;

public class SQSBatchData {
    private String batchSequence;
    @SerializedName("batch")
    private SQSBatchChanges batchChanges;

    public String getBatchSequence() {
        return batchSequence;
    }

    public void setBatchSequence(String batchSequence) {
        this.batchSequence = batchSequence;
    }

    public SQSBatchChanges getBatchChanges() {
        return batchChanges;
    }

    public void setBatchChanges(SQSBatchChanges batchChanges) {
        this.batchChanges = batchChanges;
    }

}
