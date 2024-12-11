package artefact.util;

public enum ArtefactStatus {
    INDEXED("INDEXED"),
    DELETED("DELETED"),

    INSERTED("INSERTED"),
    UPLOADED("UPLOADED"),
    ERROR("ERROR"),

    INIT("INIT");

    private String status;

    ArtefactStatus(String status){
        this.status = status;
    }

    public String getStatus(){
        return this.status;
    }

}

