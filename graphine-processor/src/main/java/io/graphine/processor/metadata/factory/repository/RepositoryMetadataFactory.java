package io.graphine.processor.metadata.factory.repository;

import io.graphine.core.GraphineRepository;
import io.graphine.processor.metadata.model.repository.RepositoryMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.List;

import static java.util.stream.Collectors.toList;
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
        List<MethodMetadata> methods = methodsIn(repositoryElement.getEnclosedElements())
                .stream()
                .filter(methodElement -> !methodElement.isDefault()) // Default methods will not be implemented!
                .map(methodMetadataFactory::createMethod)
                .collect(toList());
        return new RepositoryMetadata(repositoryElement, getEntityQualifiedName(repositoryElement), methods);
    }

    private String getEntityQualifiedName(TypeElement repositoryElement) {
        return repositoryElement.getInterfaces()
                                .stream()
                                .map(interfaceType -> (DeclaredType) interfaceType)
                                .filter(interfaceType ->
                                                ((TypeElement) interfaceType.asElement()).getQualifiedName()
                                                                                         .contentEquals(GraphineRepository.class.getName()))
                                .flatMap(interfaceType -> interfaceType.getTypeArguments()
                                                                       .stream()
                                                                       .map(typeArgument -> (DeclaredType) typeArgument))
                                .map(typeArgument -> ((TypeElement) typeArgument.asElement()).getQualifiedName().toString())
                                .findFirst()
                                .orElse(null);
    }
}
