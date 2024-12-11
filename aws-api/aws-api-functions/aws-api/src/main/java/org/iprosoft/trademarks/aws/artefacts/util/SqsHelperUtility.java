package org.iprosoft.trademarks.aws.artefacts.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactBatch;

import java.util.List;
import java.util.function.Predicate;

@Slf4j
public class SqsHelperUtility {

	public static boolean isMergeEligibleArtefact(List<ArtefactBatch> batchItems) {
		if (CollectionUtils.isEmpty(batchItems)) {
			return false;
		}

		Predicate<ArtefactBatch> isTiffArtefacts = artefact -> {
			String filename = artefact.getFilename();
			if (filename != null) {
				return filename.toLowerCase().endsWith(".tiff") || filename.toLowerCase().endsWith(".tif");
			}
			else {
				log.warn("filename is null for artefact {}", artefact);
				return false;
			}
		};

		try {
			return batchItems.stream().allMatch(isTiffArtefacts);
		}
		catch (Exception e) {
			log.error("Error checking isMergeEligibleArtefact : {}", e.getMessage());
			return false;
		}
	}

}