package io.graphine.processor.support.naming.pipeline;

import static io.graphine.processor.GraphineOptions.TABLE_NAMING_PIPELINE;
import static io.graphine.processor.support.naming.pipeline.pipe.GeneralTransformPipes.LOWER_CASE;
import static io.graphine.processor.support.naming.pipeline.pipe.GeneralTransformPipes.SNAKE_CASE;

/**
 * @author Oleg Marchenko
 */
public final class TableNamingPipeline extends UniversalNamingPipeline {
    public static final String DEFAULT_TABLE_NAMING_PIPELINE = SNAKE_CASE.name() + PIPE_SEPARATOR + LOWER_CASE.name();

    public TableNamingPipeline() {
        super(TABLE_NAMING_PIPELINE);
    }
}
