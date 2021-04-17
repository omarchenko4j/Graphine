package io.graphine.processor.query.registry;

import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryNativeQueryRegistryStorage {
    private final List<RepositoryNativeQueryRegistry> registries;

    public RepositoryNativeQueryRegistryStorage(List<RepositoryNativeQueryRegistry> registries) {
        this.registries = registries;
    }

    public List<RepositoryNativeQueryRegistry> getRegistries() {
        return unmodifiableList(registries);
    }
}
