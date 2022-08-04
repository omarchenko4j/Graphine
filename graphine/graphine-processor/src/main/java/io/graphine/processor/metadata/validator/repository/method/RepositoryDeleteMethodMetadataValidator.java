package io.graphine.processor.metadata.validator.repository.method;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.SortingFragment;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;

import static io.graphine.processor.support.EnvironmentContext.logger;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryDeleteMethodMetadataValidator extends RepositoryModifyingMethodMetadataValidator {
    public RepositoryDeleteMethodMetadataValidator(EntityMetadataRegistry entityMetadataRegistry) {
        super(entityMetadataRegistry);
    }

    @Override
    protected boolean validateSignature(MethodMetadata method, EntityMetadata entity) {
        boolean valid = true;

        QueryableMethodName queryableName = method.getQueryableName();

        QualifierFragment qualifier = queryableName.getQualifier();
        if (qualifier.hasDistinctSpecifier()) {
            valid = false;
            logger.error("Method name must not include 'Distinct' keyword", method.getNativeElement());
        }
        if (qualifier.hasFirstSpecifier()) {
            valid = false;
            logger.error("Method name must not include 'First' keyword", method.getNativeElement());
        }

        ConditionFragment condition = queryableName.getCondition();
        if (isNull(condition)) {
            if (!validateMethodParameter(method, entity)) {
                valid = false;
            }
        }
        else {
            if (!validateConditionParameters(method, entity)) {
                valid = false;
            }
        }

        SortingFragment sorting = queryableName.getSorting();
        if (nonNull(sorting)) {
            valid = false;
            logger.error("Method name must not include sorting", method.getNativeElement());
        }

        return valid;
    }
}
