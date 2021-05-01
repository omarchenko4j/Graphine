package io.graphine.processor.query.generator.specific;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.IdentifierMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.query.model.parameter.ComplexParameter;
import io.graphine.processor.query.model.parameter.IterableParameter;
import io.graphine.processor.query.model.parameter.Parameter;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.stream.Collectors;

import static io.graphine.processor.util.StringUtils.*;
import static io.graphine.processor.util.VariableNameUniqueizer.uniqueize;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 * @author Oleg Marchenko
 */
public final class RepositorySaveMethodNativeQueryGenerator extends RepositoryMethodNativeQueryGenerator {
    public RepositorySaveMethodNativeQueryGenerator(EntityMetadata entity) {
        super(entity);
    }

    @Override
    protected String generateQuery(MethodMetadata method) {
        IdentifierMetadata identifier = entity.getIdentifier();

        List<String> columns =
                entity.getAttributes(identifier.isAutogenerated())
                      .stream()
                      .map(AttributeMetadata::getColumn)
                      .collect(toList());

        return new StringBuilder()
                .append("INSERT INTO ")
                .append(entity.getQualifiedTable())
                .append(join(columns, ", ", "(", ")"))
                .append(" VALUES ")
                .append(repeat("?", ", ", "(", ")", columns.size()))
                .toString();
    }

    @Override
    protected List<Parameter> collectProducedParameters(MethodMetadata method) {
        IdentifierMetadata identifier = entity.getIdentifier();
        if (!identifier.isAutogenerated()) return emptyList();

        ExecutableElement methodElement = method.getNativeElement();
        // Validation must ensure that only one method parameter is present.
        VariableElement parameterElement = methodElement.getParameters().get(0);

        Parameter parentParameter = Parameter.basedOn(parameterElement);
        Parameter childParameter = Parameter.basedOn(identifier.getNativeElement());
        Parameter parameter = new ComplexParameter(parentParameter, singletonList(childParameter));

        QueryableMethodName queryableName = method.getQueryableName();
        QualifierFragment qualifier = queryableName.getQualifier();
        if (qualifier.isPluralForm()) {
            parentParameter = new Parameter(uniqueize(uncapitalize(entity.getName())), entity.getNativeType());
            parameter = new ComplexParameter(parentParameter, singletonList(childParameter));
            parameter = new IterableParameter(Parameter.basedOn(parameterElement), parameter);
        }
        return singletonList(parameter);
    }

    @Override
    protected List<Parameter> collectConsumedParameters(MethodMetadata method) {
        ExecutableElement methodElement = method.getNativeElement();
        // Validation must ensure that only one method parameter is present.
        VariableElement parameterElement = methodElement.getParameters().get(0);

        Parameter parentParameter = Parameter.basedOn(parameterElement);
        List<Parameter> childParameters =
                entity.getAttributes(entity.getIdentifier().isAutogenerated())
                      .stream()
                      .map(AttributeMetadata::getNativeElement)
                      .map(Parameter::basedOn)
                      .collect(Collectors.toList());
        Parameter parameter = new ComplexParameter(parentParameter, childParameters);

        QueryableMethodName queryableName = method.getQueryableName();
        QualifierFragment qualifier = queryableName.getQualifier();
        if (qualifier.isPluralForm()) {
            parentParameter = new Parameter(uniqueize(uncapitalize(entity.getName())), entity.getNativeType());
            parameter = new ComplexParameter(parentParameter, childParameters);
            parameter = new IterableParameter(Parameter.basedOn(parameterElement), parameter);
        }
        return singletonList(parameter);
    }
}
