package io.graphine.processor.metadata.model.repository.method;

import io.graphine.processor.metadata.model.NativeElementMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;

import javax.lang.model.element.ExecutableElement;

/**
 * @author Oleg Marchenko
 */
public class MethodMetadata extends NativeElementMetadata<ExecutableElement> {
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
