package io.graphine.processor.query.generator.specific;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.EmbeddedIdentifierAttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.IdentifierAttributeMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;

import static io.graphine.processor.util.StringUtils.getIfNotEmpty;
import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryCountMethodNativeQueryGenerator extends RepositoryMethodNativeQueryGenerator {
    public RepositoryCountMethodNativeQueryGenerator(EntityMetadataRegistry entityMetadataRegistry) {
        super(entityMetadataRegistry);
    }

    @Override
    protected String generateQuery(EntityMetadata entity, MethodMetadata method) {
        IdentifierAttributeMetadata identifierAttribute = entity.getIdentifier();

        StringBuilder queryBuilder = new StringBuilder()
                .append("SELECT COUNT(")
                .append(getIdentifierColumn(identifierAttribute))
                .append(") AS count")
                .append(" FROM ")
                .append(getIfNotEmpty(entity.getSchema(), () -> entity.getSchema() + '.'))
                .append(entity.getTable());

        QueryableMethodName queryableName = method.getQueryableName();
        ConditionFragment condition = queryableName.getCondition();
        if (nonNull(condition)) {
            queryBuilder.append(generateWhereClause(entity, condition));
        }

        return queryBuilder.toString();
    }

    private String getIdentifierColumn(IdentifierAttributeMetadata identifierAttribute) {
        if (identifierAttribute instanceof EmbeddedIdentifierAttributeMetadata) {
            return "*";
        }
        return identifierAttribute.getColumn();
    }
}
