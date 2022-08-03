package io.graphine.processor.code.renderer;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.index.NumericParameterIndexProvider;
import io.graphine.processor.code.renderer.index.ParameterIndexProvider;
import io.graphine.processor.code.renderer.mapping.StatementMappingRenderer;
import io.graphine.processor.metadata.model.entity.EmbeddableEntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMapperMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.EmbeddedAttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.EmbeddedIdentifierAttributeMetadata;
import io.graphine.processor.metadata.registry.AttributeMapperMetadataRegistry;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;

import java.util.List;

import static io.graphine.processor.code.generator.repository.method.RepositoryMethodImplementationGenerator.STATEMENT_VARIABLE_NAME;
import static io.graphine.processor.support.EnvironmentContext.typeUtils;
import static io.graphine.processor.util.AccessorUtils.getter;
import static io.graphine.processor.util.StringUtils.EMPTY;
import static io.graphine.processor.util.StringUtils.isNotEmpty;
import static io.graphine.processor.util.VariableNameUniqueizer.uniqueize;

/**
 * @author Oleg Marchenko
 */
public final class AttributeToStatementMappingRenderer {
    private final EntityMetadataRegistry entityMetadataRegistry;
    private final AttributeMapperMetadataRegistry attributeMapperMetadataRegistry;
    private final StatementMappingRenderer statementMappingRenderer;

    public AttributeToStatementMappingRenderer(EntityMetadataRegistry entityMetadataRegistry,
                                               AttributeMapperMetadataRegistry attributeMapperMetadataRegistry,
                                               StatementMappingRenderer statementMappingRenderer) {
        this.entityMetadataRegistry = entityMetadataRegistry;
        this.attributeMapperMetadataRegistry = attributeMapperMetadataRegistry;
        this.statementMappingRenderer = statementMappingRenderer;
    }

    public CodeBlock renderAttribute(String rootVariableName,
                                     AttributeMetadata attribute,
                                     ParameterIndexProvider parameterIndexProvider) {
        CodeBlock.Builder snippetBuilder = CodeBlock.builder();

        String attributeMapperName = attribute.getMapper();
        if (isNotEmpty(attributeMapperName)) {
            AttributeMapperMetadata attributeMapper =
                    attributeMapperMetadataRegistry.getAttributeMapper(attributeMapperName);
            String parameterIndex = parameterIndexProvider.getParameterIndex();
            snippetBuilder
                    .addStatement("$T.$L($L, $L, $L.$L())",
                                  attributeMapper.getNativeType(),
                                  attributeMapper.getSetterMethodName(),
                                  STATEMENT_VARIABLE_NAME,
                                  parameterIndex,
                                  rootVariableName,
                                  getter(attribute));
        }
        else if (attribute instanceof EmbeddedIdentifierAttributeMetadata) {
            EmbeddedIdentifierAttributeMetadata embeddedIdentifierAttribute = (EmbeddedIdentifierAttributeMetadata) attribute;
            snippetBuilder
                    .add(renderEmbeddedAttribute(rootVariableName, embeddedIdentifierAttribute.getEmbeddedAttribute(), parameterIndexProvider));
        }
        else if (attribute instanceof EmbeddedAttributeMetadata) {
            EmbeddedAttributeMetadata embeddedAttribute = (EmbeddedAttributeMetadata) attribute;
            snippetBuilder
                    .add(renderEmbeddedAttribute(rootVariableName, embeddedAttribute, parameterIndexProvider));
        }
        else {
            String parameterIndex = parameterIndexProvider.getParameterIndex();
            snippetBuilder
                    .add(statementMappingRenderer.render(attribute.getNativeType(),
                                                         parameterIndex,
                                                         CodeBlock.of("$L.$L()",
                                                                      rootVariableName, getter(attribute))));
        }
        return snippetBuilder.build();
    }

    private CodeBlock renderEmbeddedAttribute(String rootVariableName,
                                              EmbeddedAttributeMetadata embeddedAttribute,
                                              ParameterIndexProvider parameterIndexProvider) {
        CodeBlock.Builder snippetBuilder = CodeBlock.builder();

        String variableName = uniqueize(embeddedAttribute.getName());
        snippetBuilder
                .addStatement("$T $L = $L.$L()",
                              embeddedAttribute.getNativeType(),
                              variableName,
                              rootVariableName,
                              getter(embeddedAttribute));

        ParameterIndexProvider clonedParameterIndexProvider = parameterIndexProvider;
        if (parameterIndexProvider instanceof NumericParameterIndexProvider) {
            clonedParameterIndexProvider =
                    new NumericParameterIndexProvider((NumericParameterIndexProvider) parameterIndexProvider);
        }

        snippetBuilder
                .beginControlFlow("if ($L != null)", variableName);

        EmbeddableEntityMetadata embeddableEntity =
                entityMetadataRegistry.getEmbeddableEntity(embeddedAttribute.getNativeType().toString());
        List<AttributeMetadata> embeddedAttributes = embeddableEntity.getAttributes();
        for (AttributeMetadata attribute : embeddedAttributes) {
            snippetBuilder
                    .add(renderAttribute(variableName, attribute, parameterIndexProvider));
        }

        snippetBuilder
                .endControlFlow()
                .beginControlFlow("else");

        for (AttributeMetadata attribute : embeddedAttributes) {
            snippetBuilder
                    .add(renderNullableAttribute(attribute, clonedParameterIndexProvider));
        }

        snippetBuilder
                .endControlFlow();

        return snippetBuilder.build();
    }

    private CodeBlock renderNullableAttribute(AttributeMetadata attribute,
                                              ParameterIndexProvider parameterIndexProvider) {
        CodeBlock.Builder snippetBuilder = CodeBlock.builder();

        if (attribute instanceof EmbeddedAttributeMetadata) {
            EmbeddableEntityMetadata embeddableEntity =
                    entityMetadataRegistry.getEmbeddableEntity(attribute.getNativeType().toString());
            List<AttributeMetadata> embeddedAttributes = embeddableEntity.getAttributes();
            for (AttributeMetadata embeddedAttribute : embeddedAttributes) {
                snippetBuilder
                        .add(renderNullableAttribute(embeddedAttribute, parameterIndexProvider));
            }
        }
        else {
            snippetBuilder
                    .add(statementMappingRenderer.render(typeUtils.getNullType(),
                                                         parameterIndexProvider.getParameterIndex(),
                                                         EMPTY));
        }

        return snippetBuilder.build();
    }
}
