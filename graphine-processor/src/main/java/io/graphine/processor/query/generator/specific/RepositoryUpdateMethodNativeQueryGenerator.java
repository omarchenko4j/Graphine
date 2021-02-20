package io.graphine.processor.query.generator.specific;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.query.model.NativeQuery;

import java.util.stream.Collectors;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryUpdateMethodNativeQueryGenerator extends RepositoryMethodNativeQueryGenerator {
    public RepositoryUpdateMethodNativeQueryGenerator(EntityMetadata entity) {
        super(entity);
    }

    @Override
    public NativeQuery generate(MethodMetadata method) {
        String joinedColumns =
                entity.getAttributes(true)
                      .stream()
                      .map(AttributeMetadata::getColumn)
                      .map(column -> column + " = ?")
                      .collect(Collectors.joining(", "));

        String query = new StringBuilder()
                .append("UPDATE ")
                .append(entity.getQualifiedTable())
                .append(" SET ")
                .append(joinedColumns)
                .append(" WHERE ")
                .append(entity.getIdentifier().getColumn()).append(" = ?")
                .toString();
        return new NativeQuery(query);
    }
}
