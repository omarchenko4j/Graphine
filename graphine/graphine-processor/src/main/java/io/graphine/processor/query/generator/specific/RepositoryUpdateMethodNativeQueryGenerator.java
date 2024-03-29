package io.graphine.processor.query.generator.specific;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;

import java.util.List;
import java.util.stream.Collectors;

import static io.graphine.processor.util.StringUtils.getIfNotEmpty;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryUpdateMethodNativeQueryGenerator extends RepositoryMethodNativeQueryGenerator {
    public RepositoryUpdateMethodNativeQueryGenerator(EntityMetadataRegistry entityMetadataRegistry) {
        super(entityMetadataRegistry);
    }

    @Override
    protected String generateQuery(EntityMetadata entity, MethodMetadata method) {
        String joinedSetColumns =
                collectColumns(entity)
                        .stream()
                        .map(column -> column + " = ?")
                        .collect(Collectors.joining(", "));
        String joinedWhereColumn =
                getColumn(entity.getIdentifier())
                        .stream()
                        .map(column -> column + " = ?")
                        .collect(Collectors.joining(" AND "));
        return new StringBuilder()
                .append("UPDATE ")
                .append(getIfNotEmpty(entity.getSchema(), () -> entity.getSchema() + '.'))
                .append(entity.getTable())
                .append(" SET ")
                .append(joinedSetColumns)
                .append(" WHERE ")
                .append(joinedWhereColumn)
                .toString();
    }

    @Override
    protected List<String> collectColumns(EntityMetadata entity) {
        return entity.getUnidentifiedAttributes()
                     .stream()
                     .flatMap(attribute -> getColumn(attribute).stream())
                     .collect(Collectors.toList());
    }
}
