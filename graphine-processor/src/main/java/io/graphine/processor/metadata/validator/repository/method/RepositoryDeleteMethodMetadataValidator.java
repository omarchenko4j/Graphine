package io.graphine.processor.metadata.validator.repository.method;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.SortingFragment;

import javax.lang.model.element.ExecutableElement;

import static io.graphine.processor.support.EnvironmentContext.messager;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static javax.tools.Diagnostic.Kind;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryDeleteMethodMetadataValidator extends RepositoryModifyingMethodMetadataValidator {
    public RepositoryDeleteMethodMetadataValidator(EntityMetadata entity) {
        super(entity);
    }

    @Override
    protected boolean validateSignature(ExecutableElement methodElement, QueryableMethodName queryableName) {
        boolean valid = true;

        ConditionFragment condition = queryableName.getCondition();
        if (!validateConditionParameters(methodElement, condition)) {
            valid = false;
        }

        if (isNull(condition)) {
            QualifierFragment qualifier = queryableName.getQualifier();
            if (!validateConsumedParameter(methodElement, qualifier)) {
                valid = false;
            }
        }

        SortingFragment sorting = queryableName.getSorting();
        if (nonNull(sorting)) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Method name must not include sorting", methodElement);
        }

        return valid;
    }
}
