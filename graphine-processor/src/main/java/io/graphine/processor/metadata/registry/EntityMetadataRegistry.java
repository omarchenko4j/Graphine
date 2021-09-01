package io.graphine.processor.metadata.registry;

import io.graphine.processor.metadata.model.entity.EmbeddableEntityMetadata;
import io.graphine.processor.metadata.model.entity.EntityMetadata;

import java.util.Collection;
import java.util.Map;

import static java.util.Collections.unmodifiableCollection;

/**
 * @author Oleg Marchenko
 */
public final class EntityMetadataRegistry {
    private final Map<String, EntityMetadata> entityRegistry;
    private final Map<String, EmbeddableEntityMetadata> embeddableEntityRegistry;

    public EntityMetadataRegistry(Map<String, EntityMetadata> entityRegistry,
                                  Map<String, EmbeddableEntityMetadata> embeddableEntityRegistry) {
        this.entityRegistry = entityRegistry;
        this.embeddableEntityRegistry = embeddableEntityRegistry;
    }

    public Collection<EntityMetadata> getEntities() {
        return unmodifiableCollection(entityRegistry.values());
    }

    public EntityMetadata getEntity(String qualifiedName) {
        return entityRegistry.get(qualifiedName);
    }

    public boolean containsEntity(String qualifiedName) {
        return entityRegistry.containsKey(qualifiedName);
    }

    public Collection<EmbeddableEntityMetadata> getEmbeddableEntities() {
        return unmodifiableCollection(embeddableEntityRegistry.values());
    }

    public EmbeddableEntityMetadata getEmbeddableEntity(String qualifiedName) {
        return embeddableEntityRegistry.get(qualifiedName);
    }
}
