package artefact.dto;

import java.io.Serializable;

public class ArtefactIndexDto implements Serializable {

    private String mirisDocId;

    public String getMirisDocId() {
        return mirisDocId;
    }

    public void setMirisDocId(String mirisDocId) {
        this.mirisDocId = mirisDocId;
    }
}
