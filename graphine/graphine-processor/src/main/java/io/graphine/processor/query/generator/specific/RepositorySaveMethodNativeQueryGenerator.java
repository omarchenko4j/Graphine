package io.graphine.processor.query.generator.specific;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;

import java.util.List;
import java.util.stream.Collectors;

import static io.graphine.processor.util.StringUtils.*;

/**
 * @author Oleg Marchenko
 */
public final class RepositorySaveMethodNativeQueryGenerator extends RepositoryMethodNativeQueryGenerator {
    public RepositorySaveMethodNativeQueryGenerator(EntityMetadataRegistry entityMetadataRegistry) {
        super(entityMetadataRegistry);
    }

    @Override
    protected String generateQuery(EntityMetadata entity, MethodMetadata method) {
        List<String> columns = collectColumns(entity);
        return new StringBuilder()
                .append("INSERT INTO ")
                .append(getIfNotEmpty(entity.getSchema(), () -> entity.getSchema() + '.'))
                .append(entity.getTable())
                .append(join(columns, ", ", "(", ")"))
                .append(" VALUES ")
                .append(repeat("?", ", ", "(", ")", columns.size()))
                .toString();
    }

    @Override
    protected List<String> collectColumns(EntityMetadata entity) {
        return entity.getAttributes()
                     .stream()
                     .flatMap(attribute -> getColumn(attribute).stream())
                     .collect(Collectors.toList());
    }
}
