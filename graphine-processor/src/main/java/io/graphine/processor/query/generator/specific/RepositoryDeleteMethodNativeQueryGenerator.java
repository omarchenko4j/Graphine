package io.graphine.processor.query.generator.specific;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.IdentifierMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.model.repository.method.parameter.ParameterMetadata;
import io.graphine.processor.query.model.parameter.ComplexParameter;
import io.graphine.processor.query.model.parameter.IterableParameter;
import io.graphine.processor.query.model.parameter.Parameter;

import java.util.List;

import static io.graphine.processor.util.StringUtils.uncapitalize;
import static io.graphine.processor.util.VariableNameUniqueizer.uniqueize;
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
    protected List<Parameter> collectConsumedParameters(MethodMetadata method) {
        QueryableMethodName queryableName = method.getQueryableName();

        ConditionFragment condition = queryableName.getCondition();
        if (isNull(condition)) {
            // Validation must ensure that only one method parameter is present.
            ParameterMetadata methodParameter = method.getParameters().get(0);

            Parameter parentParameter = Parameter.basedOn(methodParameter);
            Parameter childParameter = Parameter.basedOn(entity.getIdentifier());
            Parameter parameter = new ComplexParameter(parentParameter, singletonList(childParameter));

            QualifierFragment qualifier = queryableName.getQualifier();
            if (qualifier.isPluralForm()) {
                parentParameter = new Parameter(uniqueize(uncapitalize(entity.getName())), entity.getNativeType());
                parameter = new ComplexParameter(parentParameter, singletonList(childParameter));
                parameter = new IterableParameter(Parameter.basedOn(methodParameter), parameter);
            }
            return singletonList(parameter);
        }
        else {
            return collectConditionParameters(condition, method.getParameters());
        }
    }
}
