package io.graphine.processor.metadata.collector;

import io.graphine.core.annotation.Repository;
import io.graphine.processor.metadata.factory.repository.RepositoryMetadataFactory;
import io.graphine.processor.metadata.model.repository.RepositoryMetadata;
import io.graphine.processor.metadata.registry.RepositoryMetadataRegistry;
import io.graphine.processor.metadata.validator.repository.RepositoryElementValidator;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryMetadataCollector {
    private final RepositoryElementValidator repositoryElementValidator;
    private final RepositoryMetadataFactory repositoryMetadataFactory;

    public RepositoryMetadataCollector(RepositoryElementValidator repositoryElementValidator,
                                       RepositoryMetadataFactory repositoryMetadataFactory) {
        this.repositoryElementValidator = repositoryElementValidator;
        this.repositoryMetadataFactory = repositoryMetadataFactory;
    }

    public RepositoryMetadataRegistry collect(RoundEnvironment environment) {
        List<RepositoryMetadata> repositories =
                environment.getElementsAnnotatedWith(Repository.class)
                           .stream()
                           .map(element -> (TypeElement) element)
                           .filter(repositoryElementValidator::validate)
                           .map(repositoryMetadataFactory::createRepository)
                           .collect(Collectors.toList());
        return new RepositoryMetadataRegistry(repositories);
    }
}
