package org.iprosoft.trademarks.aws.artefacts.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactDynamoDb;
import org.iprosoft.trademarks.aws.artefacts.model.entity.IArtefact;
import org.iprosoft.trademarks.aws.artefacts.util.InterfaceSerializer;

/** {@link Gson} utils. */
public final class GsonUtil {

	/**
	 * {@link Gson}.
	 */
	private static final Gson GSON = new GsonBuilder().disableHtmlEscaping()
		.setDateFormat(DateUtils.DATETIME_FORMAT)
		.registerTypeAdapter(IArtefact.class, new InterfaceSerializer<>(ArtefactDynamoDb.class))
		.create();

	/** private constructor. */
	private GsonUtil() {
	}

	/**
	 * Get Instance of {@link Gson}.
	 * @return {@link Gson}
	 */
	public static Gson getInstance() {
		return GSON;
	}

}
