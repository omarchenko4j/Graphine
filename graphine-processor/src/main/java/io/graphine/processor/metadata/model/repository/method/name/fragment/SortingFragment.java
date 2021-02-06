package io.graphine.processor.metadata.model.repository.method.name.fragment;

import java.util.List;
import java.util.stream.Stream;

import static io.graphine.processor.util.StringUtils.uncapitalize;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

/**
 * @author Oleg Marchenko
 */
public final class SortingFragment {
    public static final String KEYWORD = "OrderBy";

    private final List<Sort> sorts;

    public SortingFragment(String fragment) {
        String[] sorts = fragment.split("(?<=Asc|Desc)");
        this.sorts = Stream.of(sorts)
                           .map(Sort::new)
                           .collect(toList());
    }

    public List<Sort> getSorts() {
        return unmodifiableList(sorts);
    }

    public static final class Sort {
        private final String attributeName;
        private final Direction direction;

        public Sort(String predicate) {
            this.attributeName = Direction.retrieveAttributeName(predicate);
            this.direction = Direction.defineBy(predicate);
        }

        public String getAttributeName() {
            return attributeName;
        }

        public Direction getDirection() {
            return direction;
        }

        public enum Direction {
            ASC("Asc"),
            DESC("Desc");

            private final String keyword;

            Direction(String keyword) {
                this.keyword = keyword;
            }

            public static Direction defineBy(String value) {
                return value.endsWith(DESC.keyword) ? DESC : ASC;
            }

            public static String retrieveAttributeName(String predicate) {
                String attributeName = predicate;
                if (predicate.endsWith(DESC.keyword)) {
                    attributeName = predicate.substring(0, predicate.length() - DESC.keyword.length());
                }
                else if (predicate.endsWith(ASC.keyword)) {
                    attributeName = predicate.substring(0, predicate.length() - ASC.keyword.length());
                }
                return uncapitalize(attributeName);
            }
        }
    }
}
