package io.graphine.processor.query.generator.specific;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.query.model.NativeQuery;
import io.graphine.processor.query.model.parameter.ComputableParameter;
import io.graphine.processor.query.model.parameter.Parameter;

import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

import static io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment.*;
import static io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment.OperatorType.*;
import static java.util.Collections.emptyList;

/**
 * @author Oleg Marchenko
 */
public abstract class RepositoryMethodNativeQueryGenerator {
    protected final EntityMetadata entity;

    protected RepositoryMethodNativeQueryGenerator(EntityMetadata entity) {
        this.entity = entity;
    }

    public final NativeQuery generate(MethodMetadata method) {
        return new NativeQuery(generateQuery(method),
                               collectDeferredParameters(method),
                               collectProducedParameters(method),
                               collectConsumedParameters(method));
    }

    protected abstract String generateQuery(MethodMetadata method);

    protected List<Parameter> collectDeferredParameters(MethodMetadata method) {
        return emptyList();
    }

    protected List<Parameter> collectProducedParameters(MethodMetadata method) {
        return emptyList();
    }

    protected List<Parameter> collectConsumedParameters(MethodMetadata method) {
        return emptyList();
    }

    protected final String generateWhereClause(ConditionFragment condition) {
        StringJoiner orPredicateJoiner = new StringJoiner(" OR ");

        List<OrPredicate> orPredicates = condition.getOrPredicates();
        for (OrPredicate orPredicate : orPredicates) {
            StringJoiner andPredicateJoiner = new StringJoiner(" AND ");

            List<AndPredicate> andPredicates = orPredicate.getAndPredicates();
            for (AndPredicate andPredicate : andPredicates) {
                AttributeMetadata attribute = entity.getAttribute(andPredicate.getAttributeName());
                String column = attribute.getColumn();
                switch (andPredicate.getOperator()) {
                    case BETWEEN:
                        andPredicateJoiner.add(column + " BETWEEN ? AND ?");
                        break;
                    case NOT_BETWEEN:
                        andPredicateJoiner.add(column + " NOT BETWEEN ? AND ?");
                        break;
                    case NULL:
                        andPredicateJoiner.add(column + " IS NULL");
                        break;
                    case NOT_NULL:
                        andPredicateJoiner.add(column + " IS NOT NULL");
                        break;
                    case BEFORE:
                    case LESS_THAN:
                        andPredicateJoiner.add(column + " < ?");
                        break;
                    case LESS_THAN_EQUAL:
                        andPredicateJoiner.add(column + " <= ?");
                        break;
                    case AFTER:
                    case GREATER_THAN:
                        andPredicateJoiner.add(column + " > ?");
                        break;
                    case GREATER_THAN_EQUAL:
                        andPredicateJoiner.add(column + " >= ?");
                        break;
                    case LIKE:
                    case STARTING_WITH:
                    case ENDING_WITH:
                    case CONTAINING:
                        andPredicateJoiner.add(column + " LIKE ?");
                        break;
                    case NOT_LIKE:
                    case NOT_CONTAINING:
                        andPredicateJoiner.add(column + " NOT LIKE ?");
                        break;
                    case EMPTY:
                        andPredicateJoiner.add(column + " = ''");
                        break;
                    case NOT_EMPTY:
                        andPredicateJoiner.add(column + " <> ''");
                        break;
                    case IN:
                        andPredicateJoiner.add(column + " IN (%s)");
                        break;
                    case NOT_IN:
                        andPredicateJoiner.add(column + " NOT IN (%s)");
                        break;
                    case TRUE:
                        andPredicateJoiner.add(column + " IS TRUE");
                        break;
                    case FALSE:
                        andPredicateJoiner.add(column + " IS FALSE");
                        break;
                    case NOT_EQUAL:
                        andPredicateJoiner.add(column + " <> ?");
                        break;
                    case EQUAL:
                        andPredicateJoiner.add(column + " = ?");
                        break;
                }
            }
            orPredicateJoiner.add(andPredicateJoiner.toString());
        }

        return " WHERE " + orPredicateJoiner.toString();
    }

    protected final List<Parameter> collectConditionParameters(ConditionFragment condition,
                                                               List<? extends VariableElement> methodParameters) {
        List<Parameter> conditionParameters = new ArrayList<>(methodParameters.size());

        int parameterIndex = 0;

        List<OrPredicate> orPredicates = condition.getOrPredicates();
        for (OrPredicate orPredicate : orPredicates) {
            List<AndPredicate> andPredicates = orPredicate.getAndPredicates();
            for (AndPredicate andPredicate : andPredicates) {
                OperatorType operator = andPredicate.getOperator();

                int parameterCount = operator.getParameterCount();
                for (int i = parameterIndex; i < (parameterIndex + parameterCount); i++) {
                    VariableElement parameterElement = methodParameters.get(i);

                    Parameter parameter = Parameter.basedOn(parameterElement);
                    if (operator == STARTING_WITH) {
                        Function<Parameter, Parameter> computedFunction =
                                targetParameter -> new Parameter(targetParameter.getName() + " + \"%\"",
                                                                 targetParameter.getType());
                        parameter = new ComputableParameter(parameter, computedFunction);
                    }
                    else if (operator == ENDING_WITH) {
                        Function<Parameter, Parameter> computedFunction =
                                targetParameter -> new Parameter("\"%\" + " + targetParameter.getName(),
                                                                 targetParameter.getType());
                        parameter = new ComputableParameter(parameter, computedFunction);
                    }
                    else if (operator == CONTAINING || operator == NOT_CONTAINING) {
                        Function<Parameter, Parameter> computedFunction =
                                targetParameter -> new Parameter("\"%\" + " + targetParameter.getName() + " + \"%\"",
                                                                 targetParameter.getType());
                        parameter = new ComputableParameter(parameter, computedFunction);
                    }
                    conditionParameters.add(parameter);
                }

                parameterIndex += parameterCount;
            }
        }

        return conditionParameters;
    }

    protected final List<Parameter> collectDeferredParameters(ConditionFragment condition,
                                                              List<? extends VariableElement> methodParameters) {
        List<Parameter> deferredParameters = new ArrayList<>(methodParameters.size());

        int parameterIndex = 0;
        List<OrPredicate> orPredicates = condition.getOrPredicates();
        for (OrPredicate orPredicate : orPredicates) {
            List<AndPredicate> andPredicates = orPredicate.getAndPredicates();
            for (AndPredicate andPredicate : andPredicates) {
                OperatorType operator = andPredicate.getOperator();
                if (operator == IN || operator == NOT_IN) {
                    VariableElement parameterElement = methodParameters.get(parameterIndex);
                    deferredParameters.add(Parameter.basedOn(parameterElement));
                }
                parameterIndex += operator.getParameterCount();
            }
        }

        return deferredParameters;
    }
}
