package io.graphine.processor.metadata.validator.repository.method;

import io.graphine.processor.metadata.registry.EntityMetadataRegistry;

/**
 * @author Oleg Marchenko
 */
public final class RepositorySaveMethodMetadataValidator extends RepositoryModifyingMethodMetadataValidator {
    public RepositorySaveMethodMetadataValidator(EntityMetadataRegistry entityMetadataRegistry) {
        super(entityMetadataRegistry);
    }
}
