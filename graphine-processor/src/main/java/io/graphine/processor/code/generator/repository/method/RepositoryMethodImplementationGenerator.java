package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import io.graphine.core.GraphineException;
import io.graphine.core.util.UnnamedParameterRepeater;
import io.graphine.processor.code.renderer.index.IncrementalParameterIndexProvider;
import io.graphine.processor.code.renderer.index.NumericParameterIndexProvider;
import io.graphine.processor.code.renderer.index.ParameterIndexProvider;
import io.graphine.processor.code.renderer.mapping.ResultSetMappingRenderer;
import io.graphine.processor.code.renderer.mapping.StatementMappingRenderer;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment.AndPredicate;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment.OperatorType;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment.OrPredicate;
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

import static io.graphine.processor.code.renderer.index.IncrementalParameterIndexProvider.INDEX_VARIABLE_NAME;
import static io.graphine.processor.util.VariableNameUniqueizer.uniqueize;
import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
public abstract class RepositoryMethodImplementationGenerator {
    public static final String CONNECTION_VARIABLE_NAME = uniqueize("connection");
    public static final String QUERY_VARIABLE_NAME = uniqueize("query");
    public static final String STATEMENT_VARIABLE_NAME = uniqueize("statement");
    public static final String RESULT_SET_VARIABLE_NAME = uniqueize("resultSet");

    protected final StatementMappingRenderer statementMappingRenderer;
    protected final ResultSetMappingRenderer resultSetMappingRenderer;

    protected RepositoryMethodImplementationGenerator(StatementMappingRenderer statementMappingRenderer,
                                                      ResultSetMappingRenderer resultSetMappingRenderer) {
        this.statementMappingRenderer = statementMappingRenderer;
        this.resultSetMappingRenderer = resultSetMappingRenderer;
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
        CodeBlock.Builder snippetBuilder = CodeBlock.builder();

        List<ParameterMetadata> parameters = method.getParameters();
        if (!parameters.isEmpty()) {
            ParameterIndexProvider parameterIndexProvider;
            if (method.getDeferredParameters().isEmpty()) {
                parameterIndexProvider = new NumericParameterIndexProvider();
            }
            else {
                parameterIndexProvider = new IncrementalParameterIndexProvider(INDEX_VARIABLE_NAME);

                snippetBuilder.addStatement("int $L = 1", INDEX_VARIABLE_NAME);
            }

            QueryableMethodName queryableName = method.getQueryableName();
            ConditionFragment condition = queryableName.getCondition();
            if (nonNull(condition)) {
                int i = 0;

                List<OrPredicate> orPredicates = condition.getOrPredicates();
                for (OrPredicate orPredicate : orPredicates) {
                    List<AndPredicate> andPredicates = orPredicate.getAndPredicates();
                    for (AndPredicate andPredicate : andPredicates) {
                        OperatorType operator = andPredicate.getOperator();

                        for (int j = i; j < i + operator.getParameterCount(); j++) {
                            ParameterMetadata parameter = parameters.get(j);

                            String parameterName = parameter.getName();
                            CodeBlock parameterValue;
                            switch (operator) {
                                case STARTING_WITH:
                                    parameterValue = CodeBlock.of("$L + '%'", parameterName);
                                    break;
                                case ENDING_WITH:
                                    parameterValue = CodeBlock.of("'%' + $L", parameterName);
                                    break;
                                case CONTAINING:
                                case NOT_CONTAINING:
                                    parameterValue = CodeBlock.of("'%' + $L + '%'", parameterName);
                                    break;
                                default:
                                    parameterValue = CodeBlock.of(parameterName);
                                    break;
                            }

                            TypeMirror parameterType = parameter.getNativeType();
                            String parameterIndex = parameterIndexProvider.getParameterIndex();

                            snippetBuilder.add(statementMappingRenderer.render(parameterType,
                                                                               parameterIndex,
                                                                               parameterValue));
                        }

                        i += operator.getParameterCount();
                    }
                }
            }
        }

        return snippetBuilder.build();
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
