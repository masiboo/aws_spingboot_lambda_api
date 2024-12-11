package artefact.mapper;

import artefact.entity.ArtefactJob;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.function.Function;

public class AttributeValueToArtefactJobMapper
        implements Function<Map<String, AttributeValue>, ArtefactJob> {


    /**
     * Applies this function to the given argument.
     *
     * @param map the function argument
     * @return the function result
     */
    @Override
    public ArtefactJob apply(Map<String, AttributeValue> map) {

        String id = map.get("jobId").s();
//        String userId = map.containsKey("userId") ? map.get("userId").s() : null;
//        Date insertedDate = this.toInsertedDate.apply(map);
//        Date lastModifiedDate = this.toModifiedDate.apply(map);
//        ArtefactJob item = new ArtefactJob(id, insertedDate, userId);

        ArtefactJob item = new ArtefactJob().withId(id);

//        item.setLastModifiedDate(lastModifiedDate != null ? lastModifiedDate : insertedDate);

        if (map.containsKey("artefactId")) {
            item.setArtefactId(map.get("artefactId").s());
        }

        if (map.containsKey("s3_signed_url")) {
            item.setS3SignedUrl(map.get("s3_signed_url").s());
        }

        if (map.containsKey("jobStatus")) {
            item.setStatus(map.get("jobStatus").s());
        }

        if (map.containsKey("batchSequence")) {
            item.setBatchSequence(map.get("batchSequence").s());
        }

        return item;

    }
}
