package io.graphine.processor.support.naming.pipeline;

import io.graphine.processor.support.SupportedOptions;
import io.graphine.processor.support.naming.pipeline.pipe.GeneralTransformPipes;
import io.graphine.processor.support.naming.pipeline.pipe.TransformPipe;
import io.graphine.processor.util.StringUtils;

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.graphine.processor.util.StringUtils.EMPTY;
import static io.graphine.processor.util.StringUtils.isEmpty;

/**
 * @author Oleg Marchenko
 */
public abstract class UniversalNamingPipeline {
    private static final Pattern PIPELINE_PATTERN = Pattern.compile("\\s*\\|\\s*");

    protected final Collection<TransformPipe> pipes;

    protected UniversalNamingPipeline(SupportedOptions option) {
        this.pipes = option.value(this::toPipes);
    }

    public String transform(String name) {
        if (pipes.isEmpty() || isEmpty(name)) return EMPTY;

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
                     .map(GeneralTransformPipes::valueOf)
                     .sorted()
                     .collect(Collectors.toList());
    }
}
