package io.graphine.processor.metadata.model.repository.method;

import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment.AndPredicate;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment.OperatorType;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment.OrPredicate;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.model.repository.method.parameter.ParameterMetadata;
import io.graphine.processor.support.element.NativeElement;

import javax.lang.model.element.ExecutableElement;
import java.util.ArrayList;
import java.util.List;

import static io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment.OperatorType.IN;
import static io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment.OperatorType.NOT_IN;
import static io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment.MethodType.DELETE;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
public class MethodMetadata extends NativeElement<ExecutableElement> {
    private final QueryableMethodName queryableName;
    private final List<ParameterMetadata> parameters;
    private List<ParameterMetadata> deferredParameters;

    public MethodMetadata(ExecutableElement element,
                          QueryableMethodName queryableName,
                          List<ParameterMetadata> parameters) {
        super(element);
        this.queryableName = queryableName;
        this.parameters = parameters;
    }

    public QueryableMethodName getQueryableName() {
        return queryableName;
    }

    public List<ParameterMetadata> getParameters() {
        return unmodifiableList(parameters);
    }

    public List<ParameterMetadata> getDeferredParameters() {
        if (isNull(deferredParameters)) {
            deferredParameters = new ArrayList<>(parameters.size());

            ConditionFragment condition = queryableName.getCondition();
            if (nonNull(condition)) {
                int parameterIndex = 0;

                List<OrPredicate> orPredicates = condition.getOrPredicates();
                for (OrPredicate orPredicate : orPredicates) {
                    List<AndPredicate> andPredicates = orPredicate.getAndPredicates();
                    for (AndPredicate andPredicate : andPredicates) {
                        OperatorType operator = andPredicate.getOperator();
                        if (operator == IN || operator == NOT_IN) {
                            ParameterMetadata parameter = parameters.get(parameterIndex);
                            deferredParameters.add(parameter);
                        }
                        parameterIndex += operator.getParameterCount();
                    }
                }
            }
            else {
                QualifierFragment qualifier = queryableName.getQualifier();
                if (qualifier.getMethodType() == DELETE && qualifier.isPluralForm()) {
                    // Validation must ensure that only one method parameter is present.
                    ParameterMetadata firstParameter = parameters.get(0);
                    deferredParameters.add(firstParameter);
                }
            }
        }
        return unmodifiableList(deferredParameters);
    }
}
