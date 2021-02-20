package io.graphine.processor.query.generator.specific;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.query.model.NativeQuery;

import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryCountMethodNativeQueryGenerator extends RepositoryMethodNativeQueryGenerator {
    public RepositoryCountMethodNativeQueryGenerator(EntityMetadata entity) {
        super(entity);
    }

    @Override
    public NativeQuery generate(MethodMetadata method) {
        StringBuilder queryBuilder = new StringBuilder()
                .append("SELECT COUNT(")
                .append(entity.getIdentifier().getColumn())
                .append(") AS count")
                .append(" FROM ")
                .append(entity.getQualifiedTable());

        QueryableMethodName queryableName = method.getQueryableName();

        ConditionFragment condition = queryableName.getCondition();
        if (nonNull(condition)) {
            queryBuilder.append(generateConditionClause(condition));
        }

        return new NativeQuery(queryBuilder.toString());
    }
}
