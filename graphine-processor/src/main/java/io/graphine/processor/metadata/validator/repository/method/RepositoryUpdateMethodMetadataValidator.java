package io.graphine.processor.metadata.validator.repository.method;

import io.graphine.processor.metadata.registry.EntityMetadataRegistry;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryUpdateMethodMetadataValidator extends RepositoryModifyingMethodMetadataValidator {
    public RepositoryUpdateMethodMetadataValidator(EntityMetadataRegistry entityMetadataRegistry) {
        super(entityMetadataRegistry);
    }
}
