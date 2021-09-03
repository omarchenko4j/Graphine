package io.graphine.processor.query.generator.specific;

import io.graphine.processor.metadata.model.entity.EmbeddableEntityMetadata;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.EmbeddedAttribute;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.AttributeChain;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.SortingFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.SortingFragment.Sort;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;
import io.graphine.processor.util.StringUtils;

import java.util.List;
import java.util.StringJoiner;

import static io.graphine.processor.util.StringUtils.getIfNotEmpty;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryFindMethodNativeQueryGenerator extends RepositoryMethodNativeQueryGenerator {
    public RepositoryFindMethodNativeQueryGenerator(EntityMetadataRegistry entityMetadataRegistry) {
        super(entityMetadataRegistry);
    }

    @Override
    protected String generateQuery(EntityMetadata entity, MethodMetadata method) {
        StringBuilder queryBuilder = new StringBuilder()
                .append("SELECT ");

        QueryableMethodName queryableName = method.getQueryableName();

        QualifierFragment qualifier = queryableName.getQualifier();
        if (qualifier.hasDistinctSpecifier()) {
            queryBuilder.append("DISTINCT ");
        }

        String joinedColumns = StringUtils.join(collectColumns(entity), ", ", "", "");
        queryBuilder
                .append(joinedColumns)
                .append(" FROM ")
                .append(getIfNotEmpty(entity.getSchema(), () -> entity.getSchema() + '.'))
                .append(entity.getTable());

        ConditionFragment condition = queryableName.getCondition();
        if (nonNull(condition)) {
            queryBuilder.append(generateWhereClause(entity, condition));
        }

        SortingFragment sorting = queryableName.getSorting();
        if (nonNull(sorting)) {
            queryBuilder.append(generateOrderClause(entity, sorting));
        }

        if (qualifier.hasFirstSpecifier()) {
            queryBuilder.append(" LIMIT 1");
        }

        return queryBuilder.toString();
    }

    private String generateOrderClause(EntityMetadata entity, SortingFragment sorting) {
        StringJoiner orderJoiner = new StringJoiner(", ");
        for (Sort sort : sorting.getSorts()) {
            AttributeChain attributeChain = sort.getAttributeChain();
            List<String> attributeNames = attributeChain.getAttributeNames();

            AttributeMetadata prevAttribute = null;
            AttributeMetadata attribute = entity.getAttribute(attributeNames.get(0));
            for (int i = 1; i < attributeNames.size(); i++) {
                prevAttribute = attribute;
                if (attribute instanceof EmbeddedAttribute) {
                    EmbeddableEntityMetadata embeddableEntity =
                            entityMetadataRegistry.getEmbeddableEntity(attribute.getNativeType().toString());
                    attribute = embeddableEntity.getAttribute(attributeNames.get(i));
                }
            }

            List<String> columns = null;
            if (prevAttribute instanceof EmbeddedAttribute) {
                String column = ((EmbeddedAttribute) prevAttribute).overrideAttribute(attribute);
                if (nonNull(column)) {
                    columns = singletonList(column);
                }
            }
            if (isNull(columns)) {
                columns = getColumn(attribute);
            }

            for (String column : columns) {
                orderJoiner.add(column + " " + sort.getDirection().name());
            }
        }
        return " ORDER BY " + orderJoiner;
    }
}
