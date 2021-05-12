package io.graphine.processor.metadata.registry;

import io.graphine.processor.metadata.model.repository.RepositoryMetadata;

import java.util.Collection;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryMetadataRegistry extends BasicMetadataRegistry<RepositoryMetadata> {
    public RepositoryMetadataRegistry(Collection<RepositoryMetadata> repositories) {
        super(repositories);
    }
}
