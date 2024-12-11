package artefact.aws.dbaccess;

import artefact.dto.output.ArtefactInfo;
import artefact.entity.Artefact;
import artefact.entity.ArtefactTag;
import artefact.usecase.ArtefactReaderServiceInterface;

import java.util.Collection;
import java.util.List;

public class ArtefactServiceDbImpl implements ArtefactReaderServiceInterface {

    @Override
    public Artefact getArtefactByTags(Collection<ArtefactTag> tags) {
        return null;
    }

    @Override
    public List<Artefact> getAllArtefacts(String date, String status) {
        return null;
    }

    @Override
    public Artefact getArtefactById(String artefactId) {
        return null;
    }

    @Override
    public List<Artefact> getArtefactbyMirisDocId(String mirisDocId) {
        return null;
    }

    @Override
    public List<Artefact> getArtefactbyMirisDocIdAndType(String mirisDocId, List<String> typeList) {
        return null;
    }

    @Override
    public ArtefactInfo getArtefactInfoById(String artefactId) {
        return null;
    }
}
