package io.graphine.processor.metadata.collector;

import io.graphine.annotation.Embeddable;
import io.graphine.annotation.Entity;
import io.graphine.processor.metadata.factory.entity.EmbeddableEntityMetadataFactory;
import io.graphine.processor.metadata.factory.entity.EntityMetadataFactory;
import io.graphine.processor.metadata.model.entity.EmbeddableEntityMetadata;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;
import io.graphine.processor.metadata.validator.entity.EmbeddableEntityElementValidator;
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

    private final EmbeddableEntityElementValidator embeddableEntityElementValidator;
    private final EmbeddableEntityMetadataFactory embeddableEntityMetadataFactory;

    public EntityMetadataCollector(EntityElementValidator entityElementValidator,
                                   EntityMetadataFactory entityMetadataFactory,
                                   EmbeddableEntityElementValidator embeddableEntityElementValidator,
                                   EmbeddableEntityMetadataFactory embeddableEntityMetadataFactory) {
        this.entityElementValidator = entityElementValidator;
        this.entityMetadataFactory = entityMetadataFactory;
        this.embeddableEntityElementValidator = embeddableEntityElementValidator;
        this.embeddableEntityMetadataFactory = embeddableEntityMetadataFactory;
    }

    public EntityMetadataRegistry collect(RoundEnvironment environment) {
        return new EntityMetadataRegistry(collectEntities(environment), collectEmbeddableEntities(environment));
    }

    private Map<String, EntityMetadata> collectEntities(RoundEnvironment environment) {
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
        return entityRegistry;
    }

    private Map<String, EmbeddableEntityMetadata> collectEmbeddableEntities(RoundEnvironment environment) {
        Set<? extends Element> elements = environment.getElementsAnnotatedWith(Embeddable.class);

        Map<String, EmbeddableEntityMetadata> embeddableEntityRegistry = new HashMap<>(elements.size() + 1, 1);
        for (Element element : elements) {
            TypeElement embeddableEntityElement = (TypeElement) element;
            if (embeddableEntityElementValidator.validate(embeddableEntityElement)) {
                EmbeddableEntityMetadata embeddedEntity =
                        embeddableEntityMetadataFactory.createEmbeddedEntity(embeddableEntityElement);
                embeddableEntityRegistry.put(embeddedEntity.getQualifiedName(), embeddedEntity);
            }
            else {
                // Register invalid embeddable entity without creating metadata.
                String qualifiedName = embeddableEntityElement.getQualifiedName().toString();
                embeddableEntityRegistry.put(qualifiedName, null);
            }
        }
        return embeddableEntityRegistry;
    }
}
