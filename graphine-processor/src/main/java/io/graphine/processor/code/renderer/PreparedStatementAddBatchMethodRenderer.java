package io.graphine.processor.code.renderer;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.parameter.ParameterIndexProvider;
import io.graphine.processor.query.model.parameter.IterableParameter;
import io.graphine.processor.query.model.parameter.Parameter;

/**
 * @author Oleg Marchenko
 */
public class PreparedStatementAddBatchMethodRenderer extends PreparedStatementParameterRenderer {
    public PreparedStatementAddBatchMethodRenderer(ParameterIndexProvider parameterIndexProvider) {
        super(parameterIndexProvider);
    }

    @Override
    public CodeBlock visit(IterableParameter parameter) {
        Parameter iteratedParameter = parameter.getIteratedParameter();
        return CodeBlock.builder()
                        .beginControlFlow("for ($T $L : $L)",
                                          iteratedParameter.getType(), iteratedParameter.getName(), parameter.getName())
                        .add(iteratedParameter.accept(this))
                        .addStatement("statement.addBatch()")
                        .endControlFlow()
                        .build();
    }
}
