package io.graphine.processor.code.renderer.parameter;

/**
 * @author Oleg Marchenko
 */
public class IncrementalParameterIndexProvider implements ParameterIndexProvider {
    private final String externalVariableName;

    public IncrementalParameterIndexProvider(String externalVariableName) {
        this.externalVariableName = externalVariableName;
    }

    @Override
    public String getParameterIndex() {
        return externalVariableName + "++";
    }
}
