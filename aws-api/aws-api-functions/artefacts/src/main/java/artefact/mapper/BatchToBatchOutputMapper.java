package artefact.mapper;

import artefact.dto.output.BatchOutput;

import java.util.function.Function;

public class BatchToBatchOutputMapper
        implements Function<BatchOutput, BatchOutput> {


    /**
     * Applies this function to the given argument.
     *
     * @param map the function argument
     * @return the function result
     */
    @Override
    public BatchOutput apply(BatchOutput map) {

        String id = map.getBatchSequence();
        BatchOutput item = new BatchOutput().withId(id).withBatchSequence(id);
//
//        if (map.containsKey("batchStatus")) {
//            item.setStatus(map.get("batchStatus").s());
//        }
//
//        if (map.containsKey("operator")) {
//            item.setOperator(new Operator().withUsername( map.get("operator").s()) );
//        }

        return item;

    }
}
