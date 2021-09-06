package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.CodeBlock;
import io.graphine.core.GraphineException;
import io.graphine.processor.code.renderer.mapping.ResultSetMappingRenderer;
import io.graphine.processor.code.renderer.mapping.StatementMappingRenderer;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;
import io.graphine.processor.query.model.NativeQuery;

import java.sql.Connection;
import java.sql.SQLException;

import static io.graphine.processor.util.VariableNameUniqueizer.uniqueize;

/**
 * @author Oleg Marchenko
 */
public abstract class RepositoryModifyingMethodImplementationGenerator extends RepositoryMethodImplementationGenerator {
    private static final String AUTO_COMMIT_VARIABLE_NAME = uniqueize("autoCommit");

    protected RepositoryModifyingMethodImplementationGenerator(EntityMetadataRegistry entityMetadataRegistry,
                                                               StatementMappingRenderer statementMappingRenderer,
                                                               ResultSetMappingRenderer resultSetMappingRenderer) {
        super(entityMetadataRegistry, statementMappingRenderer, resultSetMappingRenderer);
    }

    @Override
    protected CodeBlock renderConnection(MethodMetadata method, NativeQuery query, EntityMetadata entity) {
        return CodeBlock.builder()
                        .beginControlFlow("try ($T $L = dataSource.getConnection())",
                                          Connection.class, CONNECTION_VARIABLE_NAME)
                        .addStatement("$T $L = $L.getAutoCommit()",
                                      boolean.class, AUTO_COMMIT_VARIABLE_NAME, CONNECTION_VARIABLE_NAME)
                        .beginControlFlow("if ($L)", AUTO_COMMIT_VARIABLE_NAME)
                        .addStatement("$L.setAutoCommit(false)", CONNECTION_VARIABLE_NAME)
                        .endControlFlow()
                        .beginControlFlow("try")
                        .add(renderQuery(method, query))
                        .add(renderStatement(method, query, entity))
                        .beginControlFlow("if ($L)", AUTO_COMMIT_VARIABLE_NAME)
                        .addStatement("$L.commit()", CONNECTION_VARIABLE_NAME)
                        .endControlFlow()
                        .endControlFlow()
                        .beginControlFlow("catch ($T $L)",
                                          Exception.class, EXCEPTION_VARIABLE_NAME)
                        .beginControlFlow("if ($L)", AUTO_COMMIT_VARIABLE_NAME)
                        .addStatement("$L.rollback()", CONNECTION_VARIABLE_NAME)
                        .endControlFlow()
                        .addStatement("throw $L", EXCEPTION_VARIABLE_NAME)
                        .endControlFlow()
                        .endControlFlow()
                        .beginControlFlow("catch ($T $L)",
                                          SQLException.class, EXCEPTION_VARIABLE_NAME)
                        .addStatement("throw new $T($L)",
                                      GraphineException.class, EXCEPTION_VARIABLE_NAME)
                        .endControlFlow()
                        .build();
    }
}
