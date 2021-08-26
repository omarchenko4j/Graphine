package io.graphine.processor.query.generator.specific;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.IdentifierMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;

import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryCountMethodNativeQueryGenerator extends RepositoryMethodNativeQueryGenerator {
    public RepositoryCountMethodNativeQueryGenerator(EntityMetadata entity) {
        super(entity);
    }

    @Override
    protected String generateQuery(MethodMetadata method) {
        IdentifierMetadata identifier = entity.getIdentifier();

        StringBuilder queryBuilder = new StringBuilder()
                .append("SELECT COUNT(")
                .append(identifier.getColumn())
                .append(") AS count")
                .append(" FROM ")
                .append(entity.getQualifiedTable());

        QueryableMethodName queryableName = method.getQueryableName();

        ConditionFragment condition = queryableName.getCondition();
        if (nonNull(condition)) {
            queryBuilder.append(generateWhereClause(condition));
        }

        return queryBuilder.toString();
    }
}
