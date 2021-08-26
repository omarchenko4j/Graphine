package io.graphine.processor.query.generator.specific;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.IdentifierMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;

import java.util.stream.Collectors;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryUpdateMethodNativeQueryGenerator extends RepositoryMethodNativeQueryGenerator {
    public RepositoryUpdateMethodNativeQueryGenerator(EntityMetadata entity) {
        super(entity);
    }

    @Override
    protected String generateQuery(MethodMetadata method) {
        String joinedColumns =
                entity.getAttributes(true)
                      .stream()
                      .map(AttributeMetadata::getColumn)
                      .map(column -> column + " = ?")
                      .collect(Collectors.joining(", "));

        IdentifierMetadata identifier = entity.getIdentifier();
        return new StringBuilder()
                .append("UPDATE ")
                .append(entity.getQualifiedTable())
                .append(" SET ")
                .append(joinedColumns)
                .append(" WHERE ")
                .append(identifier.getColumn()).append(" = ?")
                .toString();
    }
}
