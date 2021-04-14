package io.graphine.processor.code.renderer.parameter.index_provider;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Oleg Marchenko
 */
public class NumericParameterIndexProvider implements ParameterIndexProvider {
    private final AtomicInteger indexCounter;

    public NumericParameterIndexProvider() {
        this.indexCounter = new AtomicInteger(1);
    }

    @Override
    public String getParameterIndex() {
        return String.valueOf(indexCounter.getAndIncrement());
    }
}
