package io.graphine.processor.metadata.factory.repository;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.RepositoryMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;

import javax.lang.model.element.TypeElement;
import java.util.List;

import static io.graphine.processor.util.RepositoryAnnotationUtils.getEntityElementFromRepositoryAnnotation;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.util.ElementFilter.methodsIn;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryMetadataFactory {
    private final EntityMetadataRegistry entityMetadataRegistry;
    private final MethodMetadataFactory methodMetadataFactory;

    public RepositoryMetadataFactory(EntityMetadataRegistry entityMetadataRegistry,
                                     MethodMetadataFactory methodMetadataFactory) {
        this.entityMetadataRegistry = entityMetadataRegistry;
        this.methodMetadataFactory = methodMetadataFactory;
    }

    public RepositoryMetadata createRepository(TypeElement repositoryElement) {
        TypeElement entityElement = getEntityElementFromRepositoryAnnotation(repositoryElement);
        EntityMetadata entity = entityMetadataRegistry.getEntity(entityElement.getQualifiedName().toString());

        List<MethodMetadata> methods = methodsIn(repositoryElement.getEnclosedElements())
                .stream()
                .filter(methodElement -> !methodElement.isDefault()) // Default methods will not be implemented!
                .map(methodMetadataFactory::createMethod)
                .collect(toList());
        return new RepositoryMetadata(repositoryElement, entity, methods);
    }
}
