package ejigma.model.type;

import com.google.gson.*;
import ejigma.util.GsonExclude;

public interface Printable {

    default GsonBuilder getGsonBuilder() {
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
                .registerTypeAdapter(
                        ScramblerType.class,
                        (JsonSerializer<ScramblerType<?, ?>>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getName()))
                .setPrettyPrinting();
    }

    default String print() {
        Gson gson = getGsonBuilder().create();
        return gson.toJson(this);
    }
}
