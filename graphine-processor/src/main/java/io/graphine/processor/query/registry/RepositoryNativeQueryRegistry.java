package io.graphine.processor.query.registry;

import io.graphine.processor.metadata.model.repository.RepositoryMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.query.model.NativeQuery;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryNativeQueryRegistry {
    private final RepositoryMetadata repository;
    private final Map<MethodMetadata, NativeQuery> queryRegistry;

    public RepositoryNativeQueryRegistry(RepositoryMetadata repository) {
        this.repository = repository;
        this.queryRegistry = new LinkedHashMap<>(repository.getMethods().size() + 1, 1);
    }

    public RepositoryMetadata getRepository() {
        return repository;
    }

    public void registerQuery(MethodMetadata method, NativeQuery query) {
        queryRegistry.put(method, query);
    }

    public NativeQuery getQuery(MethodMetadata method) {
        return queryRegistry.get(method);
    }
}
