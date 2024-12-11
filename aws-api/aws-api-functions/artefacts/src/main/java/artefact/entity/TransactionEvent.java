package artefact.entity;

import java.util.Date;
import java.util.List;

public class TransactionEvent {

    private String transactionId;
    private Date insertedDate;
    private String userId;
    private String contentType;
    private String status;
    private List<IArtefact> Artefacts;

}
