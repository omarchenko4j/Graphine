package io.graphine.processor.code.generator.repository.method;

import io.graphine.processor.metadata.model.entity.EntityMetadata;

/**
 * @author Oleg Marchenko
 */
public final class RepositorySaveMethodImplementationGenerator extends RepositoryMethodImplementationGenerator {
    public RepositorySaveMethodImplementationGenerator(EntityMetadata entity) {
        super(entity);
    }
}
