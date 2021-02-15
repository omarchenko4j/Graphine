package io.graphine.processor.metadata.collector;

import io.graphine.core.annotation.Repository;
import io.graphine.processor.metadata.factory.repository.RepositoryMetadataFactory;
import io.graphine.processor.metadata.model.repository.RepositoryMetadata;
import io.graphine.processor.metadata.registry.RepositoryMetadataRegistry;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryMetadataCollector {
    private final RepositoryMetadataFactory repositoryMetadataFactory;

    public RepositoryMetadataCollector(RepositoryMetadataFactory repositoryMetadataFactory) {
        this.repositoryMetadataFactory = repositoryMetadataFactory;
    }

    public RepositoryMetadataRegistry collect(RoundEnvironment environment) {
        List<RepositoryMetadata> repositories =
                environment.getElementsAnnotatedWith(Repository.class)
                           .stream()
                           .map(element -> (TypeElement) element)
                           .map(repositoryMetadataFactory::createRepository)
                           .collect(toList());
        return new RepositoryMetadataRegistry(repositories);
    }
}
