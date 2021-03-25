package io.graphine.processor.code.renderer;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.query.model.parameter.ComplexParameter;
import io.graphine.processor.query.model.parameter.IterableParameter;
import io.graphine.processor.query.model.parameter.Parameter;
import io.graphine.processor.query.model.parameter.ParameterVisitor;

/**
 * @author Oleg Marchenko
 */
public class PreparedStatementExecuteMethodRenderer implements ParameterVisitor<CodeBlock> {
    @Override
    public CodeBlock visit(Parameter parameter) {
        return CodeBlock.builder()
                        .addStatement("statement.executeUpdate()")
                        .build();
    }

    @Override
    public CodeBlock visit(ComplexParameter parameter) {
        return CodeBlock.builder()
                        .addStatement("statement.executeUpdate()")
                        .build();
    }

    @Override
    public CodeBlock visit(IterableParameter parameter) {
        return CodeBlock.builder()
                        .addStatement("statement.executeBatch()")
                        .build();
    }
}
