package io.graphine.processor.support;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.graphine.processor.support.EnvironmentContext.options;
import static io.graphine.processor.util.StringUtils.EMPTY;

/**
 * @author Oleg Marchenko
 */
public enum SupportedOptions {
    DEFAULT_SCHEME("graphine.default_schema"),
    TABLE_NAMING_PIPELINE("graphine.table_naming_pipeline"),
    COLUMN_NAMING_PIPELINE("graphine.column_naming_pipeline"),
    ATTRIBUTE_DETECTION_STRATEGY("graphine.attribute_detection_strategy");

    private final String name;

    SupportedOptions(String name) {
        this.name = name;
    }

    public String value() {
        return options.getOrDefault(name, EMPTY);
    }

    public <R> R value(Function<String, R> mapper) {
        String value = value();
        return mapper.apply(value);
    }

    public static Set<String> names() {
        return Arrays.stream(values())
                     .map(option -> option.name)
                     .collect(Collectors.toSet());
    }
}
