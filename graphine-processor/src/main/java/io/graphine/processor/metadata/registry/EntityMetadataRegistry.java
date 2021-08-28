package io.graphine.processor.metadata.registry;

import io.graphine.processor.metadata.model.entity.EntityMetadata;

import java.util.Collection;
import java.util.Map;

import static java.util.Collections.unmodifiableCollection;

/**
 * @author Oleg Marchenko
 */
public final class EntityMetadataRegistry {
    private final Map<String, EntityMetadata> entityRegistry;

    public EntityMetadataRegistry(Map<String, EntityMetadata> entityRegistry) {
        this.entityRegistry = entityRegistry;
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
}
