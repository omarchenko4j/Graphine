package io.graphine.processor.code.collector;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.RepositoryMetadata;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Oleg Marchenko
 */
public final class OriginatingElementDependencyCollector {
    private final EntityMetadataRegistry entityMetadataRegistry;

    public OriginatingElementDependencyCollector(EntityMetadataRegistry entityMetadataRegistry) {
        this.entityMetadataRegistry = entityMetadataRegistry;
    }

    public Collection<Element> collect(RepositoryMetadata repository) {
        TypeElement repositoryElement = repository.getNativeElement();

        // Entity is a dependency of the repository implementation.
        // It positively affects on incremental build!
        EntityMetadata entity = entityMetadataRegistry.get(repository.getEntityQualifiedName());
        TypeElement entityElement = entity.getNativeElement();

        return Arrays.asList(repositoryElement, entityElement);
    }
}
