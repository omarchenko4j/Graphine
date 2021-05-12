package io.graphine.processor.metadata.registry;

import io.graphine.processor.metadata.model.BasicMetadata;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableCollection;

/**
 * @author Oleg Marchenko
 */
public abstract class BasicMetadataRegistry<M extends BasicMetadata> {
    protected final Map<String, M> registry;

    protected BasicMetadataRegistry(Collection<M> elements) {
        this.registry = elements.stream()
                                .collect(Collectors.toMap(BasicMetadata::getQualifiedName, Function.identity()));
    }

    public Collection<M> getAll() {
        return unmodifiableCollection(registry.values());
    }

    public M get(String qualifiedName) {
        return registry.get(qualifiedName);
    }
}
