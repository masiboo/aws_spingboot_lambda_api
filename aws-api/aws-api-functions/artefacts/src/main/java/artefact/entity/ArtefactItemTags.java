package artefact.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import artefact.util.DateUtils;

import java.time.ZonedDateTime;

public class ArtefactItemTags {
	
	private int id;
    private String value;
    private String key;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.DATETIME_FORMAT)
	private ZonedDateTime insertedDate;
    private String type;

	public ArtefactItemTags(){
		this.insertedDate = ZonedDateTime.now();
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public ZonedDateTime getInsertedDate() {
		return insertedDate;
	}
	public void setInsertedDate(ZonedDateTime insertedDate) {
		this.insertedDate = insertedDate;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
    
    

}
