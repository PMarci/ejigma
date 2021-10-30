package ejigma.model.type;

import com.google.gson.*;
import ejigma.util.GsonExclude;

public interface Printable {

    Gson gson = getGsonBuilder().create();

    static GsonBuilder getGsonBuilder() {
        JsonSerializer<? extends ScramblerType<?, ?>> serializer = getSerializer();
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }

                    @Override
                    public boolean shouldSkipField(FieldAttributes field) {
                        return field.getAnnotation(GsonExclude.class) != null;
                    }
                })
                // TODO try and unify
                .registerTypeAdapter(
                        EntryWheelType.class,
                        serializer)
                .registerTypeAdapter(
                        RotorType.class,
                        serializer)
                .registerTypeAdapter(
                        ReflectorType.class,
                        serializer)
                .registerTypeAdapter(
                        PlugBoardConfig.class,
                        serializer)
                .setPrettyPrinting();
    }

    private static JsonSerializer<? extends ScramblerType<?, ?>> getSerializer() {
        return (src, typeOfSrc, context) -> new JsonPrimitive(src.toString());
    }

    default String print() {
        return gson.toJson(this);
    }
}
