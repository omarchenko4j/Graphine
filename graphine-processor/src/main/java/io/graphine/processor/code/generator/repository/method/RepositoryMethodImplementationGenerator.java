package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import io.graphine.core.GraphineException;
import io.graphine.core.util.UnnamedParameterRepeater;
import io.graphine.processor.code.renderer.parameter.index_provider.IncrementalParameterIndexProvider;
import io.graphine.processor.code.renderer.parameter.index_provider.NumericParameterIndexProvider;
import io.graphine.processor.code.renderer.parameter.index_provider.ParameterIndexProvider;
import io.graphine.processor.code.renderer.parameter.prepared_statement.PreparedStatementParameterLowLevelRenderer;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.query.model.NativeQuery;
import io.graphine.processor.query.model.parameter.Parameter;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static io.graphine.processor.code.renderer.parameter.index_provider.IncrementalParameterIndexProvider.INDEX_VARIABLE_NAME;
import static io.graphine.processor.util.VariableNameUniqueizer.uniqueize;

/**
 * @author Oleg Marchenko
 */
public abstract class RepositoryMethodImplementationGenerator {
    public static final String CONNECTION_VARIABLE_NAME = uniqueize("connection");
    public static final String QUERY_VARIABLE_NAME = uniqueize("query");
    public static final String STATEMENT_VARIABLE_NAME = uniqueize("statement");
    public static final String RESULT_SET_VARIABLE_NAME = uniqueize("resultSet");

    public final MethodSpec generate(MethodMetadata method, NativeQuery query) {
        return MethodSpec.overriding(method.getNativeElement())
                         .addCode(renderConnection(method, query))
                         .build();
    }

    protected CodeBlock renderConnection(MethodMetadata method, NativeQuery query) {
        return CodeBlock.builder()
                        .beginControlFlow("try ($T $L = dataSource.getConnection())",
                                          Connection.class, CONNECTION_VARIABLE_NAME)
                        .add(renderQuery(query))
                        .add(renderStatement(method, query))
                        .endControlFlow()
                        .beginControlFlow("catch ($T e)", SQLException.class)
                        .addStatement("throw new $T(e)", GraphineException.class)
                        .endControlFlow()
                        .build();
    }

    protected CodeBlock renderQuery(NativeQuery query) {
        List<Parameter> deferredParameters = query.getDeferredParameters();
        if (deferredParameters.isEmpty()) {
            return CodeBlock.builder()
                            .addStatement("$T $L = $S", String.class, QUERY_VARIABLE_NAME, query.getValue())
                            .build();
        }
        else {
            List<CodeBlock> unnamedParameterSnippets = new ArrayList<>(deferredParameters.size());
            for (Parameter parameter : deferredParameters) {
                String parameterName = parameter.getName();
                TypeMirror parameterType = parameter.getType();
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

    protected CodeBlock renderStatement(MethodMetadata method, NativeQuery query) {
        return CodeBlock.builder()
                        .beginControlFlow("try ($T $L = $L.prepareStatement($L))",
                                          PreparedStatement.class,
                                          STATEMENT_VARIABLE_NAME,
                                          CONNECTION_VARIABLE_NAME,
                                          QUERY_VARIABLE_NAME)
                        .add(renderStatementParameters(method, query))
                        .add(renderResultSet(method, query))
                        .endControlFlow()
                        .build();
    }

    protected CodeBlock renderStatementParameters(MethodMetadata method, NativeQuery query) {
        CodeBlock.Builder builder = CodeBlock.builder();

        List<Parameter> consumedParameters = query.getConsumedParameters();
        if (!consumedParameters.isEmpty()) {
            ParameterIndexProvider parameterIndexProvider;

            List<Parameter> deferredParameters = query.getDeferredParameters();
            if (deferredParameters.isEmpty()) {
                parameterIndexProvider = new NumericParameterIndexProvider();
            }
            else {
                builder.addStatement("int $L = 1", INDEX_VARIABLE_NAME);
                parameterIndexProvider = new IncrementalParameterIndexProvider(INDEX_VARIABLE_NAME);
            }

            for (Parameter parameter : consumedParameters) {
                builder.add(parameter.accept(new PreparedStatementParameterLowLevelRenderer(parameterIndexProvider)));
            }
        }

        return builder.build();
    }

    protected CodeBlock renderResultSet(MethodMetadata method, NativeQuery query) {
        return CodeBlock.builder()
                        .beginControlFlow("try ($T $L = $L.executeQuery())",
                                          ResultSet.class,
                                          RESULT_SET_VARIABLE_NAME,
                                          STATEMENT_VARIABLE_NAME)
                        .add(renderResultSetParameters(method, query))
                        .endControlFlow()
                        .build();
    }

    protected CodeBlock renderResultSetParameters(MethodMetadata method, NativeQuery query) {
        return CodeBlock.builder().build();
    }
}
