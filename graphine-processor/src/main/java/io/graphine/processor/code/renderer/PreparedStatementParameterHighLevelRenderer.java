package io.graphine.processor.code.renderer;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.parameter.ParameterIndexProvider;
import io.graphine.processor.query.model.parameter.ComplexParameter;
import io.graphine.processor.query.model.parameter.IterableParameter;
import io.graphine.processor.query.model.parameter.Parameter;

/**
 * @author Oleg Marchenko
 */
public class PreparedStatementParameterHighLevelRenderer extends PreparedStatementParameterRenderer {
    public PreparedStatementParameterHighLevelRenderer(ParameterIndexProvider parameterIndexProvider) {
        super(parameterIndexProvider);
    }

    @Override
    public CodeBlock visit(Parameter parameter) {
        return CodeBlock.builder()
                        .add(parameter.accept(new PreparedStatementParameterLowLevelRenderer(parameterIndexProvider)))
                        .addStatement("$L.executeUpdate()", DEFAULT_STATEMENT_VARIABLE_NAME)
                        .build();
    }

    @Override
    public CodeBlock visit(ComplexParameter parameter) {
        return CodeBlock.builder()
                        .add(parameter.accept(new PreparedStatementParameterLowLevelRenderer(parameterIndexProvider)))
                        .addStatement("$L.executeUpdate()", DEFAULT_STATEMENT_VARIABLE_NAME)
                        .build();
    }

    @Override
    public CodeBlock visit(IterableParameter parameter) {
        Parameter iteratedParameter = parameter.getIteratedParameter();
        return CodeBlock.builder()
                        .beginControlFlow("for ($T $L : $L)",
                                          iteratedParameter.getType(), iteratedParameter.getName(), parameter.getName())
                        .add(parameter.getIteratedParameter().accept(new PreparedStatementParameterLowLevelRenderer(parameterIndexProvider)))
                        .addStatement("$L.addBatch()", DEFAULT_STATEMENT_VARIABLE_NAME)
                        .endControlFlow()
                        .addStatement("$L.executeBatch()", DEFAULT_STATEMENT_VARIABLE_NAME)
                        .build();
    }
}
