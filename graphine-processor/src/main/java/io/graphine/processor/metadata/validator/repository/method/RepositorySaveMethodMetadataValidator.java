package io.graphine.processor.metadata.validator.repository.method;

import io.graphine.processor.metadata.model.entity.EntityMetadata;

/**
 * @author Oleg Marchenko
 */
public final class RepositorySaveMethodMetadataValidator extends RepositoryModifyingMethodMetadataValidator {
    public RepositorySaveMethodMetadataValidator(EntityMetadata entity) {
        super(entity);
    }
}
