package artefact.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import artefact.util.DateUtils;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Artefact {
	
	private String id;  
	 
	@NotNull(message = "Name cannot be null")
	protected String artefactName;
	@NotNull(message = "ClassType cannot be null")
	private String artefactClassType;
//	@NotNull(message = "Status cannot be null")
	private String status;
	private String error;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.DATETIME_FORMAT)
	private ZonedDateTime indexationDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.DATETIME_FORMAT)
	private ZonedDateTime archiveDate;

	public String getInsertedDate() {
		return insertedDate;
	}

	public void setInsertedDate(String insertedDate) {
		this.insertedDate = insertedDate;
	}

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
	private String insertedDate;

	private String s3Bucket;
	private String s3Key;

	private List<ArtefactItemTags> artefactItemTags;
	private String mirisDocId;

	private Boolean sizeWarning;

	private String contentLength;



    @JsonProperty("items")
    private List<Items> items = null;

	public Artefact() {
		this.indexationDate = ZonedDateTime.now();
	}
	
	public Artefact(String id, String artefactName,
					String artefactClassType, String status, String error, ZonedDateTime indexationDate, ZonedDateTime archiveDate,
					String insertedDate, String s3Bucket, String s3Key, List<ArtefactItemTags> artefactItemTags, List<Items> items) {
					super();

		this.id = id;
		this.artefactName = artefactName;
		this.artefactClassType = artefactClassType;
		this.status = status;
		this.error = error;
		this.indexationDate = indexationDate;
		this.archiveDate = archiveDate;
		this.insertedDate = insertedDate;
		this.s3Bucket = s3Bucket;
		this.s3Key = s3Key;
		this.artefactItemTags = artefactItemTags;
		this.items = items;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getArtefactName() {
		return artefactName;
	}
	public void setArtefactName(String artefactName) {
		this.artefactName = artefactName;
	}
	public String getArtefactClassType() {
		return artefactClassType;
	}
	public void setArtefactClassType(String artefactClassType) {
		this.artefactClassType = artefactClassType;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public ZonedDateTime getIndexationDate() {
		return indexationDate;
	}
	public void setIndexationDate(ZonedDateTime indexationDate) {
		this.indexationDate = indexationDate;
	}
	public ZonedDateTime getArchiveDate() {
		return archiveDate;
	}
	public void setArchiveDate(ZonedDateTime archiveDate) {
		this.archiveDate = archiveDate;
	}
	public String gets3Bucket() {
		return s3Bucket;
	}
	public void setS3Bucket(String s3Bucket) {
		this.s3Bucket = s3Bucket;
	}
	public List<ArtefactItemTags> getArtefactItemTags() {
		return artefactItemTags;
	}
	public void setArtefactItemTags(List<ArtefactItemTags> artefactItemTags) {
		this.artefactItemTags = artefactItemTags;
	}

    @JsonProperty("items")
    public List<Items> getItems() {
        return items;
    }

    @JsonProperty("items")
    public void setItems(List<Items> items) {
        this.items = items;
    }

	public String getMirisDocId() {
		return mirisDocId;
	}

	public void setMirisDocId(String mirisDocId) {
		this.mirisDocId = mirisDocId;
	}

	public String getS3Key() {
		return s3Key;
	}

	public void setS3Key(String s3Key) {
		this.s3Key = s3Key;
	}

	public Boolean getSizeWarning() {
		return sizeWarning;
	}

	public void setSizeWarning(Boolean sizeWarning) {
		this.sizeWarning = sizeWarning;
	}

	public String getContentLength() {
		return contentLength;
	}

	public void setContentLength(String contentLength) {
		this.contentLength = contentLength;
	}

	@Override
	public String toString() {
		return "Artefact{" +
				"id='" + id + '\'' +
				", artefactName='" + artefactName + '\'' +
				", artefactClassType='" + artefactClassType + '\'' +
				", status='" + status + '\'' +
				", error='" + error + '\'' +
				", indexationDate=" + indexationDate +
				", archiveDate=" + archiveDate +
				", insertedDate='" + insertedDate + '\'' +
				", s3Bucket='" + s3Bucket + '\'' +
				", s3Key='" + s3Key + '\'' +
				", artefactItemTags=" + artefactItemTags +
				", mirisDocId='" + mirisDocId + '\'' +
				", sizeWarning=" + sizeWarning +
				", contentLength='" + contentLength + '\'' +
				", items=" + items +
				'}';
	}
}
