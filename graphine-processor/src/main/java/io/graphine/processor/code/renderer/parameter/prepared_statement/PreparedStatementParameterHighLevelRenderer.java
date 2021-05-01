package io.graphine.processor.code.renderer.parameter.prepared_statement;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.parameter.index_provider.ParameterIndexProvider;
import io.graphine.processor.query.model.parameter.ComplexParameter;
import io.graphine.processor.query.model.parameter.IterableParameter;
import io.graphine.processor.query.model.parameter.Parameter;

import static io.graphine.processor.code.generator.repository.method.RepositoryMethodImplementationGenerator.STATEMENT_VARIABLE_NAME;

/**
 * @author Oleg Marchenko
 */
public final class PreparedStatementParameterHighLevelRenderer extends PreparedStatementParameterRenderer {
    public PreparedStatementParameterHighLevelRenderer(ParameterIndexProvider parameterIndexProvider) {
        super(parameterIndexProvider);
    }

    @Override
    public CodeBlock visit(Parameter parameter) {
        return CodeBlock.builder()
                        .add(parameter.accept(new PreparedStatementParameterLowLevelRenderer(parameterIndexProvider)))
                        .addStatement("$L.executeUpdate()", STATEMENT_VARIABLE_NAME)
                        .build();
    }

    @Override
    public CodeBlock visit(ComplexParameter parameter) {
        return CodeBlock.builder()
                        .add(parameter.accept(new PreparedStatementParameterLowLevelRenderer(parameterIndexProvider)))
                        .addStatement("$L.executeUpdate()", STATEMENT_VARIABLE_NAME)
                        .build();
    }

    @Override
    public CodeBlock visit(IterableParameter parameter) {
        Parameter iteratedParameter = parameter.getIteratedParameter();
        return CodeBlock.builder()
                        .beginControlFlow("for ($T $L : $L)",
                                          iteratedParameter.getType(), iteratedParameter.getName(), parameter.getName())
                        .add(parameter.getIteratedParameter()
                                      .accept(new PreparedStatementParameterLowLevelRenderer(parameterIndexProvider)))
                        .addStatement("$L.addBatch()", STATEMENT_VARIABLE_NAME)
                        .endControlFlow()
                        .addStatement("$L.executeBatch()", STATEMENT_VARIABLE_NAME)
                        .build();
    }
}
