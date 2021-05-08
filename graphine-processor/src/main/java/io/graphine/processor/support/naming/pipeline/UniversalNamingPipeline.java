package io.graphine.processor.support.naming.pipeline;

import io.graphine.processor.GraphineOptions;
import io.graphine.processor.support.naming.pipeline.pipe.GeneralTransformPipes;
import io.graphine.processor.support.naming.pipeline.pipe.TransformPipe;
import io.graphine.processor.util.EnumUtils;
import io.graphine.processor.util.StringUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Oleg Marchenko
 */
public abstract class UniversalNamingPipeline {
    private static final Pattern PIPELINE_PATTERN = Pattern.compile("\\s*\\|\\s*");

    protected final Collection<TransformPipe> pipes;

    protected UniversalNamingPipeline(GraphineOptions option) {
        this.pipes = option.value(this::toPipes);
    }

    public String transform(String name) {
        if (pipes.isEmpty()) return name;

        String transformedName = name;
        for (TransformPipe pipe : pipes) {
            transformedName = pipe.transform(transformedName);
        }
        return transformedName;
    }

    private Collection<TransformPipe> toPipes(String value) {
        return Stream.of(PIPELINE_PATTERN.split(value))
                     .map(String::trim)
                     .filter(StringUtils::isNotEmpty)
                     .map(pipeName -> EnumUtils.valueOf(GeneralTransformPipes.class, pipeName))
                     .filter(Objects::nonNull)
                     .sorted()
                     .collect(Collectors.toList());
    }
}
