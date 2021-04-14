package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.query.model.NativeQuery;

import java.sql.PreparedStatement;

import static io.graphine.processor.code.renderer.PreparedStatementParameterRenderer.DEFAULT_STATEMENT_VARIABLE_NAME;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryDeleteMethodImplementationGenerator extends RepositoryMethodImplementationGenerator {
    @Override
    protected CodeBlock renderStatement(MethodMetadata method, NativeQuery query) {
        return CodeBlock.builder()
                        .beginControlFlow("try ($T $L = connection.prepareStatement(query))",
                                          PreparedStatement.class, DEFAULT_STATEMENT_VARIABLE_NAME)
                        .add(renderStatementParameters(method, query))
                        .addStatement("$L.executeUpdate()", DEFAULT_STATEMENT_VARIABLE_NAME)
                        .endControlFlow()
                        .build();
    }
}
