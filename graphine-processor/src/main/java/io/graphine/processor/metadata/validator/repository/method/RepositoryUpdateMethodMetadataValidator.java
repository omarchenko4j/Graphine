package io.graphine.processor.metadata.validator.repository.method;

import io.graphine.processor.metadata.model.entity.EntityMetadata;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryUpdateMethodMetadataValidator extends RepositoryModifyingMethodMetadataValidator {
    public RepositoryUpdateMethodMetadataValidator(EntityMetadata entity) {
        super(entity);
    }
}
