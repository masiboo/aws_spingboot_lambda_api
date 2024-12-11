package org.iprosoft.trademarks.aws.artefacts.service.metadata;

import org.springframework.stereotype.Service;
import org.iprosoft.trademarks.aws.artefacts.util.AppConstants;

import java.util.Map;

import static software.amazon.awssdk.utils.StringUtils.isNotBlank;

@Service
public class MetadataServiceImpl implements MetadataService {

	@Override
	public boolean isMetadataValid(Map<String, String> metadataMap) {
		return isNotBlank(metadataMap.get(AppConstants.METADATA_KEY_MIRIS_DOCID))
				|| isNotBlank(metadataMap.get(AppConstants.METADATA_KEY_BATCH_SEQ));
	}

}
