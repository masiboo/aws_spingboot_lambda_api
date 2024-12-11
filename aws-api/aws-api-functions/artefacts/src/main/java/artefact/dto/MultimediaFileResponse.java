package artefact.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MultimediaFileResponse {
    private S3ObjectTags s3ObjectTags;
}