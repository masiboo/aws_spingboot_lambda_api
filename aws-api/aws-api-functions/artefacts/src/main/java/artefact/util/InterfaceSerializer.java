package artefact.util;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * {@link Gson} {@link JsonSerializer} for converting Interface to Object.
 * 
 *
 * @param <T> Type of {@link Class}
 */
public class InterfaceSerializer<T> implements JsonSerializer<T>, JsonDeserializer<T> {
  /** Interface {@link Class}. */
  private final Class<T> implementationClass;

  /**
   * constructor.
   * 
   * @param clazz {@link Class}
   */
  public InterfaceSerializer(final Class<T> clazz) {
    this.implementationClass = clazz;
  }

  static <T> InterfaceSerializer<T> interfaceSerializer(final Class<T> implementationClass) {
    return new InterfaceSerializer<>(implementationClass);
  }

  @Override
  public JsonElement serialize(final T value, final Type type,
      final JsonSerializationContext context) {
    final Type targetType = value != null ? value.getClass() : type;
    return context.serialize(value, targetType);
  }

  @Override
  public T deserialize(final JsonElement jsonElement, final Type typeOfT,
      final JsonDeserializationContext context) {
    return context.deserialize(jsonElement, this.implementationClass);
  }
}
