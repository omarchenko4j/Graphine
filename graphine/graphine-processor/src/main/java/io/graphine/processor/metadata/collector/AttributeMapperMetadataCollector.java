package io.graphine.processor.metadata.collector;

import io.graphine.annotation.AttributeMapper;
import io.graphine.processor.metadata.factory.entity.AttributeMapperMetadataFactory;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMapperMetadata;
import io.graphine.processor.metadata.registry.AttributeMapperMetadataRegistry;
import io.graphine.processor.metadata.validator.entity.AttributeMapperElementValidator;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Oleg Marchenko
 */
public final class AttributeMapperMetadataCollector {
    private final AttributeMapperElementValidator attributeMapperElementValidator;
    private final AttributeMapperMetadataFactory attributeMapperMetadataFactory;

    public AttributeMapperMetadataCollector(AttributeMapperElementValidator attributeMapperElementValidator,
                                            AttributeMapperMetadataFactory attributeMapperMetadataFactory) {
        this.attributeMapperElementValidator = attributeMapperElementValidator;
        this.attributeMapperMetadataFactory = attributeMapperMetadataFactory;
    }

    public AttributeMapperMetadataRegistry collect(RoundEnvironment environment) {
        Set<? extends Element> elements = environment.getElementsAnnotatedWith(AttributeMapper.class);

        Map<String, AttributeMapperMetadata> attributeMapperRegistry = new HashMap<>(elements.size() + 1, 1);
        for (Element element : elements) {
            TypeElement attributeMapperElement = (TypeElement) element;
            if (attributeMapperElementValidator.validate(attributeMapperElement)) {
                AttributeMapperMetadata attributeMapper =
                        attributeMapperMetadataFactory.create(attributeMapperElement);
                attributeMapperRegistry.put(attributeMapper.getQualifiedName(), attributeMapper);
            }
            else {
                // Register invalid attribute mapper without creating metadata.
                // This affects the output of correct errors when validating the entity.
                String qualifiedName = attributeMapperElement.getQualifiedName().toString();
                attributeMapperRegistry.put(qualifiedName, null);
            }
        }
        return new AttributeMapperMetadataRegistry(attributeMapperRegistry);
    }
}
