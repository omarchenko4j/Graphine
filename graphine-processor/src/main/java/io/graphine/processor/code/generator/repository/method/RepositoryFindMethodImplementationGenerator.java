package io.graphine.processor.code.generator.repository.method;

import io.graphine.processor.metadata.model.entity.EntityMetadata;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryFindMethodImplementationGenerator extends RepositoryMethodImplementationGenerator {
    public RepositoryFindMethodImplementationGenerator(EntityMetadata entity) {
        super(entity);
    }
}
