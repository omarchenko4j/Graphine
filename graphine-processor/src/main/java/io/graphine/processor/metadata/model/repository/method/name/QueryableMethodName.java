package io.graphine.processor.metadata.model.repository.method.name;

import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.SortingFragment;

/**
 * @author Oleg Marchenko
 */
public final class QueryableMethodName {
    private final QualifierFragment qualifier;
    private final ConditionFragment condition;
    private final SortingFragment sorting;

    public QueryableMethodName(QualifierFragment qualifier,
                               ConditionFragment condition,
                               SortingFragment sorting) {
        this.qualifier = qualifier;
        this.condition = condition;
        this.sorting = sorting;
    }

    public QualifierFragment getQualifier() {
        return qualifier;
    }

    public ConditionFragment getCondition() {
        return condition;
    }

    public SortingFragment getSorting() {
        return sorting;
    }
}
