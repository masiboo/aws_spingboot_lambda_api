package artefact.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class S3ObjectTags {
    private String bitDepth;
    private String samplingFrequency;
    private String format;
    private String codec;
    private String frameRate;
    private String resolutionInDpi;
}