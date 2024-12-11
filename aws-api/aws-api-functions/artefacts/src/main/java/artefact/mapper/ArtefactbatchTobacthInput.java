package artefact.mapper;

import artefact.dto.input.ArtefactInput;
import artefact.dto.input.ArtefactItemInput;
import artefact.dto.input.BatchInput;
import artefact.entity.ArtefactBatch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class ArtefactbatchTobacthInput implements Function<List<ArtefactBatch>, BatchInput> {

    @Override
    public BatchInput apply(List<ArtefactBatch> artefactBatchList) {

        ArtefactItemInput items =new ArtefactItemInput();

        ArtefactInput  artefact = new ArtefactInput();

        Set<BatchInput> batchSet = new HashSet<>();
        List<BatchInput> batchInputs = new ArrayList<BatchInput>();

        for (ArtefactBatch artefactBatch : artefactBatchList) {
            BatchInput batchInput = new BatchInput().withBatchSequence(artefactBatch.getBatchSequence());

            batchSet.add(batchInput);
        }

        for (BatchInput batchUnique : batchSet) {
            for (ArtefactBatch artefactBatch : artefactBatchList) {

                if(batchUnique.getBatchSequence() == artefactBatch.getBatchSequence()) {
                    // create a new line item


                }

            }
        }




        return null;
    }
}
