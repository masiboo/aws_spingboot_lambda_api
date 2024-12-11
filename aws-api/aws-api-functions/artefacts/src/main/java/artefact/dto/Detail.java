
package artefact.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Detail implements Serializable
{

    @SerializedName("version")
    @Expose
    private String version;
    @SerializedName("bucket")
    @Expose
    private Bucket bucket;
    @SerializedName("object")
    @Expose
    private Object object;
    @SerializedName("request-id")
    @Expose
    private String requestId;
    @SerializedName("requester")
    @Expose
    private String requester;
    @SerializedName("source-ip-address")
    @Expose
    private String sourceIpAddress;
    @SerializedName("reason")
    @Expose
    private String reason;
    private final static long serialVersionUID = -2182960884441214969L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Detail() {
    }

    /**
     * 
     * @param bucket
     * @param requester
     * @param reason
     * @param requestId
     * @param sourceIpAddress
     * @param version
     * @param object
     */
    public Detail(String version, Bucket bucket, Object object, String requestId, String requester, String sourceIpAddress, String reason) {
        super();
        this.version = version;
        this.bucket = bucket;
        this.object = object;
        this.requestId = requestId;
        this.requester = requester;
        this.sourceIpAddress = sourceIpAddress;
        this.reason = reason;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Bucket getBucket() {
        return bucket;
    }

    public void setBucket(Bucket bucket) {
        this.bucket = bucket;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequester() {
        return requester;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    public String getSourceIpAddress() {
        return sourceIpAddress;
    }

    public void setSourceIpAddress(String sourceIpAddress) {
        this.sourceIpAddress = sourceIpAddress;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Detail.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("version");
        sb.append('=');
        sb.append(((this.version == null)?"<null>":this.version));
        sb.append(',');
        sb.append("bucket");
        sb.append('=');
        sb.append(((this.bucket == null)?"<null>":this.bucket));
        sb.append(',');
        sb.append("object");
        sb.append('=');
        sb.append(((this.object == null)?"<null>":this.object));
        sb.append(',');
        sb.append("requestId");
        sb.append('=');
        sb.append(((this.requestId == null)?"<null>":this.requestId));
        sb.append(',');
        sb.append("requester");
        sb.append('=');
        sb.append(((this.requester == null)?"<null>":this.requester));
        sb.append(',');
        sb.append("sourceIpAddress");
        sb.append('=');
        sb.append(((this.sourceIpAddress == null)?"<null>":this.sourceIpAddress));
        sb.append(',');
        sb.append("reason");
        sb.append('=');
        sb.append(((this.reason == null)?"<null>":this.reason));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.bucket == null)? 0 :this.bucket.hashCode()));
        result = ((result* 31)+((this.requester == null)? 0 :this.requester.hashCode()));
        result = ((result* 31)+((this.reason == null)? 0 :this.reason.hashCode()));
        result = ((result* 31)+((this.requestId == null)? 0 :this.requestId.hashCode()));
        result = ((result* 31)+((this.sourceIpAddress == null)? 0 :this.sourceIpAddress.hashCode()));
        result = ((result* 31)+((this.version == null)? 0 :this.version.hashCode()));
        result = ((result* 31)+((this.object == null)? 0 :this.object.hashCode()));
        return result;
    }

    @Override
    public boolean equals(java.lang.Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Detail) == false) {
            return false;
        }
        Detail rhs = ((Detail) other);
        return ((((((((this.bucket == rhs.bucket)||((this.bucket!= null)&&this.bucket.equals(rhs.bucket)))&&((this.requester == rhs.requester)||((this.requester!= null)&&this.requester.equals(rhs.requester))))&&((this.reason == rhs.reason)||((this.reason!= null)&&this.reason.equals(rhs.reason))))&&((this.requestId == rhs.requestId)||((this.requestId!= null)&&this.requestId.equals(rhs.requestId))))&&((this.sourceIpAddress == rhs.sourceIpAddress)||((this.sourceIpAddress!= null)&&this.sourceIpAddress.equals(rhs.sourceIpAddress))))&&((this.version == rhs.version)||((this.version!= null)&&this.version.equals(rhs.version))))&&((this.object == rhs.object)||((this.object!= null)&&this.object.equals(rhs.object))));
    }

}
