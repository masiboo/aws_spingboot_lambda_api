package artefact.mapper;

import artefact.dto.output.ArtefactOutput;
import artefact.entity.Artefact;

import java.util.function.Function;

public class ArtefactToArtefactOutputMapper implements Function<Artefact, ArtefactOutput> {


    /**
     * Applies this function to the given argument.
     *
     * @param artefact the function argument
     * @return the function result
     */
    @Override
    public ArtefactOutput apply(Artefact artefact) {

        return new ArtefactOutput()
                .withId(artefact.getId())
                .withArtefactClassType(artefact.getArtefactClassType())
                .withStatus(artefact.getStatus())
                .withArtefactName(artefact.getArtefactName());
    }

    /**
     * Returns a composed function that first applies the {@code before}
     * function to its input, and then applies this function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param before the function to apply before this function is applied
     * @return a composed function that first applies the {@code before}
     * function and then applies this function
     * @throws NullPointerException if before is null
     * @see #andThen(Function)
     */
    @Override
    public <V> Function<V, ArtefactOutput> compose(Function<? super V, ? extends Artefact> before) {
        return Function.super.compose(before);
    }

    /**
     * Returns a composed function that first applies this function to
     * its input, and then applies the {@code after} function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then
     * applies the {@code after} function
     * @throws NullPointerException if after is null
     * @see #compose(Function)
     */
    @Override
    public <V> Function<Artefact, V> andThen(Function<? super ArtefactOutput, ? extends V> after) {
        return Function.super.andThen(after);
    }
}
