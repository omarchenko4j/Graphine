package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.index.IncrementalParameterIndexProvider;
import io.graphine.processor.code.renderer.index.NumericParameterIndexProvider;
import io.graphine.processor.code.renderer.index.ParameterIndexProvider;
import io.graphine.processor.code.renderer.mapping.ResultSetMappingRenderer;
import io.graphine.processor.code.renderer.mapping.StatementMappingRenderer;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.IdentifierAttributeMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.model.repository.method.parameter.ParameterMetadata;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;
import io.graphine.processor.query.model.NativeQuery;

import java.sql.PreparedStatement;

import static io.graphine.processor.code.renderer.index.IncrementalParameterIndexProvider.INDEX_VARIABLE_NAME;
import static io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment.MethodForm.PLURAL;
import static io.graphine.processor.util.AccessorUtils.getter;
import static io.graphine.processor.util.StringUtils.uncapitalize;
import static io.graphine.processor.util.VariableNameUniqueizer.uniqueize;
import static java.util.Objects.isNull;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryDeleteMethodImplementationGenerator
        extends RepositoryModifyingMethodImplementationGenerator {
    public RepositoryDeleteMethodImplementationGenerator(EntityMetadataRegistry entityMetadataRegistry,
                                                         StatementMappingRenderer statementMappingRenderer,
                                                         ResultSetMappingRenderer resultSetMappingRenderer) {
        super(entityMetadataRegistry, statementMappingRenderer, resultSetMappingRenderer);
    }

    @Override
    protected CodeBlock renderStatement(MethodMetadata method, NativeQuery query, EntityMetadata entity) {
        return CodeBlock.builder()
                        .beginControlFlow("try ($T $L = $L.prepareStatement($L))",
                                          PreparedStatement.class,
                                          STATEMENT_VARIABLE_NAME,
                                          CONNECTION_VARIABLE_NAME,
                                          QUERY_VARIABLE_NAME)
                        .add(renderStatementParameters(method, query, entity))
                        .addStatement("$L.executeUpdate()", STATEMENT_VARIABLE_NAME)
                        .endControlFlow()
                        .build();
    }

    @Override
    protected CodeBlock renderStatementParameters(MethodMetadata method, NativeQuery query, EntityMetadata entity) {
        CodeBlock.Builder snippetBuilder = CodeBlock.builder();

        QueryableMethodName queryableName = method.getQueryableName();
        ConditionFragment condition = queryableName.getCondition();
        if (isNull(condition)) {
            ParameterMetadata parameter = method.getParameters().get(0);

            ParameterIndexProvider parameterIndexProvider;

            String entityVariableName;

            QualifierFragment qualifier = queryableName.getQualifier();
            if (qualifier.getMethodForm() == PLURAL) {
                parameterIndexProvider = new IncrementalParameterIndexProvider(INDEX_VARIABLE_NAME);

                snippetBuilder.addStatement("int $L = 1", INDEX_VARIABLE_NAME);

                entityVariableName = uniqueize(uncapitalize(entity.getName()));
                snippetBuilder
                        .beginControlFlow("for ($T $L : $L)",
                                          entity.getNativeType(), entityVariableName, parameter.getName());
            }
            else {
                parameterIndexProvider = new NumericParameterIndexProvider();

                entityVariableName = parameter.getName();
            }

            IdentifierAttributeMetadata identifierAttribute = entity.getIdentifier();
            snippetBuilder.add(
                    statementMappingRenderer.render(identifierAttribute.getNativeType(),
                                                    parameterIndexProvider.getParameterIndex(),
                                                    CodeBlock.of("$L.$L()",
                                                                 entityVariableName,
                                                                 getter(identifierAttribute)))
            );

            if (qualifier.getMethodForm() == PLURAL) {
                snippetBuilder.endControlFlow();
            }
        }
        else {
            snippetBuilder.add(super.renderStatementParameters(method, query, entity));
        }
        return snippetBuilder.build();
    }
}
