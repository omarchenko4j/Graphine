package io.graphine.processor.metadata.registry;

import io.graphine.processor.metadata.model.entity.EntityMetadata;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableCollection;
import static java.util.function.Function.identity;

/**
 * @author Oleg Marchenko
 */
public final class EntityMetadataRegistry {
    private final Map<String, EntityMetadata> entityRegistry;

    public EntityMetadataRegistry(Collection<EntityMetadata> entities) {
        this.entityRegistry = entities
                .stream()
                .collect(Collectors.toMap(EntityMetadata::getQualifiedName, identity()));
    }

    public Collection<EntityMetadata> getEntities() {
        return unmodifiableCollection(entityRegistry.values());
    }

    public EntityMetadata getEntity(String name) {
        return entityRegistry.get(name);
    }
}
