
package artefact.dto;
import java.io.Serializable;

public class BatchEventDetail implements Serializable
{

    private String batchSequence;
    private Batch batch;
    private String type;
    private String eventType;
    private String eventId;
    private String status;
    private final static long serialVersionUID = -3098651461034555827L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public BatchEventDetail() {
    }

    /**
     * 
     * @param eventId
     * @param batch
     * @param eventType
     * @param batchSequence
     * @param type
     * @param status
     */
    public BatchEventDetail(String batchSequence, Batch batch, String type, String eventType, String eventId, String status) {
        super();
        this.batchSequence = batchSequence;
        this.batch = batch;
        this.type = type;
        this.eventType = eventType;
        this.eventId = eventId;
        this.status = status;
    }

    public String getBatchSequence() {
        return batchSequence;
    }

    public void setBatchSequence(String batchSequence) {
        this.batchSequence = batchSequence;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
