package io.graphine.processor.support;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static io.graphine.processor.support.EnvironmentContext.options;
import static io.graphine.processor.util.StringUtils.EMPTY;

/**
 * @author Oleg Marchenko
 */
public enum SupportedOptions {
    DEFAULT_SCHEME("graphine.default_schema");

    private final String name;

    SupportedOptions(String name) {
        this.name = name;
    }

    public String value() {
        return options.getOrDefault(name, EMPTY);
    }

    public static Set<String> names() {
        return Arrays.stream(values())
                     .map(option -> option.name)
                     .collect(Collectors.toSet());
    }
}
