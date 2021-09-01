package io.graphine.processor.query.generator.specific;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.IdentifierMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;

import static java.util.Objects.isNull;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryDeleteMethodNativeQueryGenerator extends RepositoryMethodNativeQueryGenerator {
    public RepositoryDeleteMethodNativeQueryGenerator(EntityMetadataRegistry entityMetadataRegistry) {
        super(entityMetadataRegistry);
    }

    @Override
    protected String generateQuery(EntityMetadata entity, MethodMetadata method) {
        StringBuilder queryBuilder = new StringBuilder()
                .append("DELETE FROM ")
                .append(entity.getQualifiedTable());

        QueryableMethodName queryableName = method.getQueryableName();

        ConditionFragment condition = queryableName.getCondition();
        if (isNull(condition)) {
            IdentifierMetadata identifier = entity.getIdentifier();
            queryBuilder
                    .append(" WHERE ")
                    .append(identifier.getColumn());

            QualifierFragment qualifier = queryableName.getQualifier();
            switch (qualifier.getMethodForm()) {
                case SINGULAR:
                    queryBuilder.append(" = ?");
                    break;
                case PLURAL:
                    queryBuilder.append(" IN (%s)");
                    break;
            }
        }
        else {
            queryBuilder.append(generateWhereClause(entity, condition));
        }

        return queryBuilder.toString();
    }
}
