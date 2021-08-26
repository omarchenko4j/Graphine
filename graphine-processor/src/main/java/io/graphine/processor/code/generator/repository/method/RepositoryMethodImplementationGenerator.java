package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import io.graphine.core.GraphineException;
import io.graphine.core.util.UnnamedParameterRepeater;
import io.graphine.processor.code.renderer.PreparedStatementMethodMappingRenderer;
import io.graphine.processor.code.renderer.RepositoryMethodParameterMappingRenderer;
import io.graphine.processor.code.renderer.ResultSetMethodMappingRenderer;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.parameter.ParameterMetadata;
import io.graphine.processor.query.model.NativeQuery;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static io.graphine.processor.util.VariableNameUniqueizer.uniqueize;

/**
 * @author Oleg Marchenko
 */
public abstract class RepositoryMethodImplementationGenerator {
    public static final String CONNECTION_VARIABLE_NAME = uniqueize("connection");
    public static final String QUERY_VARIABLE_NAME = uniqueize("query");
    public static final String STATEMENT_VARIABLE_NAME = uniqueize("statement");
    public static final String RESULT_SET_VARIABLE_NAME = uniqueize("resultSet");

    protected final PreparedStatementMethodMappingRenderer preparedStatementMethodMappingRenderer;
    protected final ResultSetMethodMappingRenderer resultSetMethodMappingRenderer;

    protected final RepositoryMethodParameterMappingRenderer repositoryMethodParameterMappingRenderer;

    protected RepositoryMethodImplementationGenerator() {
        this.preparedStatementMethodMappingRenderer = new PreparedStatementMethodMappingRenderer();
        this.resultSetMethodMappingRenderer = new ResultSetMethodMappingRenderer();

        this.repositoryMethodParameterMappingRenderer =
                new RepositoryMethodParameterMappingRenderer(preparedStatementMethodMappingRenderer);
    }

    public final MethodSpec generate(MethodMetadata method, NativeQuery query, EntityMetadata entity) {
        return MethodSpec.overriding(method.getNativeElement())
                         .addCode(renderConnection(method, query, entity))
                         .build();
    }

    protected CodeBlock renderConnection(MethodMetadata method, NativeQuery query, EntityMetadata entity) {
        return CodeBlock.builder()
                        .beginControlFlow("try ($T $L = dataSource.getConnection())",
                                          Connection.class, CONNECTION_VARIABLE_NAME)
                        .add(renderQuery(method, query))
                        .add(renderStatement(method, query, entity))
                        .endControlFlow()
                        .beginControlFlow("catch ($T e)", SQLException.class)
                        .addStatement("throw new $T(e)", GraphineException.class)
                        .endControlFlow()
                        .build();
    }

    protected CodeBlock renderQuery(MethodMetadata method, NativeQuery query) {
        List<ParameterMetadata> deferredParameters = method.getDeferredParameters();
        if (deferredParameters.isEmpty()) {
            return CodeBlock.builder()
                            .addStatement("$T $L = $S", String.class, QUERY_VARIABLE_NAME, query.getValue())
                            .build();
        }
        else {
            List<CodeBlock> unnamedParameterSnippets = new ArrayList<>(deferredParameters.size());
            for (ParameterMetadata parameter : deferredParameters) {
                String parameterName = parameter.getName();
                TypeMirror parameterType = parameter.getNativeType();
                switch (parameterType.getKind()) {
                    case ARRAY:
                        unnamedParameterSnippets.add(CodeBlock.of("$T.repeat($L.length)",
                                                                  UnnamedParameterRepeater.class, parameterName));
                        break;
                    case DECLARED:
                        DeclaredType declaredType = (DeclaredType) parameterType;
                        TypeElement typeElement = (TypeElement) declaredType.asElement();
                        switch (typeElement.getQualifiedName().toString()) {
                            case "java.lang.Iterable":
                                unnamedParameterSnippets.add(CodeBlock.of("$T.repeatFor($L)",
                                                                          UnnamedParameterRepeater.class, parameterName));
                                break;
                            case "java.util.Collection":
                            case "java.util.List":
                            case "java.util.Set":
                                unnamedParameterSnippets.add(CodeBlock.of("$T.repeat($L.size())",
                                                                          UnnamedParameterRepeater.class, parameterName));
                                break;
                        }
                        break;
                }
            }
            return CodeBlock.builder()
                            .addStatement("$T $L = $T.format($S, $L)",
                                          String.class, QUERY_VARIABLE_NAME, String.class, query.getValue(),
                                          CodeBlock.join(unnamedParameterSnippets, ", "))
                            .build();
        }
    }

    protected CodeBlock renderStatement(MethodMetadata method, NativeQuery query, EntityMetadata entity) {
        return CodeBlock.builder()
                        .beginControlFlow("try ($T $L = $L.prepareStatement($L))",
                                          PreparedStatement.class,
                                          STATEMENT_VARIABLE_NAME,
                                          CONNECTION_VARIABLE_NAME,
                                          QUERY_VARIABLE_NAME)
                        .add(renderStatementParameters(method, query, entity))
                        .add(renderResultSet(method, query, entity))
                        .endControlFlow()
                        .build();
    }

    protected CodeBlock renderStatementParameters(MethodMetadata method, NativeQuery query, EntityMetadata entity) {
        return repositoryMethodParameterMappingRenderer.render(method);
    }

    protected CodeBlock renderResultSet(MethodMetadata method, NativeQuery query, EntityMetadata entity) {
        return CodeBlock.builder()
                        .beginControlFlow("try ($T $L = $L.executeQuery())",
                                          ResultSet.class,
                                          RESULT_SET_VARIABLE_NAME,
                                          STATEMENT_VARIABLE_NAME)
                        .add(renderResultSetParameters(method, query, entity))
                        .endControlFlow()
                        .build();
    }

    protected CodeBlock renderResultSetParameters(MethodMetadata method, NativeQuery query, EntityMetadata entity) {
        return CodeBlock.builder().build();
    }
}
