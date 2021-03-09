package io.graphine.processor.code.generator.repository.method;

import io.graphine.processor.metadata.model.entity.EntityMetadata;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryUpdateMethodImplementationGenerator extends RepositoryMethodImplementationGenerator {
    public RepositoryUpdateMethodImplementationGenerator(EntityMetadata entity) {
        super(entity);
    }
}
