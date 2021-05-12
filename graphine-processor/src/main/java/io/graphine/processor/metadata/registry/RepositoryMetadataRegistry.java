package io.graphine.processor.metadata.registry;

import io.graphine.processor.metadata.model.repository.RepositoryMetadata;

import java.util.Map;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryMetadataRegistry extends BasicMetadataRegistry<RepositoryMetadata> {
    public RepositoryMetadataRegistry(Map<String, RepositoryMetadata> registry) {
        super(registry);
    }
}
