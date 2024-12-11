
package artefact.usecase;

import artefact.dto.ArtefactIndexDto;
import artefact.dto.input.ArtefactInput;
import artefact.dto.input.BatchInput;
import artefact.dto.output.ArtefactInfo;
import artefact.entity.Artefact;
import artefact.entity.ArtefactTag;
import artefact.entity.IArtefact;
import artefact.util.ArtefactStatus;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Services for Querying, Updating Documents. */
public interface ArtefactReaderServiceInterface {


  Artefact getArtefactByTags(Collection<ArtefactTag> tags);

  List<Artefact> getAllArtefacts(String date,String status);

  Artefact getArtefactById(String artefactId);

  List<Artefact> getArtefactbyMirisDocId(String mirisDocId);
  List<Artefact> getArtefactbyMirisDocIdAndType(String mirisDocId,List<String> typeList);

  ArtefactInfo getArtefactInfoById(String artefactId);


}
