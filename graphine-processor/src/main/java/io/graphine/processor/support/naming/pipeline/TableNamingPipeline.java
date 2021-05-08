package io.graphine.processor.support.naming.pipeline;

import static io.graphine.processor.GraphineOptions.TABLE_NAMING_PIPELINE;

/**
 * @author Oleg Marchenko
 */
public final class TableNamingPipeline extends UniversalNamingPipeline {
    public TableNamingPipeline() {
        super(TABLE_NAMING_PIPELINE);
    }
}
