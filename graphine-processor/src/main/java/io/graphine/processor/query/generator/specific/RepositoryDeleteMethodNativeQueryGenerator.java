package io.graphine.processor.query.generator.specific;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.IdentifierMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;

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
}
