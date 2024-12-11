package artefact.usecase;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import artefact.dto.ArtefactsDTO;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DBAccessResponse {
    List<ArtefactsDTO> artefactDBAccesses;
}
