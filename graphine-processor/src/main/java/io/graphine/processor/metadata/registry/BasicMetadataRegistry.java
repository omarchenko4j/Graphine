package io.graphine.processor.metadata.registry;

import io.graphine.processor.metadata.model.BasicMetadata;

import java.util.Collection;
import java.util.Map;

import static java.util.Collections.unmodifiableCollection;

/**
 * @author Oleg Marchenko
 */
public abstract class BasicMetadataRegistry<M extends BasicMetadata> {
    protected final Map<String, M> registry;

    protected BasicMetadataRegistry(Map<String, M> registry) {
        this.registry = registry;
    }

    public Collection<M> getAll() {
        return unmodifiableCollection(registry.values());
    }

    public M get(String qualifiedName) {
        return registry.get(qualifiedName);
    }

    public boolean exists(String qualifiedName) {
        return registry.containsKey(qualifiedName);
    }
}
