package artefact.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import artefact.entity.ArtefactDynamoDb;
import artefact.entity.IArtefact;

import static artefact.util.AppConstants.DATETIME_FORMAT;

/** {@link Gson} utils. */
public final class GsonUtil {

    /**
     * {@link Gson}.
     */
    private static final Gson GSON =
            new GsonBuilder().disableHtmlEscaping().setDateFormat(DATETIME_FORMAT).registerTypeAdapter(
                    IArtefact.class, new InterfaceSerializer<>(ArtefactDynamoDb.class)).create();

  /** private constructor. */
  private GsonUtil() {}

  /**
   * Get Instance of {@link Gson}.
   *
   * @return {@link Gson}
   */
  public static Gson getInstance() {
    return GSON;
  }
}
