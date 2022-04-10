package io.graphine.processor.code.renderer;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.index.ParameterIndexProvider;
import io.graphine.processor.code.renderer.mapping.ResultSetMappingRenderer;
import io.graphine.processor.metadata.model.entity.EmbeddableEntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.EmbeddedAttributeMetadata;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;

import java.util.List;

import static io.graphine.processor.util.AccessorUtils.setter;
import static io.graphine.processor.util.VariableNameUniqueizer.uniqueize;

/**
 * @author Oleg Marchenko
 */
public final class AttributeFromResultSetMappingRenderer {
    private final EntityMetadataRegistry entityMetadataRegistry;
    private final ResultSetMappingRenderer resultSetMappingRenderer;

    public AttributeFromResultSetMappingRenderer(EntityMetadataRegistry entityMetadataRegistry,
                                                 ResultSetMappingRenderer resultSetMappingRenderer) {
        this.entityMetadataRegistry = entityMetadataRegistry;
        this.resultSetMappingRenderer = resultSetMappingRenderer;
    }

    public CodeBlock renderAttribute(String rootVariableName,
                                     AttributeMetadata attribute,
                                     ParameterIndexProvider parameterIndexProvider) {
        CodeBlock.Builder snippetBuilder = CodeBlock.builder();
        if (attribute instanceof EmbeddedAttributeMetadata) {
            EmbeddedAttributeMetadata embeddedAttribute = (EmbeddedAttributeMetadata) attribute;
            snippetBuilder
                    .add(renderEmbeddedAttribute(rootVariableName, embeddedAttribute, parameterIndexProvider));
        }
        else {
            String parameterIndex = parameterIndexProvider.getParameterIndex();
            snippetBuilder
                    .addStatement("$L.$L($L)",
                                  rootVariableName,
                                  setter(attribute),
                                  resultSetMappingRenderer.render(attribute.getNativeType(), parameterIndex));
        }
        return snippetBuilder.build();
    }

    private CodeBlock renderEmbeddedAttribute(String rootVariableName,
                                              EmbeddedAttributeMetadata embeddedAttribute,
                                              ParameterIndexProvider parameterIndexProvider) {
        CodeBlock.Builder snippetBuilder = CodeBlock.builder();

        EmbeddableEntityMetadata embeddableEntity =
                entityMetadataRegistry.getEmbeddableEntity(embeddedAttribute.getNativeType().toString());
        String variableName = uniqueize(embeddedAttribute.getName());

        snippetBuilder
                .addStatement("$T $L = new $T()",
                              embeddableEntity.getNativeType(),
                              variableName,
                              embeddableEntity.getNativeType());

        List<AttributeMetadata> embeddedAttributes = embeddableEntity.getAttributes();
        for (AttributeMetadata attribute : embeddedAttributes) {
            snippetBuilder
                    .add(renderAttribute(variableName, attribute, parameterIndexProvider));
        }

        snippetBuilder
                .addStatement("$L.$L($L)",
                              rootVariableName,
                              setter(embeddedAttribute),
                              variableName);

        return snippetBuilder.build();
    }
}
