package artefact.entity;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

public class ArtefactBuilder {
    private String id;
    private String artefactName;
    private String artefactClassType;
    private String status;
    private String error;
    private ZonedDateTime indexationDate;
    private ZonedDateTime archiveDate;

    private String insertedDate;
    private String s3Bucket;

    String s3Key;
    private List<ArtefactItemTags> artefactItemTags;
    private List<Items> items;

    public ArtefactBuilder(){
        this.indexationDate = ZonedDateTime.now();
    }

    public ArtefactBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public ArtefactBuilder setArtefactName(String artefactName) {
        this.artefactName = artefactName;
        return this;
    }

    public ArtefactBuilder setArtefactClassType(String artefactClassType) {
        this.artefactClassType = artefactClassType;
        return this;
    }

    public ArtefactBuilder setStatus(String status) {
        this.status = status;
        return this;
    }

    public ArtefactBuilder setError(String error) {
        this.error = error;
        return this;
    }

    public ArtefactBuilder setIndexationDate(ZonedDateTime indexationDate) {
        this.indexationDate = indexationDate;
        return this;
    }

    public ArtefactBuilder setArchiveDate(ZonedDateTime archiveDate) {
        this.archiveDate = archiveDate;
        return this;
    }

    public ArtefactBuilder setS3Bucket(String s3Bucket) {
        this.s3Bucket = s3Bucket;
        return this;
    }

    public ArtefactBuilder setArtefactItemTags(List<ArtefactItemTags> artefactItemTags) {
        this.artefactItemTags = artefactItemTags;
        return this;
    }

    public ArtefactBuilder setItems(List<Items> items) {
        this.items = items;
        return this;
    }

    public Artefact createArtefact() {

        return new Artefact(id, artefactName,
    			artefactClassType, status, error, indexationDate, archiveDate,
                insertedDate, s3Bucket, s3Key, artefactItemTags, items);
    }
}
