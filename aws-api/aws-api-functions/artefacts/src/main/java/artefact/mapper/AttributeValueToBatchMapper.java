package artefact.mapper;

import artefact.dto.Operator;
import artefact.dto.output.BatchOutput;
import artefact.util.ScannedAppType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.function.Function;

public class AttributeValueToBatchMapper
        implements Function<Map<String, AttributeValue>, BatchOutput> {


    /**
     * Applies this function to the given argument.
     *
     * @param map the function argument
     * @return the function result
     */
    @Override
    public BatchOutput apply(Map<String, AttributeValue> map) {

        String id = map.get("batchSequence").s();

        BatchOutput item = new BatchOutput().withId(id).withBatchSequence(id);

        if (map.containsKey("batchStatus")) {
            item.setStatus(map.get("batchStatus").s());
        }

        if (map.containsKey("requestType")) {
            item.setRequestType(map.get("requestType").s());
        }

        if (map.containsKey("operator")) {
            item.setOperator(new Operator().withUsername( map.get("operator").s()) );
        }

        boolean isLocked = map.containsKey("locked") ? map.get("locked").bool() : false;
        item.setLocked(isLocked);


        if (map.containsKey("scanType")) {
            item.setScanType(ScannedAppType.valueOf(map.get("scanType").s()));
        }


        return item;

    }
}
