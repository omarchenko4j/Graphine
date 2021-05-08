package io.graphine.processor.support.naming.pipeline;

import static io.graphine.processor.GraphineOptions.COLUMN_NAMING_PIPELINE;

/**
 * @author Oleg Marchenko
 */
public final class ColumnNamingPipeline extends UniversalNamingPipeline {
    public ColumnNamingPipeline() {
        super(COLUMN_NAMING_PIPELINE);
    }
}
