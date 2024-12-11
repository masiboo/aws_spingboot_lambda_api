package artefact.mapper;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import artefact.dto.output.ArtefactOutput;

import java.util.Map;
import java.util.function.Function;


public class AttributeValueDB2ToArtefactMapper
        implements Function<Map<String, AttributeValue>, ArtefactOutput> {
    /**
     * Applies this function to the given argument.
     *
     * @param stringAttributeValueMap the function argument
     * @return the function result
     */
    @Override
    public ArtefactOutput apply(Map<String, AttributeValue> stringAttributeValueMap) {

        String id = stringAttributeValueMap.get("artefactId").getS();

        ArtefactOutput item = new ArtefactOutput().withId(id);

        if (stringAttributeValueMap.containsKey("batchSequence")) {
            item.setBatchSequence(stringAttributeValueMap.get("batchSequence").getS());
        }

        return item;
    }
}
