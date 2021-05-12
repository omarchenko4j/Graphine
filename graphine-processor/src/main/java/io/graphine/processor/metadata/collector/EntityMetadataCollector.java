package io.graphine.processor.metadata.collector;

import io.graphine.core.annotation.Entity;
import io.graphine.processor.metadata.factory.entity.EntityMetadataFactory;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;
import io.graphine.processor.metadata.validator.entity.EntityElementValidator;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Oleg Marchenko
 */
public final class EntityMetadataCollector {
    private final EntityElementValidator entityElementValidator;
    private final EntityMetadataFactory entityMetadataFactory;

    public EntityMetadataCollector(EntityElementValidator entityElementValidator,
                                   EntityMetadataFactory entityMetadataFactory) {
        this.entityElementValidator = entityElementValidator;
        this.entityMetadataFactory = entityMetadataFactory;
    }

    public EntityMetadataRegistry collect(RoundEnvironment environment) {
        Set<? extends Element> elements = environment.getElementsAnnotatedWith(Entity.class);

        Map<String, EntityMetadata> entityRegistry = new HashMap<>(elements.size() + 1, 1);

        for (Element element : elements) {
            TypeElement entityElement = (TypeElement) element;
            if (entityElementValidator.validate(entityElement)) {
                EntityMetadata entity = entityMetadataFactory.createEntity(entityElement);
                entityRegistry.put(entity.getQualifiedName(), entity);
            }
            else {
                // Register invalid entity without creating metadata.
                // This affects the output of correct errors when validating the repository.
                String qualifiedName = entityElement.getQualifiedName().toString();
                entityRegistry.put(qualifiedName, null);
            }
        }

        return new EntityMetadataRegistry(entityRegistry);
    }
}
