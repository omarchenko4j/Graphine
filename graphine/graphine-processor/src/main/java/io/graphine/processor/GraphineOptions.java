package io.graphine.processor;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.graphine.processor.support.AttributeDetectionStrategy.DEFAULT_ATTRIBUTE_DETECTION_STRATEGY;
import static io.graphine.processor.support.EnvironmentContext.options;
import static io.graphine.processor.support.naming.pipeline.ColumnNamingPipeline.DEFAULT_COLUMN_NAMING_PIPELINE;
import static io.graphine.processor.support.naming.pipeline.TableNamingPipeline.DEFAULT_TABLE_NAMING_PIPELINE;
import static io.graphine.processor.util.StringUtils.EMPTY;

/**
 * @author Oleg Marchenko
 */
public enum GraphineOptions {
    DEFAULT_SCHEME("graphine.default_schema", EMPTY),
    TABLE_NAMING_PIPELINE("graphine.table_naming_pipeline", DEFAULT_TABLE_NAMING_PIPELINE),
    COLUMN_NAMING_PIPELINE("graphine.column_naming_pipeline", DEFAULT_COLUMN_NAMING_PIPELINE),
    ATTRIBUTE_DETECTION_STRATEGY("graphine.attribute_detection_strategy", DEFAULT_ATTRIBUTE_DETECTION_STRATEGY),
    READ_ONLY_HINT_ENABLED("graphine.read_only_hint.enabled", "false");

    private final String name;
    private final String defaultValue;

    GraphineOptions(String name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public String value() {
        return options.getOrDefault(name, defaultValue);
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
