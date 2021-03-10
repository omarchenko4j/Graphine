package io.graphine.processor.query.generator.specific;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.IdentifierMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.query.model.parameter.Parameter;

import javax.lang.model.element.ExecutableElement;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryCountMethodNativeQueryGenerator extends RepositoryMethodNativeQueryGenerator {
    public RepositoryCountMethodNativeQueryGenerator(EntityMetadata entity) {
        super(entity);
    }

    @Override
    protected String generateQuery(MethodMetadata method) {
        IdentifierMetadata identifier = entity.getIdentifier();

        StringBuilder queryBuilder = new StringBuilder()
                .append("SELECT COUNT(")
                .append(identifier.getColumn())
                .append(") AS count")
                .append(" FROM ")
                .append(entity.getQualifiedTable());

        QueryableMethodName queryableName = method.getQueryableName();

        ConditionFragment condition = queryableName.getCondition();
        if (nonNull(condition)) {
            queryBuilder.append(generateWhereClause(condition));
        }

        return queryBuilder.toString();
    }

    @Override
    protected List<Parameter> collectDeferredParameters(MethodMetadata method) {
        QueryableMethodName queryableName = method.getQueryableName();

        ConditionFragment condition = queryableName.getCondition();
        if (isNull(condition)) return emptyList();

        ExecutableElement methodElement = method.getNativeElement();
        return collectDeferredParameters(condition, methodElement.getParameters());
    }

    @Override
    protected List<Parameter> collectProducedParameters(MethodMetadata method) {
        ExecutableElement methodElement = method.getNativeElement();
        return singletonList(new Parameter("count", methodElement.getReturnType()));
    }

    @Override
    protected List<Parameter> collectConsumedParameters(MethodMetadata method) {
        QueryableMethodName queryableName = method.getQueryableName();

        ConditionFragment condition = queryableName.getCondition();
        if (nonNull(condition)) {
            ExecutableElement methodElement = method.getNativeElement();
            return collectConditionParameters(condition, methodElement.getParameters());
        }
        return emptyList();
    }
}
