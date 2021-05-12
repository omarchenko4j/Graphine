package io.graphine.processor.metadata.model.repository;

import io.graphine.processor.metadata.model.BasicMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;

import javax.lang.model.element.TypeElement;
import java.util.List;

import static io.graphine.processor.util.StringUtils.getIfNotEmpty;
import static java.util.Collections.unmodifiableList;

/**
 * @author Oleg Marchenko
 */
public class RepositoryMetadata extends BasicMetadata {
    private final String entityQualifiedName;
    private final List<MethodMetadata> methods;

    public RepositoryMetadata(TypeElement element,
                              String entityQualifiedName,
                              List<MethodMetadata> methods) {
        super(element);
        this.entityQualifiedName = entityQualifiedName;
        this.methods = methods;
    }

    public String getEntityQualifiedName() {
        return entityQualifiedName;
    }

    public List<MethodMetadata> getMethods() {
        return unmodifiableList(methods);
    }

    @Override
    public String toString() {
        return qualifiedName + getIfNotEmpty(entityQualifiedName, () -> " [" + entityQualifiedName + ']');
    }
}
