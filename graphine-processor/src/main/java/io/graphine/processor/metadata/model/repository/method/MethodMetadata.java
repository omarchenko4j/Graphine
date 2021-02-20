package io.graphine.processor.metadata.model.repository.method;

import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.support.element.NativeElement;

import javax.lang.model.element.ExecutableElement;

/**
 * @author Oleg Marchenko
 */
public class MethodMetadata extends NativeElement<ExecutableElement> {
    private final QueryableMethodName queryableName;

    public MethodMetadata(ExecutableElement element, QueryableMethodName queryableName) {
        super(element);
        this.queryableName = queryableName;
    }

    public QueryableMethodName getQueryableName() {
        return queryableName;
    }

    @Override
    public String toString() {
        return nativeElement.toString();
    }
}
