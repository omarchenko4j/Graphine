package io.graphine.processor.metadata.registry;

import io.graphine.processor.metadata.model.entity.EntityMetadata;

import java.util.Collection;

/**
 * @author Oleg Marchenko
 */
public final class EntityMetadataRegistry extends BasicMetadataRegistry<EntityMetadata> {
    public EntityMetadataRegistry(Collection<EntityMetadata> entities) {
        super(entities);
    }
}
