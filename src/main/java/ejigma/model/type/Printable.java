package ejigma.model.type;

import com.google.gson.*;
import ejigma.util.GsonExclude;

public interface Printable {

    Gson GSON = getGsonBuilder().create();

    static GsonBuilder getGsonBuilder() {
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
                .registerTypeHierarchyAdapter(ScramblerType.class, getSerializer())
                .setPrettyPrinting();
    }

    private static JsonSerializer<? extends ScramblerType<?, ?>> getSerializer() {
        return (src, typeOfSrc, context) -> new JsonPrimitive(src.toString());
    }

    default String print() {
        return GSON.toJson(this);
    }
}
