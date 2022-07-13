package io.graphine.processor.metadata.registry;

import io.graphine.processor.metadata.model.repository.RepositoryMetadata;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableCollection;
import static java.util.function.Function.identity;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryMetadataRegistry {
    private final Map<String, RepositoryMetadata> repositoryRegistry;

    public RepositoryMetadataRegistry(Collection<RepositoryMetadata> repositories) {
        this.repositoryRegistry = repositories
                .stream()
                .collect(Collectors.toMap(RepositoryMetadata::getQualifiedName, identity()));
    }

    public Collection<RepositoryMetadata> getRepositories() {
        return unmodifiableCollection(repositoryRegistry.values());
    }
}
