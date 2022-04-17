package io.graphine.processor.query.generator.specific;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;

import java.util.stream.Collectors;

import static io.graphine.processor.util.StringUtils.getIfNotEmpty;
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
                .append(getIfNotEmpty(entity.getSchema(), () -> entity.getSchema() + '.'))
                .append(entity.getTable());

        QueryableMethodName queryableName = method.getQueryableName();

        ConditionFragment condition = queryableName.getCondition();
        if (isNull(condition)) {
            String joinedWhereColumn =
                    getColumn(entity.getIdentifier())
                            .stream()
                            .map(column -> {
                                QualifierFragment qualifier = queryableName.getQualifier();
                                switch (qualifier.getMethodForm()) {
                                    case SINGULAR:
                                        return column + " = ?";
                                    case PLURAL:
                                        return column + " IN (%s)";
                                    default:
                                        return "";
                                }
                            })
                            .collect(Collectors.joining(" AND "));
            queryBuilder
                    .append(" WHERE ")
                    .append(joinedWhereColumn);
        }
        else {
            queryBuilder.append(generateWhereClause(entity, condition));
        }

        return queryBuilder.toString();
    }
}
