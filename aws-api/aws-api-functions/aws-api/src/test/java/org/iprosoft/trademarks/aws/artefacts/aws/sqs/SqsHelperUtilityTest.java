package org.iprosoft.trademarks.aws.artefacts.aws.sqs;

import org.junit.jupiter.api.Test;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactBatch;
import org.iprosoft.trademarks.aws.artefacts.util.SqsHelperUtility;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqsHelperUtilityTest {

	@Test
	public void testIsMergeEligibleArtefact_isTrue() {

		List<ArtefactBatch> listItems = new ArrayList<>();

		ArtefactBatch item = new ArtefactBatch();
		item.setFilename("00000001.TIF");
		listItems.add(item);

		ArtefactBatch item2 = new ArtefactBatch();
		item2.setFilename("00000002.TIF");
		listItems.add(item2);

		assertTrue(SqsHelperUtility.isMergeEligibleArtefact(listItems));

	}

	@Test
	public void testIsMergeEligibleArtefact_isEmpty() {

		List<ArtefactBatch> listItems = new ArrayList<>();
		assertFalse(SqsHelperUtility.isMergeEligibleArtefact(listItems));

	}

	@Test
	public void testIsMergeEligibleArtefact_isFalse() {

		List<ArtefactBatch> listItems = new ArrayList<>();

		ArtefactBatch item = new ArtefactBatch();
		item.setFilename("00000001.TIF");
		listItems.add(item);

		ArtefactBatch item2 = new ArtefactBatch();
		item2.setFilename("00000002.TIF");
		listItems.add(item2);

		ArtefactBatch item3 = new ArtefactBatch();
		item3.setFilename("00000003.pdf");
		listItems.add(item3);

		assertFalse(SqsHelperUtility.isMergeEligibleArtefact(listItems));

	}

}