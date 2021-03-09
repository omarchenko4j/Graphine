package io.graphine.processor.query.generator.specific;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.IdentifierMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.query.model.parameter.ComplexParameter;
import io.graphine.processor.query.model.parameter.Parameter;

import javax.lang.model.element.ExecutableElement;
import java.util.List;

import static io.graphine.processor.util.StringUtils.uncapitalize;
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
            IdentifierMetadata identifier = entity.getIdentifier();

            Parameter parentParameter = new Parameter(uncapitalize(entity.getName()), entity.getNativeType());
            Parameter childParameter = Parameter.basedOn(identifier.getNativeElement());
            return singletonList(new ComplexParameter(parentParameter, singletonList(childParameter)));
        }
        else {
            ExecutableElement methodElement = method.getNativeElement();
            return collectConditionParameters(condition, methodElement.getParameters());
        }
    }
}
