package io.graphine.processor.code.renderer.index;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Oleg Marchenko
 */
public class NumericParameterIndexProvider implements ParameterIndexProvider {
    private final AtomicInteger indexCounter;

    public NumericParameterIndexProvider() {
        this.indexCounter = new AtomicInteger(1);
    }

    public NumericParameterIndexProvider(NumericParameterIndexProvider parameterIndexProvider) {
        this.indexCounter = new AtomicInteger(parameterIndexProvider.indexCounter.intValue());
    }

    @Override
    public String getParameterIndex() {
        return String.valueOf(indexCounter.getAndIncrement());
    }
}
