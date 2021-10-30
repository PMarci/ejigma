package ejigma.model.type;

import com.google.gson.*;
import ejigma.model.component.Scrambler;
import ejigma.util.GsonExclude;

public interface Printable {

    Gson gson = getGsonBuilder().create();

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
                // TODO try and unify
                .registerTypeAdapter(
//                        ScramblerType.class,
//                        AutoEntryWheelType.class,
                        EntryWheelType.class,
                        getSerializer())
                .registerTypeAdapter(
//                        ScramblerType.class,
//                        AutoEntryWheelType.class,
                        RotorType.class,
                        getSerializer())
                .registerTypeAdapter(
//                        ScramblerType.class,
//                        AutoEntryWheelType.class,
                        ReflectorType.class,
                        getSerializer())
                .registerTypeAdapter(
//                        ScramblerType.class,
//                        AutoEntryWheelType.class,
                        PlugBoardConfig.class,
                        getSerializer())
                .setPrettyPrinting();
    }

    private static <S extends Scrambler<S, T>, T extends ScramblerType<S, T>> JsonSerializer<T> getSerializer() {
        return (src, typeOfSrc, context) -> new JsonPrimitive(src.toString());
    }

    default String print() {
        return gson.toJson(this);
    }
}
