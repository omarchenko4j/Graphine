package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.index.NumericParameterIndexProvider;
import io.graphine.processor.code.renderer.mapping.ResultSetMappingRenderer;
import io.graphine.processor.code.renderer.mapping.StatementMappingRenderer;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.IdentifierMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.model.repository.method.parameter.ParameterMetadata;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;
import io.graphine.processor.query.model.NativeQuery;

import java.util.Collection;

import static io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment.MethodForm.PLURAL;
import static io.graphine.processor.util.AccessorUtils.getter;
import static io.graphine.processor.util.StringUtils.uncapitalize;
import static io.graphine.processor.util.VariableNameUniqueizer.uniqueize;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryUpdateMethodImplementationGenerator
        extends RepositoryModifyingMethodImplementationGenerator {
    public RepositoryUpdateMethodImplementationGenerator(EntityMetadataRegistry entityMetadataRegistry,
                                                         StatementMappingRenderer statementMappingRenderer,
                                                         ResultSetMappingRenderer resultSetMappingRenderer) {
        super(entityMetadataRegistry, statementMappingRenderer, resultSetMappingRenderer);
    }

    @Override
    protected CodeBlock renderQuery(MethodMetadata method, NativeQuery query) {
        return CodeBlock.builder()
                        .addStatement("String $L = $S", QUERY_VARIABLE_NAME, query.getValue())
                        .build();
    }

    @Override
    protected CodeBlock renderStatementParameters(MethodMetadata method, NativeQuery query, EntityMetadata entity) {
        CodeBlock.Builder snippetBuilder = CodeBlock.builder();

        ParameterMetadata parameter = method.getParameters().get(0);

        String rootVariableName;

        QueryableMethodName queryableName = method.getQueryableName();
        QualifierFragment qualifier = queryableName.getQualifier();
        if (qualifier.getMethodForm() == PLURAL) {
            rootVariableName = uniqueize(uncapitalize(entity.getName()));
            snippetBuilder
                    .beginControlFlow("for ($T $L : $L)",
                                      entity.getNativeType(), rootVariableName, parameter.getName());
        }
        else {
            rootVariableName = parameter.getName();
        }

        NumericParameterIndexProvider parameterIndexProvider = new NumericParameterIndexProvider();

        Collection<AttributeMetadata> attributes = entity.getAttributes(true);
        for (AttributeMetadata attribute : attributes) {
            snippetBuilder
                    .add(attributeToStatementMappingRenderer.renderAttribute(rootVariableName,
                                                                             attribute,
                                                                             parameterIndexProvider));
        }

        IdentifierMetadata identifier = entity.getIdentifier();
        snippetBuilder.add(
                statementMappingRenderer.render(identifier.getNativeType(),
                                                parameterIndexProvider.getParameterIndex(),
                                                CodeBlock.of("$L.$L()",
                                                             rootVariableName, getter(identifier)))
        );

        if (qualifier.getMethodForm() == PLURAL) {
            snippetBuilder
                    .addStatement("$L.addBatch()", STATEMENT_VARIABLE_NAME)
                    .endControlFlow()
                    .addStatement("$L.executeBatch()", STATEMENT_VARIABLE_NAME);
        }
        else {
            snippetBuilder
                    .addStatement("$L.executeUpdate()", STATEMENT_VARIABLE_NAME);
        }

        return snippetBuilder.build();
    }

    @Override
    protected CodeBlock renderResultSet(MethodMetadata method, NativeQuery query, EntityMetadata entity) {
        return CodeBlock.builder().build();
    }
}
