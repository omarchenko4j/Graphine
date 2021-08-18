package io.graphine.processor.metadata.model.repository.method;

import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.parameter.ParameterMetadata;
import io.graphine.processor.support.element.NativeElement;

import javax.lang.model.element.ExecutableElement;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * @author Oleg Marchenko
 */
public class MethodMetadata extends NativeElement<ExecutableElement> {
    private final QueryableMethodName queryableName;
    private final List<ParameterMetadata> parameters;

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
}
