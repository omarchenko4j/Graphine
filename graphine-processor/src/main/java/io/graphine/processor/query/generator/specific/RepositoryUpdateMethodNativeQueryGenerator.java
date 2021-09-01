package io.graphine.processor.query.generator.specific;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.IdentifierMetadata;
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
        String joinedColumns =
                collectColumns(entity)
                        .stream()
                        .map(column -> column + " = ?")
                        .collect(Collectors.joining(", "));

        IdentifierMetadata identifier = entity.getIdentifier();
        return new StringBuilder()
                .append("UPDATE ")
                .append(getIfNotEmpty(entity.getSchema(), () -> entity.getSchema() + '.'))
                .append(entity.getTable())
                .append(" SET ")
                .append(joinedColumns)
                .append(" WHERE ")
                .append(identifier.getColumn()).append(" = ?")
                .toString();
    }

    @Override
    protected List<String> collectColumns(EntityMetadata entity) {
        return entity.getAttributes(true)
                     .stream()
                     .flatMap(attribute -> getColumn(attribute).stream())
                     .collect(Collectors.toList());
    }
}
