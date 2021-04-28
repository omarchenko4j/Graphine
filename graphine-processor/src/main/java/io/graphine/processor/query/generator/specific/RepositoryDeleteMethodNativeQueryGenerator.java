package io.graphine.processor.query.generator.specific;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.IdentifierMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.query.model.parameter.ComplexParameter;
import io.graphine.processor.query.model.parameter.IterableParameter;
import io.graphine.processor.query.model.parameter.Parameter;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.List;

import static io.graphine.processor.util.StringUtils.uncapitalize;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryDeleteMethodNativeQueryGenerator extends RepositoryMethodNativeQueryGenerator {
    public RepositoryDeleteMethodNativeQueryGenerator(EntityMetadata entity) {
        super(entity);
    }

    @Override
    protected String generateQuery(MethodMetadata method) {
        StringBuilder queryBuilder = new StringBuilder()
                .append("DELETE FROM ")
                .append(entity.getQualifiedTable());

        QueryableMethodName queryableName = method.getQueryableName();

        ConditionFragment condition = queryableName.getCondition();
        if (isNull(condition)) {
            IdentifierMetadata identifier = entity.getIdentifier();

            QualifierFragment qualifier = queryableName.getQualifier();
            switch (qualifier.getMethodForm()) {
                case SINGULAR:
                    queryBuilder
                            .append(" WHERE ")
                            .append(identifier.getColumn())
                            .append(" = ?");
                    break;
                case PLURAL:
                    queryBuilder
                            .append(" WHERE ")
                            .append(identifier.getColumn())
                            .append(" IN (%s)");
                    break;
            }
        }
        else {
            queryBuilder.append(generateWhereClause(condition));
        }

        return queryBuilder.toString();
    }

    @Override
    protected List<Parameter> collectDeferredParameters(MethodMetadata method) {
        ExecutableElement methodElement = method.getNativeElement();
        List<? extends VariableElement> methodParameters = methodElement.getParameters();

        QueryableMethodName queryableName = method.getQueryableName();

        ConditionFragment condition = queryableName.getCondition();
        if (isNull(condition)) {
            QualifierFragment qualifier = queryableName.getQualifier();
            if (qualifier.isPluralForm()) {
                // Validation must ensure that only one method parameter is present.
                VariableElement parameterElement = methodParameters.get(0);
                return singletonList(Parameter.basedOn(parameterElement));
            }
        }
        else {
            return collectDeferredParameters(condition, methodParameters);
        }
        return emptyList();
    }

    @Override
    protected List<Parameter> collectConsumedParameters(MethodMetadata method) {
        QueryableMethodName queryableName = method.getQueryableName();

        ConditionFragment condition = queryableName.getCondition();
        if (isNull(condition)) {
            ExecutableElement methodElement = method.getNativeElement();
            // Validation must ensure that only one method parameter is present.
            VariableElement parameterElement = methodElement.getParameters().get(0);

            Parameter parentParameter = Parameter.basedOn(parameterElement);
            Parameter childParameter = Parameter.basedOn(entity.getIdentifier().getNativeElement());
            Parameter parameter = new ComplexParameter(parentParameter, singletonList(childParameter));

            QualifierFragment qualifier = queryableName.getQualifier();
            if (qualifier.isPluralForm()) {
                parentParameter = new Parameter(uncapitalize(entity.getName()), entity.getNativeType());
                parameter = new ComplexParameter(parentParameter, singletonList(childParameter));
                parameter = new IterableParameter(Parameter.basedOn(parameterElement), parameter);
            }
            return singletonList(parameter);
        }
        else {
            ExecutableElement methodElement = method.getNativeElement();
            return collectConditionParameters(condition, methodElement.getParameters());
        }
    }
}
