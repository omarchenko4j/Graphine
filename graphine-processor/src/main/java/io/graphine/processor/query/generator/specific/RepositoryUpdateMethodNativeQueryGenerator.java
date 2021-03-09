package io.graphine.processor.query.generator.specific;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.IdentifierMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.query.model.parameter.ComplexParameter;
import io.graphine.processor.query.model.parameter.Parameter;

import java.util.List;
import java.util.stream.Collectors;

import static io.graphine.processor.util.StringUtils.uncapitalize;
import static java.util.Collections.singletonList;

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

    @Override
    protected List<Parameter> collectConsumedParameters(MethodMetadata method) {
        IdentifierMetadata identifier = entity.getIdentifier();

        Parameter parentParameter = new Parameter(uncapitalize(entity.getName()), entity.getNativeType());
        List<Parameter> childParameters =
                entity.getAttributes(true)
                      .stream()
                      .map(AttributeMetadata::getNativeElement)
                      .map(Parameter::basedOn)
                      .collect(Collectors.toList());
        childParameters.add(Parameter.basedOn(identifier.getNativeElement()));
        return singletonList(new ComplexParameter(parentParameter, childParameters));
    }
}
