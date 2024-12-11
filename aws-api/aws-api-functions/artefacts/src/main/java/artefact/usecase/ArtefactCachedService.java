package artefact.usecase;

import artefact.aws.AwsServiceCache;
import artefact.entity.Artefact;

import java.util.List;

public class ArtefactCachedService {

    public ArtefactCachedService() {

    }

    public List<Artefact> getArtefactbyMirisDocIdAndType(String mirisDocId, List<String> typeList, AwsServiceCache serviceCache) {

        ArtefactServiceInterface cachedDBservice = serviceCache.documentService();
        artefact.usecase.ArtefactServiceInterface dbDocumentService = serviceCache.dbDocumentService();

        // 1.
        List<Artefact> cachedArtefacts = cachedDBservice.getArtefactbyMirisDocIdAndType(mirisDocId, typeList);

        if (cachedArtefacts.isEmpty()){
            // 2.
            return dbDocumentService.getArtefactbyMirisDocIdAndType(mirisDocId, typeList);
        } else {

            return cachedArtefacts;
        }

    }

    public List<Artefact> getArtefactbyMirisDocId(String mirisDocId, AwsServiceCache serviceCache) {
        ArtefactServiceInterface cachedDBservice = serviceCache.documentService();
        ArtefactServiceInterface dbDocumentService = serviceCache.dbDocumentService();

        // 1.
        List<Artefact> cachedArtefacts = cachedDBservice.getArtefactbyMirisDocId(mirisDocId);

        if (cachedArtefacts.isEmpty()){
            //2.
            return dbDocumentService.getArtefactbyMirisDocId(mirisDocId);
        } else {

            return cachedArtefacts;
        }

    }

    public Artefact getArtefactById(String artefactId, AwsServiceCache serviceCache) {
        ArtefactServiceInterface cachedDBservice = serviceCache.documentService();
        ArtefactServiceInterface dbDocumentService = serviceCache.dbDocumentService();

        // 1.
        Artefact cachedArtefact = cachedDBservice.getArtefactById(artefactId);

        if (cachedArtefact == null){

            //2.
            return dbDocumentService.getArtefactById(artefactId);
        } else {

            return cachedArtefact;
        }

    }

}
