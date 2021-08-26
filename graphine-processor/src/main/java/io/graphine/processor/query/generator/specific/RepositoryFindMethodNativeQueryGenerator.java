package io.graphine.processor.query.generator.specific;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.SortingFragment;

import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryFindMethodNativeQueryGenerator extends RepositoryMethodNativeQueryGenerator {
    public RepositoryFindMethodNativeQueryGenerator(EntityMetadata entity) {
        super(entity);
    }

    @Override
    protected String generateQuery(MethodMetadata method) {
        StringBuilder queryBuilder = new StringBuilder()
                .append("SELECT ");

        QueryableMethodName queryableName = method.getQueryableName();

        QualifierFragment qualifier = queryableName.getQualifier();
        if (qualifier.hasDistinctSpecifier()) {
            queryBuilder.append("DISTINCT ");
        }

        String joinedColumns =
                entity.getAttributes()
                      .stream()
                      .map(AttributeMetadata::getColumn)
                      .collect(Collectors.joining(", "));
        queryBuilder
                .append(joinedColumns)
                .append(" FROM ")
                .append(entity.getQualifiedTable());

        ConditionFragment condition = queryableName.getCondition();
        if (nonNull(condition)) {
            queryBuilder.append(generateWhereClause(condition));
        }

        SortingFragment sorting = queryableName.getSorting();
        if (nonNull(sorting)) {
            queryBuilder.append(generateOrderClause(sorting));
        }

        if (qualifier.hasFirstSpecifier()) {
            queryBuilder.append(" LIMIT 1");
        }

        return queryBuilder.toString();
    }

    private String generateOrderClause(SortingFragment sorting) {
        String joinedOrderColumns =
                sorting.getSorts()
                       .stream()
                       .map(sort -> {
                           AttributeMetadata attribute = entity.getAttribute(sort.getAttributeName());
                           return attribute.getColumn() + " " + sort.getDirection().name();
                       })
                       .collect(Collectors.joining(", "));
        return " ORDER BY " + joinedOrderColumns;
    }
}
