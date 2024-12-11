package artefact.mapper;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import artefact.dto.Operator;
import artefact.dto.output.BatchOutput;
import artefact.util.ScannedAppType;

import java.util.Map;
import java.util.function.Function;

import static artefact.util.AppConstants.KEY_LOCKED;


public class AttributeValueDB2ToBatchMapper
        implements Function<Map<String, AttributeValue>, BatchOutput> {
    /**
     * Applies this function to the given argument.
     *
     * @param stringAttributeValueMap the function argument
     * @return the function result
     */
    @Override
    public BatchOutput apply(Map<String, AttributeValue> stringAttributeValueMap) {

        String id = stringAttributeValueMap.get("batchSequence").getS();

        BatchOutput item = new BatchOutput().withId(id).withBatchSequence(id);

        if (stringAttributeValueMap.containsKey("batchStatus")) {
            item.setStatus(stringAttributeValueMap.get("batchStatus").getS());
        }

        if (stringAttributeValueMap.containsKey("operator")) {
            item.setOperator(new Operator().withUsername( stringAttributeValueMap.get("operator").getS()) );
        }

        if (stringAttributeValueMap.containsKey("requestType")) {
            item.setRequestType(stringAttributeValueMap.get("requestType").getS());
        }

        if (stringAttributeValueMap.containsKey("scanType")){
            item.setScanType(ScannedAppType.valueOf(stringAttributeValueMap.get("scanType").getS()));
        }

        boolean isLocked = stringAttributeValueMap.containsKey(KEY_LOCKED) ? stringAttributeValueMap.get(KEY_LOCKED).getBOOL() : false;
        item.setLocked(isLocked);


        return item;
    }


}
