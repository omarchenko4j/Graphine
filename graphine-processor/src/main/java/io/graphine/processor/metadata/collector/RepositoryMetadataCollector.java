package io.graphine.processor.metadata.collector;

import io.graphine.core.annotation.Repository;
import io.graphine.processor.metadata.factory.repository.RepositoryMetadataFactory;
import io.graphine.processor.metadata.model.repository.RepositoryMetadata;
import io.graphine.processor.metadata.registry.RepositoryMetadataRegistry;
import io.graphine.processor.metadata.validator.repository.RepositoryElementValidator;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.Map;
import java.util.function.Function;
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
        Map<String, RepositoryMetadata> repositoryRegistry =
                environment.getElementsAnnotatedWith(Repository.class)
                           .stream()
                           .map(element -> (TypeElement) element)
                           .filter(repositoryElementValidator::validate)
                           .map(repositoryMetadataFactory::createRepository)
                           .collect(Collectors.toMap(RepositoryMetadata::getQualifiedName, Function.identity()));
        return new RepositoryMetadataRegistry(repositoryRegistry);
    }
}
