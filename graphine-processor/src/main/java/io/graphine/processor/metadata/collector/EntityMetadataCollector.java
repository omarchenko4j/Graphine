package io.graphine.processor.metadata.collector;

import io.graphine.core.annotation.Entity;
import io.graphine.processor.metadata.EntityMetadata;
import io.graphine.processor.metadata.factory.EntityMetadataFactory;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Oleg Marchenko
 */
public final class EntityMetadataCollector {
    private final EntityMetadataFactory entityMetadataFactory;

    public EntityMetadataCollector(EntityMetadataFactory entityMetadataFactory) {
        this.entityMetadataFactory = entityMetadataFactory;
    }

    public EntityMetadataRegistry collect(RoundEnvironment environment) {
        List<EntityMetadata> entities =
                environment.getElementsAnnotatedWith(Entity.class)
                           .stream()
                           .map(element -> (TypeElement) element)
                           .map(entityMetadataFactory::createEntity)
                           .collect(toList());
        return new EntityMetadataRegistry(entities);
    }
}
