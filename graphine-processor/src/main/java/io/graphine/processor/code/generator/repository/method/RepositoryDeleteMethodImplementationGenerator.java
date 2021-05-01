package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.query.model.NativeQuery;

import java.sql.PreparedStatement;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryDeleteMethodImplementationGenerator extends RepositoryMethodImplementationGenerator {
    @Override
    protected CodeBlock renderStatement(MethodMetadata method, NativeQuery query) {
        return CodeBlock.builder()
                        .beginControlFlow("try ($T $L = $L.prepareStatement($L))",
                                          PreparedStatement.class,
                                          STATEMENT_VARIABLE_NAME,
                                          CONNECTION_VARIABLE_NAME,
                                          QUERY_VARIABLE_NAME)
                        .add(renderStatementParameters(method, query))
                        .addStatement("$L.executeUpdate()", STATEMENT_VARIABLE_NAME)
                        .endControlFlow()
                        .build();
    }
}
