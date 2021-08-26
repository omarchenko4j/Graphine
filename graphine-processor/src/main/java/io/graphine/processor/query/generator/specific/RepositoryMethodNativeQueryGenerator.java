package io.graphine.processor.query.generator.specific;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.query.model.NativeQuery;

import java.util.List;
import java.util.StringJoiner;

import static io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment.AndPredicate;
import static io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment.OrPredicate;

/**
 * @author Oleg Marchenko
 */
public abstract class RepositoryMethodNativeQueryGenerator {
    protected final EntityMetadata entity;

    protected RepositoryMethodNativeQueryGenerator(EntityMetadata entity) {
        this.entity = entity;
    }

    public final NativeQuery generate(MethodMetadata method) {
        return new NativeQuery(generateQuery(method));
    }

    protected abstract String generateQuery(MethodMetadata method);

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
}
