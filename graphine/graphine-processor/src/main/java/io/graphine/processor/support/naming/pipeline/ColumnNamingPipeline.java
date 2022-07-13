package io.graphine.processor.support.naming.pipeline;

import static io.graphine.processor.GraphineOptions.COLUMN_NAMING_PIPELINE;
import static io.graphine.processor.support.naming.pipeline.pipe.GeneralTransformPipes.LOWER_CASE;
import static io.graphine.processor.support.naming.pipeline.pipe.GeneralTransformPipes.SNAKE_CASE;

/**
 * @author Oleg Marchenko
 */
public final class ColumnNamingPipeline extends UniversalNamingPipeline {
    public static final String DEFAULT_COLUMN_NAMING_PIPELINE = SNAKE_CASE.name() + PIPE_SEPARATOR + LOWER_CASE.name();

    public ColumnNamingPipeline() {
        super(COLUMN_NAMING_PIPELINE);
    }
}
