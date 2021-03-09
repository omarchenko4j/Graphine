package io.graphine.processor.code.generator.repository.method;

import io.graphine.processor.metadata.model.entity.EntityMetadata;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryCountMethodImplementationGenerator extends RepositoryMethodImplementationGenerator {
    public RepositoryCountMethodImplementationGenerator(EntityMetadata entity) {
        super(entity);
    }
}
