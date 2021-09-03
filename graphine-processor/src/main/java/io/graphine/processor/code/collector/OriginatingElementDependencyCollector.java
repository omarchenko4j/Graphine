package io.graphine.processor.code.collector;

import io.graphine.processor.metadata.model.entity.EmbeddableEntityMetadata;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.EmbeddedAttribute;
import io.graphine.processor.metadata.model.repository.RepositoryMetadata;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Oleg Marchenko
 */
public final class OriginatingElementDependencyCollector {
    private final EntityMetadataRegistry entityMetadataRegistry;

    public OriginatingElementDependencyCollector(EntityMetadataRegistry entityMetadataRegistry) {
        this.entityMetadataRegistry = entityMetadataRegistry;
    }

    public Collection<Element> collect(RepositoryMetadata repository) {
        List<Element> originatingElements = new ArrayList<>();
        originatingElements.add(repository.getNativeElement());

        // Entity and all internal embeddable entities are dependencies for the repository implementation.
        // It positively affects on incremental build!
        EntityMetadata entity = entityMetadataRegistry.getEntity(repository.getEntityQualifiedName());
        originatingElements.addAll(collect(entity));

        return originatingElements;
    }

    public Collection<Element> collect(EntityMetadata entity) {
        Collection<AttributeMetadata> attributes = entity.getAttributes();

        List<Element> originatingElements = new ArrayList<>(attributes.size());
        originatingElements.add(entity.getNativeElement());
        originatingElements.addAll(collect(attributes));
        return originatingElements;
    }

    public Collection<Element> collect(EmbeddableEntityMetadata embeddableEntity) {
        Collection<AttributeMetadata> attributes = embeddableEntity.getAttributes();

        List<Element> originatingElements = new ArrayList<>(attributes.size());
        originatingElements.add(embeddableEntity.getNativeElement());
        originatingElements.addAll(collect(attributes));
        return originatingElements;
    }

    private Collection<Element> collect(Collection<AttributeMetadata> attributes) {
        List<Element> originatingElements = new ArrayList<>(attributes.size());
        for (AttributeMetadata attribute : attributes) {
            if (attribute instanceof EmbeddedAttribute) {
                EmbeddableEntityMetadata embeddableEntity =
                        entityMetadataRegistry.getEmbeddableEntity(attribute.getNativeType().toString());
                originatingElements.addAll(collect(embeddableEntity));
            }
        }
        return originatingElements;
    }
}
