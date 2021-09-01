package io.graphine.processor.code.renderer;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.index.ParameterIndexProvider;
import io.graphine.processor.code.renderer.mapping.ResultSetMappingRenderer;
import io.graphine.processor.metadata.model.entity.EmbeddableEntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.EmbeddedAttribute;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;

import java.util.Collection;

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
        if (attribute instanceof EmbeddedAttribute) {
            snippetBuilder
                    .add(renderEmbeddedAttribute(rootVariableName, (EmbeddedAttribute) attribute, parameterIndexProvider));
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
                                              EmbeddedAttribute attribute,
                                              ParameterIndexProvider parameterIndexProvider) {
        CodeBlock.Builder snippetBuilder = CodeBlock.builder();

        EmbeddableEntityMetadata embeddableEntity =
                entityMetadataRegistry.getEmbeddableEntity(attribute.getNativeType().toString());
        String variableName = uniqueize(attribute.getName());

        snippetBuilder
                .addStatement("$T $L = new $T()",
                              embeddableEntity.getNativeType(),
                              variableName,
                              embeddableEntity.getNativeType());

        Collection<AttributeMetadata> embeddedAttributes = embeddableEntity.getAttributes();
        for (AttributeMetadata embeddedAttribute : embeddedAttributes) {
            snippetBuilder
                    .add(renderAttribute(variableName, embeddedAttribute, parameterIndexProvider));
        }

        snippetBuilder
                .addStatement("$L.$L($L)",
                              rootVariableName,
                              setter(attribute),
                              variableName);

        return snippetBuilder.build();
    }
}
