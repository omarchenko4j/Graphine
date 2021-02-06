package io.graphine.processor.util;

import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.isNull;

/**
 * @author Oleg Marchenko
 */
public final class ObjectUtils {

    public static <T, R> R computeIf(T object, Supplier<R> nullSupplier, Function<T, R> nonNullFunction) {
        return isNull(object) ? nullSupplier.get() : nonNullFunction.apply(object);
    }

    private ObjectUtils() {
    }
}
