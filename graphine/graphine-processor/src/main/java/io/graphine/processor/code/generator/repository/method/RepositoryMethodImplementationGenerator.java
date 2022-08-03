package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import io.graphine.processor.code.renderer.AttributeFromResultSetMappingRenderer;
import io.graphine.processor.code.renderer.AttributeToStatementMappingRenderer;
import io.graphine.processor.code.renderer.index.IncrementalParameterIndexProvider;
import io.graphine.processor.code.renderer.index.NumericParameterIndexProvider;
import io.graphine.processor.code.renderer.index.ParameterIndexProvider;
import io.graphine.processor.code.renderer.mapping.ResultSetMappingRenderer;
import io.graphine.processor.code.renderer.mapping.StatementMappingRenderer;
import io.graphine.processor.metadata.model.entity.EmbeddableEntityMetadata;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment.AndPredicate;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment.OperatorType;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment.OrPredicate;
import io.graphine.processor.metadata.model.repository.method.parameter.ParameterMetadata;
import io.graphine.processor.metadata.registry.AttributeMapperMetadataRegistry;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;
import io.graphine.processor.query.model.NativeQuery;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static io.graphine.processor.GraphineOptions.READ_ONLY_HINT_ENABLED;
import static io.graphine.processor.code.generator.infrastructure.GeneralExceptionGenerator.GraphineException;
import static io.graphine.processor.code.generator.infrastructure.WildcardRepeaterGenerator.WildcardRepeater;
import static io.graphine.processor.code.renderer.index.IncrementalParameterIndexProvider.INDEX_VARIABLE_NAME;
import static io.graphine.processor.util.AccessorUtils.getter;
import static io.graphine.processor.util.StringUtils.uncapitalize;
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
    protected static final String EXCEPTION_VARIABLE_NAME = uniqueize("e");
    private static final String READ_ONLY_VARIABLE_NAME = uniqueize("readOnly");

    protected final EntityMetadataRegistry entityMetadataRegistry;
    protected final StatementMappingRenderer statementMappingRenderer;
    protected final ResultSetMappingRenderer resultSetMappingRenderer;

    protected final AttributeToStatementMappingRenderer attributeToStatementMappingRenderer;
    protected final AttributeFromResultSetMappingRenderer attributeFromResultSetMappingRenderer;

    protected RepositoryMethodImplementationGenerator(EntityMetadataRegistry entityMetadataRegistry,
                                                      AttributeMapperMetadataRegistry attributeMapperMetadataRegistry,
                                                      StatementMappingRenderer statementMappingRenderer,
                                                      ResultSetMappingRenderer resultSetMappingRenderer) {
        this.entityMetadataRegistry = entityMetadataRegistry;
        this.statementMappingRenderer = statementMappingRenderer;
        this.resultSetMappingRenderer = resultSetMappingRenderer;

        this.attributeToStatementMappingRenderer =
                new AttributeToStatementMappingRenderer(entityMetadataRegistry,
                                                        attributeMapperMetadataRegistry,
                                                        statementMappingRenderer);
        this.attributeFromResultSetMappingRenderer =
                new AttributeFromResultSetMappingRenderer(entityMetadataRegistry,
                                                          attributeMapperMetadataRegistry,
                                                          resultSetMappingRenderer);
    }

    public final MethodSpec generate(MethodMetadata method, NativeQuery query, String entityQualifiedName) {
        EntityMetadata entity = entityMetadataRegistry.getEntity(entityQualifiedName);
        return MethodSpec.overriding(method.getNativeElement())
                         .addCode(renderConnection(method, query, entity))
                         .build();
    }

    protected CodeBlock renderConnection(MethodMetadata method, NativeQuery query, EntityMetadata entity) {
        boolean readOnlyHintEnabled = READ_ONLY_HINT_ENABLED.value(Boolean::parseBoolean);

        CodeBlock.Builder snippetBuilder = CodeBlock.builder();
        snippetBuilder
                .beginControlFlow("try ($T $L = dataSource.getConnection())",
                                  Connection.class, CONNECTION_VARIABLE_NAME);
        if (readOnlyHintEnabled) {
            snippetBuilder
                    .addStatement("$T $L = $L.isReadOnly()",
                                  boolean.class, READ_ONLY_VARIABLE_NAME, CONNECTION_VARIABLE_NAME)
                    .beginControlFlow("if (!$L)", READ_ONLY_VARIABLE_NAME)
                    .addStatement("$L.setReadOnly(true)", CONNECTION_VARIABLE_NAME)
                    .endControlFlow()
                    .beginControlFlow("try");
        }
        snippetBuilder
                .add(renderQuery(method, query))
                .add(renderStatement(method, query, entity));
        if (readOnlyHintEnabled) {
            snippetBuilder
                    .endControlFlow()
                    .beginControlFlow("finally")
                    .addStatement("$L.setReadOnly($L)",
                                  CONNECTION_VARIABLE_NAME, READ_ONLY_VARIABLE_NAME)
                    .endControlFlow();
        }
        snippetBuilder
                .endControlFlow()
                .beginControlFlow("catch ($T $L)",
                                  SQLException.class, EXCEPTION_VARIABLE_NAME)
                .addStatement("throw new $T($L)",
                              GraphineException, EXCEPTION_VARIABLE_NAME)
                .endControlFlow();
        return snippetBuilder.build();
    }

    protected CodeBlock renderQuery(MethodMetadata method, NativeQuery query) {
        List<ParameterMetadata> deferredParameters = method.getDeferredParameters();
        if (deferredParameters.isEmpty()) {
            return CodeBlock.builder()
                            .addStatement("$T $L = $S", String.class, QUERY_VARIABLE_NAME, query.getValue())
                            .build();
        }
        else {
            List<CodeBlock> wildcardParameterSnippets = new ArrayList<>(deferredParameters.size());
            for (ParameterMetadata parameter : deferredParameters) {
                String parameterName = parameter.getName();
                TypeMirror parameterType = parameter.getNativeType();
                switch (parameterType.getKind()) {
                    case ARRAY:
                        ArrayType arrayType = (ArrayType) parameterType;
                        TypeMirror componentType = arrayType.getComponentType();
                        if (entityMetadataRegistry.containsEmbeddableEntity(componentType.toString())) {
                            EmbeddableEntityMetadata embeddableEntity =
                                    entityMetadataRegistry.getEmbeddableEntity(componentType.toString());
                            for (int i = 0; i < embeddableEntity.getAttributes().size(); i++) {
                                wildcardParameterSnippets.add(CodeBlock.of("$T.repeat($L.length)",
                                                                           WildcardRepeater, parameterName));
                            }
                        }
                        else {
                            wildcardParameterSnippets.add(CodeBlock.of("$T.repeat($L.length)",
                                                                       WildcardRepeater, parameterName));
                        }
                        break;
                    case DECLARED:
                        DeclaredType declaredType = (DeclaredType) parameterType;
                        TypeElement typeElement = (TypeElement) declaredType.asElement();
                        switch (typeElement.getQualifiedName().toString()) {
                            case "java.lang.Iterable":
                                wildcardParameterSnippets.add(CodeBlock.of("$T.repeatFor($L)",
                                                                           WildcardRepeater, parameterName));
                                break;
                            case "java.util.Collection":
                            case "java.util.List":
                            case "java.util.Set":
                                TypeMirror genericType = declaredType.getTypeArguments().get(0);

                                String qualifiedName = genericType.toString();
                                if (entityMetadataRegistry.containsEmbeddableEntity(qualifiedName)) {
                                    EmbeddableEntityMetadata embeddableEntity =
                                            entityMetadataRegistry.getEmbeddableEntity(qualifiedName);
                                    for (int i = 0; i < embeddableEntity.getAttributes().size(); i++) {
                                        wildcardParameterSnippets.add(CodeBlock.of("$T.repeat($L.size())",
                                                                                   WildcardRepeater, parameterName));
                                    }
                                }
                                else {
                                    wildcardParameterSnippets.add(CodeBlock.of("$T.repeat($L.size())",
                                                                               WildcardRepeater, parameterName));
                                }
                                break;
                        }
                        break;
                }
            }
            return CodeBlock.builder()
                            .addStatement("$T $L = $T.format($S, $L)",
                                          String.class, QUERY_VARIABLE_NAME, String.class, query.getValue(),
                                          CodeBlock.join(wildcardParameterSnippets, ", "))
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
                            String parameterIndex;

                            TypeMirror parameterType = parameter.getNativeType();
                            switch (parameterType.getKind()) {
                                case ARRAY:
                                    ArrayType arrayType = (ArrayType) parameterType;
                                    TypeMirror componentType = arrayType.getComponentType();
                                    if (entityMetadataRegistry.containsEmbeddableEntity(componentType.toString())) {
                                        EmbeddableEntityMetadata embeddableEntity =
                                                entityMetadataRegistry.getEmbeddableEntity(componentType.toString());

                                        snippetBuilder
                                                .addStatement("int $L = $L.length",
                                                              uniqueize("length"), parameter.getName());

                                        String entityVariableName = uniqueize(uncapitalize(embeddableEntity.getName()));
                                        snippetBuilder
                                                .beginControlFlow("for ($T $L : $L)",
                                                                  embeddableEntity.getNativeType(), entityVariableName, parameter.getName());

                                        List<AttributeMetadata> embeddedAttributes = embeddableEntity.getAttributes();
                                        if (embeddedAttributes.size() >= 1) {
                                            AttributeMetadata attribute = embeddedAttributes.get(0);
                                            snippetBuilder
                                                    .add(statementMappingRenderer.render(attribute.getNativeType(),
                                                                                         INDEX_VARIABLE_NAME,
                                                                                         CodeBlock.of("$L.$L()",
                                                                                                      entityVariableName, getter(attribute))));
                                        }
                                        if (embeddedAttributes.size() >= 2) {
                                            AttributeMetadata attribute = embeddedAttributes.get(1);
                                            snippetBuilder
                                                    .add(statementMappingRenderer.render(attribute.getNativeType(),
                                                                                         INDEX_VARIABLE_NAME + " + " + uniqueize("length"),
                                                                                         CodeBlock.of("$L.$L()",
                                                                                                      entityVariableName, getter(attribute))));
                                        }
                                        for (int k = 2; k < embeddedAttributes.size(); k++) {
                                            AttributeMetadata attribute = embeddedAttributes.get(k);
                                            snippetBuilder
                                                    .add(statementMappingRenderer.render(attribute.getNativeType(),
                                                                                         INDEX_VARIABLE_NAME + " + (" + uniqueize("length") + " * " + k + ")",
                                                                                         CodeBlock.of("$L.$L()",
                                                                                                      entityVariableName, getter(attribute))));
                                        }

                                        snippetBuilder
                                                .addStatement("$L++", INDEX_VARIABLE_NAME)
                                                .endControlFlow();
                                        break;
                                    }

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

                                    parameterIndex = parameterIndexProvider.getParameterIndex();
                                    snippetBuilder
                                            .add(statementMappingRenderer.render(parameterType, parameterIndex, parameterValue));
                                    break;
                                case DECLARED:
                                    DeclaredType declaredType = (DeclaredType) parameterType;
                                    TypeElement typeElement = (TypeElement) declaredType.asElement();
                                    switch (typeElement.getQualifiedName().toString()) {
                                        case "java.lang.Iterable":
                                        case "java.util.Collection":
                                        case "java.util.List":
                                        case "java.util.Set":
                                            TypeMirror genericType = declaredType.getTypeArguments().get(0);
                                            if (entityMetadataRegistry.containsEmbeddableEntity(genericType.toString())) {
                                                EmbeddableEntityMetadata embeddableEntity =
                                                        entityMetadataRegistry.getEmbeddableEntity(genericType.toString());

                                                snippetBuilder
                                                        .addStatement("int $L = $L.size()",
                                                                      uniqueize("size"), parameter.getName());

                                                String entityVariableName = uniqueize(uncapitalize(embeddableEntity.getName()));
                                                snippetBuilder
                                                        .beginControlFlow("for ($T $L : $L)",
                                                                          embeddableEntity.getNativeType(), entityVariableName, parameter.getName());

                                                List<AttributeMetadata> embeddedAttributes = embeddableEntity.getAttributes();
                                                if (embeddedAttributes.size() >= 1) {
                                                    AttributeMetadata attribute = embeddedAttributes.get(0);
                                                    snippetBuilder
                                                            .add(statementMappingRenderer.render(attribute.getNativeType(),
                                                                                                 INDEX_VARIABLE_NAME,
                                                                                                 CodeBlock.of("$L.$L()",
                                                                                                              entityVariableName, getter(attribute))));
                                                }
                                                if (embeddedAttributes.size() >= 2) {
                                                    AttributeMetadata attribute = embeddedAttributes.get(1);
                                                    snippetBuilder
                                                            .add(statementMappingRenderer.render(attribute.getNativeType(),
                                                                                                 INDEX_VARIABLE_NAME + " + " + uniqueize("size"),
                                                                                                 CodeBlock.of("$L.$L()",
                                                                                                              entityVariableName, getter(attribute))));
                                                }
                                                for (int k = 2; k < embeddedAttributes.size(); k++) {
                                                    AttributeMetadata attribute = embeddedAttributes.get(k);
                                                    snippetBuilder
                                                            .add(statementMappingRenderer.render(attribute.getNativeType(),
                                                                                                 INDEX_VARIABLE_NAME + " + (" + uniqueize("size") + " * " + k + ")",
                                                                                                 CodeBlock.of("$L.$L()",
                                                                                                              entityVariableName, getter(attribute))));
                                                }

                                                snippetBuilder
                                                        .addStatement("$L++", INDEX_VARIABLE_NAME)
                                                        .endControlFlow();
                                                break;
                                            }

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

                                            parameterIndex = parameterIndexProvider.getParameterIndex();
                                            snippetBuilder
                                                    .add(statementMappingRenderer.render(parameterType, parameterIndex, parameterValue));
                                            break;
                                        default:
                                            if (entityMetadataRegistry.containsEmbeddableEntity(parameterType.toString())) {
                                                NumericParameterIndexProvider clonedParameterIndexProvider;
                                                if (parameterIndexProvider instanceof NumericParameterIndexProvider) {
                                                    clonedParameterIndexProvider =
                                                            (NumericParameterIndexProvider) parameterIndexProvider;
                                                }
                                                else {
                                                    clonedParameterIndexProvider = new NumericParameterIndexProvider();
                                                }

                                                EmbeddableEntityMetadata embeddableEntity =
                                                        entityMetadataRegistry.getEmbeddableEntity(parameterType.toString());
                                                List<AttributeMetadata> embeddedAttributes = embeddableEntity.getAttributes();
                                                for (AttributeMetadata attribute : embeddedAttributes) {
                                                    snippetBuilder
                                                            .add(attributeToStatementMappingRenderer.renderAttribute(parameterName, attribute, clonedParameterIndexProvider));
                                                }
                                                break;
                                            }

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

                                            parameterIndex = parameterIndexProvider.getParameterIndex();
                                            snippetBuilder
                                                    .add(statementMappingRenderer.render(parameterType, parameterIndex, parameterValue));
                                            break;
                                    }
                                    break;
                                default:
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

                                    parameterIndex = parameterIndexProvider.getParameterIndex();
                                    snippetBuilder
                                            .add(statementMappingRenderer.render(parameterType, parameterIndex, parameterValue));
                                    break;
                            }
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
