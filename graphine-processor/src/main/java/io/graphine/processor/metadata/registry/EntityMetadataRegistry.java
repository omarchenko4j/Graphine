package io.graphine.processor.metadata.registry;

import io.graphine.processor.metadata.model.entity.EntityMetadata;

import java.util.Map;

/**
 * @author Oleg Marchenko
 */
public final class EntityMetadataRegistry extends BasicMetadataRegistry<EntityMetadata> {
    public EntityMetadataRegistry(Map<String, EntityMetadata> registry) {
        super(registry);
    }
}
