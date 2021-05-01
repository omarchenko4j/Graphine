package io.graphine.processor.code.renderer.parameter.index_provider;

import static io.graphine.processor.util.VariableNameUniqueizer.uniqueize;

/**
 * @author Oleg Marchenko
 */
public class IncrementalParameterIndexProvider implements ParameterIndexProvider {
    public static final String INDEX_VARIABLE_NAME = uniqueize("index");

    private final String externalVariableName;

    public IncrementalParameterIndexProvider(String externalVariableName) {
        this.externalVariableName = externalVariableName;
    }

    @Override
    public String getParameterIndex() {
        return externalVariableName + "++";
    }
}
