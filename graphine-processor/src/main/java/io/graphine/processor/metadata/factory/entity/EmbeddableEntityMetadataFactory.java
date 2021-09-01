package io.graphine.processor.metadata.factory.entity;

import io.graphine.processor.metadata.model.entity.EmbeddableEntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.support.AttributeDetectionStrategy;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.lang.model.util.ElementFilter.fieldsIn;

/**
 * @author Oleg Marchenko
 */
public final class EmbeddableEntityMetadataFactory {
    private final AttributeMetadataFactory attributeMetadataFactory;

    public EmbeddableEntityMetadataFactory(AttributeMetadataFactory attributeMetadataFactory) {
        this.attributeMetadataFactory = attributeMetadataFactory;
    }

    public EmbeddableEntityMetadata createEmbeddedEntity(TypeElement embeddableEntityElement) {
        Stream<VariableElement> fields = fieldsIn(embeddableEntityElement.getEnclosedElements()).stream();

        if (AttributeDetectionStrategy.onlyAnnotatedFields()) {
            fields = fields.filter(AttributeMetadata::isAttribute);
        }

        List<AttributeMetadata> attributes = fields
                .map(attributeMetadataFactory::createAttribute)
                .collect(Collectors.toList());

        return new EmbeddableEntityMetadata(embeddableEntityElement, attributes);
    }
}
