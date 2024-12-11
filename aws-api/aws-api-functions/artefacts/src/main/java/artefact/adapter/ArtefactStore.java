
package artefact.adapter;


import artefact.entity.Artefact;

public interface ArtefactStore {
	void addTags();
	void saveArtefactWithTags(Artefact artefact);
	String findTags(String tags);
    void postArtefact(Artefact artefact);
    Artefact getArtefactByDocId(String docid);

}
