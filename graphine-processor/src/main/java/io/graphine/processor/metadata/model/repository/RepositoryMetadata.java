package io.graphine.processor.metadata.model.repository;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.support.element.NativeTypeElement;

import javax.lang.model.element.TypeElement;
import java.util.List;

import static io.graphine.processor.util.ObjectUtils.computeIf;
import static io.graphine.processor.util.StringUtils.EMPTY;
import static java.util.Collections.unmodifiableList;

/**
 * @author Oleg Marchenko
 */
public class RepositoryMetadata extends NativeTypeElement {
    private final EntityMetadata entity;
    private final List<MethodMetadata> methods;

    public RepositoryMetadata(TypeElement element,
                              EntityMetadata entity,
                              List<MethodMetadata> methods) {
        super(element);
        this.entity = entity;
        this.methods = methods;
    }

    public EntityMetadata getEntity() {
        return entity;
    }

    public List<MethodMetadata> getMethods() {
        return unmodifiableList(methods);
    }

    @Override
    public String toString() {
        return qualifiedName + computeIf(entity, () -> EMPTY, entity -> " [" + entity.getQualifiedName() + ']');
    }
}
