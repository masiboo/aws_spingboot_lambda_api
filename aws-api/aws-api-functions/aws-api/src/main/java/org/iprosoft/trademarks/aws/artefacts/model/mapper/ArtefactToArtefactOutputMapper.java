package org.iprosoft.trademarks.aws.artefacts.model.mapper;

import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactOutput;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;

import java.util.function.Function;

@Slf4j
public class ArtefactToArtefactOutputMapper implements Function<Artefact, ArtefactOutput> {

	/**
	 * Applies this function to the given argument.
	 * @param artefact the function argument
	 * @return the function result
	 */
	@Override
	public ArtefactOutput apply(Artefact artefact) {
		if (artefact == null) {
			throw new IllegalArgumentException("artefact cannot be null");
		}
		log.info("ArtefactToArtefactOutputMapper input map {}", artefact);

		String id = artefact.getId() != null ? artefact.getId() : "";
		String artefactClassType = artefact.getArtefactClassType() != null ? artefact.getArtefactClassType() : "";
		String status = artefact.getStatus() != null ? artefact.getStatus() : "";
		String artefactName = artefact.getArtefactName() != null ? artefact.getArtefactName() : "";
		log.info("id:{} artefactClassType: {} status: {} artefactName {}", id, artefactClassType, status, artefactName);

		return new ArtefactOutput().withId(id)
			.withArtefactClassType(artefactClassType)
			.withStatus(status)
			.withArtefactName(artefactName);
	}

	/**
	 * Returns a composed function that first applies the {@code before} function to its
	 * input, and then applies this function to the result. If evaluation of either
	 * function throws an exception, it is relayed to the caller of the composed function.
	 * @param before the function to apply before this function is applied
	 * @return a composed function that first applies the {@code before} function and then
	 * applies this function
	 * @throws NullPointerException if before is null
	 * @see #andThen(Function)
	 */
	@Override
	public <V> Function<V, ArtefactOutput> compose(Function<? super V, ? extends Artefact> before) {
		return Function.super.compose(before);
	}

	/**
	 * Returns a composed function that first applies this function to its input, and then
	 * applies the {@code after} function to the result. If evaluation of either function
	 * throws an exception, it is relayed to the caller of the composed function.
	 * @param after the function to apply after this function is applied
	 * @return a composed function that first applies this function and then applies the
	 * {@code after} function
	 * @throws NullPointerException if after is null
	 * @see #compose(Function)
	 */
	@Override
	public <V> Function<Artefact, V> andThen(Function<? super ArtefactOutput, ? extends V> after) {
		return Function.super.andThen(after);
	}

}
