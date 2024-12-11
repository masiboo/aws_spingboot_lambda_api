package artefact.dto.output;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArtefactInfo {

    String artefactId;
    String mediaType;
    String fileType;
    String bitDepth;
    String samplingFrequency;
    String resolutionInDpi;

    private Boolean sizeWarning;

    private String contentLength;

    public String getArtefactId() {
        return artefactId;
    }

    public void setArtefactId(String artefactId) {
        this.artefactId = artefactId;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getBitDepth() {
        return bitDepth;
    }

    public void setBitDepth(String bitDepth) {
        this.bitDepth = bitDepth;
    }

    public String getSamplingFrequency() {
        return samplingFrequency;
    }

    public void setSamplingFrequency(String samplingFrequency) {
        this.samplingFrequency = samplingFrequency;
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

    public String getResolutionInDpi() {
        return resolutionInDpi;
    }

    public void setResolutionInDpi(String resolutionInDpi) {
        this.resolutionInDpi = resolutionInDpi;
    }


}
