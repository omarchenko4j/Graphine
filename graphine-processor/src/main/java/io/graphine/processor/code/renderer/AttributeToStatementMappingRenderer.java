package io.graphine.processor.code.renderer;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.index.NumericParameterIndexProvider;
import io.graphine.processor.code.renderer.mapping.StatementMappingRenderer;
import io.graphine.processor.metadata.model.entity.EmbeddableEntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.EmbeddedAttribute;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;

import java.util.Collection;

import static io.graphine.processor.support.EnvironmentContext.typeUtils;
import static io.graphine.processor.util.AccessorUtils.getter;
import static io.graphine.processor.util.VariableNameUniqueizer.uniqueize;

/**
 * @author Oleg Marchenko
 */
public final class AttributeToStatementMappingRenderer {
    private final EntityMetadataRegistry entityMetadataRegistry;
    private final StatementMappingRenderer statementMappingRenderer;

    public AttributeToStatementMappingRenderer(EntityMetadataRegistry entityMetadataRegistry,
                                               StatementMappingRenderer statementMappingRenderer) {
        this.entityMetadataRegistry = entityMetadataRegistry;
        this.statementMappingRenderer = statementMappingRenderer;
    }

    public CodeBlock renderAttribute(String rootVariableName,
                                     AttributeMetadata attribute,
                                     NumericParameterIndexProvider parameterIndexProvider) {
        CodeBlock.Builder snippetBuilder = CodeBlock.builder();

        if (attribute instanceof EmbeddedAttribute) {
            EmbeddedAttribute embeddedAttribute = (EmbeddedAttribute) attribute;
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
                                              EmbeddedAttribute attribute,
                                              NumericParameterIndexProvider parameterIndexProvider) {
        CodeBlock.Builder snippetBuilder = CodeBlock.builder();

        String variableName = uniqueize(attribute.getName());
        snippetBuilder
                .addStatement("$T $L = $L.$L()",
                              attribute.getNativeType(),
                              variableName,
                              rootVariableName,
                              getter(attribute));

        NumericParameterIndexProvider clonedParameterIndexProvider =
                new NumericParameterIndexProvider(parameterIndexProvider);


        snippetBuilder
                .beginControlFlow("if ($L != null)", variableName);

        EmbeddableEntityMetadata embeddableEntity =
                entityMetadataRegistry.getEmbeddableEntity(attribute.getNativeType().toString());
        Collection<AttributeMetadata> embeddedAttributes = embeddableEntity.getAttributes();
        for (AttributeMetadata embeddedAttribute : embeddedAttributes) {
            snippetBuilder
                    .add(renderAttribute(variableName, embeddedAttribute, parameterIndexProvider));
        }

        snippetBuilder
                .endControlFlow()
                .beginControlFlow("else");

        for (AttributeMetadata embeddedAttribute : embeddedAttributes) {
            snippetBuilder
                    .add(renderNullableAttribute(embeddedAttribute, clonedParameterIndexProvider));
        }

        snippetBuilder
                .endControlFlow();

        return snippetBuilder.build();
    }

    private CodeBlock renderNullableAttribute(AttributeMetadata attribute,
                                              NumericParameterIndexProvider parameterIndexProvider) {
        CodeBlock.Builder snippetBuilder = CodeBlock.builder();

        if (attribute instanceof EmbeddedAttribute) {
            EmbeddableEntityMetadata embeddableEntity =
                    entityMetadataRegistry.getEmbeddableEntity(attribute.getNativeType().toString());
            Collection<AttributeMetadata> embeddedAttributes = embeddableEntity.getAttributes();
            for (AttributeMetadata embeddedAttribute : embeddedAttributes) {
                snippetBuilder
                        .add(renderNullableAttribute(embeddedAttribute, parameterIndexProvider));
            }
        }
        else {
            snippetBuilder
                    .add(statementMappingRenderer.render(typeUtils.getNullType(),
                                                         parameterIndexProvider.getParameterIndex(),
                                                         CodeBlock.of("")));
        }

        return snippetBuilder.build();
    }
}
