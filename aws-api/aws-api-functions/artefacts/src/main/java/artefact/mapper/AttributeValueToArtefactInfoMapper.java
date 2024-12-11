package artefact.mapper;

import artefact.dto.output.ArtefactInfo;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.function.Function;

public class AttributeValueToArtefactInfoMapper  implements Function<Map<String, AttributeValue>, ArtefactInfo> {
    @Override
    public ArtefactInfo apply(Map<String, AttributeValue> attributeValueMap) {

        ArtefactInfo info = new ArtefactInfo();
        info.setArtefactId(attributeValueMap.get("artefactId").s());
        if(attributeValueMap.get("fileType") != null){
            info.setFileType(attributeValueMap.get("fileType").s());
        }
        if(attributeValueMap.get("bitDepth") != null){
            info.setBitDepth(attributeValueMap.get("bitDepth").s());
        }

        if(attributeValueMap.get("mediaType") != null){
            info.setMediaType(attributeValueMap.get("mediaType").s());
        }
        if(attributeValueMap.get("samplingFrequency") != null){
            info.setSamplingFrequency(attributeValueMap.get("samplingFrequency").s());
        }
        if (attributeValueMap.containsKey("contentLength")) {
            info.setContentLength(attributeValueMap.get("contentLength").n());
        }

        if (attributeValueMap.containsKey("sizeWarning")) {
            info.setSizeWarning(attributeValueMap.get("sizeWarning").bool());
        }
        if (attributeValueMap.get("resolutionInDpi") != null) {
            info.setResolutionInDpi(attributeValueMap.get("resolutionInDpi").s());
        }
        return info;
    }
}
