package io.graphine.processor.metadata.model.repository.method.name.fragment;

import java.util.List;
import java.util.stream.Stream;

import static io.graphine.processor.util.StringUtils.uncapitalize;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

/**
 * @author Oleg Marchenko
 */
public final class ConditionFragment {
    private final List<OrPredicate> orPredicates;

    public ConditionFragment(String fragment) {
        String[] predicates = fragment.split(OrPredicate.KEYWORD);
        this.orPredicates = Stream.of(predicates)
                                  .map(OrPredicate::new)
                                  .collect(toList());
    }

    public List<OrPredicate> getOrPredicates() {
        return unmodifiableList(orPredicates);
    }

    public static class OrPredicate {
        private static final String KEYWORD = "Or";

        private final List<AndPredicate> andPredicates;

        public OrPredicate(String fragment) {
            String[] predicates = fragment.split(AndPredicate.KEYWORD);
            this.andPredicates = Stream.of(predicates)
                                       .map(AndPredicate::new)
                                       .collect(toList());
        }

        public List<AndPredicate> getAndPredicates() {
            return unmodifiableList(andPredicates);
        }
    }

    public static class AndPredicate {
        private static final String KEYWORD = "And";

        private final String attributeName;
        private final OperatorType operator;

        public AndPredicate(String predicate) {
            this.attributeName = OperatorType.retrieveAttributeName(predicate);
            this.operator = OperatorType.defineBy(predicate);
        }

        public String getAttributeName() {
            return attributeName;
        }

        public OperatorType getOperator() {
            return operator;
        }
    }

    public enum OperatorType {
        NOT_BETWEEN("NotBetween", 2),
        BETWEEN("Between", 2),
        LESS_THAN("LessThan", 1),
        LESS_THAN_EQUAL("LessThanEqual", 1),
        GREATER_THAN("GreaterThan", 1),
        GREATER_THAN_EQUAL("GreaterThanEqual", 1),
        BEFORE("Before", 1),
        AFTER("After", 1),
        NOT_LIKE("NotLike", 1),
        LIKE("Like", 1),
        STARTING_WITH("StartingWith", 1),
        ENDING_WITH("EndingWith", 1),
        NOT_CONTAINING("NotContaining", 1),
        CONTAINING("Containing", 1),
        NOT_EMPTY("IsNotEmpty", 0),
        EMPTY("IsEmpty", 0),
        NOT_IN("NotIn", 1),
        IN("In", 1),
        NOT_NULL("IsNotNull", 0),
        NULL("IsNull", 0),
        TRUE("IsTrue", 0),
        FALSE("IsFalse", 0),
        NOT_EQUAL("IsNot", 1),
        EQUAL("Is", 1);

        private final String keyword;
        private final int parameterCount;

        OperatorType(String keyword, int parameterCount) {
            this.keyword = keyword;
            this.parameterCount = parameterCount;
        }

        public int getParameterCount() {
            return parameterCount;
        }

        private static final OperatorType[] ALL_OPERATORS = values();

        public static OperatorType defineBy(String value) {
            for (OperatorType operator : ALL_OPERATORS) {
                if (value.endsWith(operator.keyword)) {
                    return operator;
                }
            }
            return EQUAL;
        }

        public static String retrieveAttributeName(String predicate) {
            String attribute = predicate;
            for (OperatorType operator : ALL_OPERATORS) {
                if (predicate.endsWith(operator.keyword)) {
                    attribute = predicate.substring(0, predicate.length() - operator.keyword.length());
                    break;
                }
            }
            return uncapitalize(attribute);
        }
    }
}
