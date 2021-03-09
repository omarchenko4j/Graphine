package io.graphine.processor.code.generator.repository.method;

import io.graphine.processor.metadata.model.entity.EntityMetadata;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryDeleteMethodImplementationGenerator extends RepositoryMethodImplementationGenerator {
    public RepositoryDeleteMethodImplementationGenerator(EntityMetadata entity) {
        super(entity);
    }
}
