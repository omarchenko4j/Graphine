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
                        .beginControlFlow("try ($T statement = connection.prepareStatement(query))",
                                          PreparedStatement.class)
                        .add(renderStatementParameters(method, query))
                        .addStatement("statement.executeUpdate()")
                        .endControlFlow()
                        .build();
    }
}
