package io.graphine.processor.query.generator;

import io.graphine.processor.metadata.model.repository.RepositoryMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.query.registry.RepositoryNativeQueryRegistry;

import java.util.List;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryNativeQueryGenerator {
    public RepositoryNativeQueryRegistry generate(RepositoryMetadata repository) {
        List<MethodMetadata> methods = repository.getMethods();
        for (MethodMetadata method : methods) {
            // TODO: add generation of native query for a specific method
        }
        return new RepositoryNativeQueryRegistry(repository);
    }
}
