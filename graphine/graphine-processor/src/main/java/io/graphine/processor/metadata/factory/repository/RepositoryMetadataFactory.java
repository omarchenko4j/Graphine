package io.graphine.processor.metadata.factory.repository;

import io.graphine.processor.metadata.model.repository.RepositoryMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;

import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.stream.Collectors;

import static io.graphine.processor.util.GraphineAnnotationUtils.getRepositoryAnnotationValueAttributeValue;
import static javax.lang.model.util.ElementFilter.methodsIn;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryMetadataFactory {
    private final MethodMetadataFactory methodMetadataFactory;

    public RepositoryMetadataFactory(MethodMetadataFactory methodMetadataFactory) {
        this.methodMetadataFactory = methodMetadataFactory;
    }

    public RepositoryMetadata createRepository(TypeElement repositoryElement) {
        TypeElement entityElement = getRepositoryAnnotationValueAttributeValue(repositoryElement);
        String entityQualifiedName = entityElement.getQualifiedName().toString();

        List<MethodMetadata> methods = methodsIn(repositoryElement.getEnclosedElements())
                .stream()
                .filter(methodElement -> !methodElement.isDefault()) // Default methods will not be implemented!
                .map(methodMetadataFactory::createMethod)
                .collect(Collectors.toList());
        return new RepositoryMetadata(repositoryElement, entityQualifiedName, methods);
    }
}
