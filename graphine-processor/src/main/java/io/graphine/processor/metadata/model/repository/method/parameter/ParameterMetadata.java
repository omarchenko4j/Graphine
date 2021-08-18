package io.graphine.processor.metadata.model.repository.method.parameter;

import io.graphine.processor.support.element.NativeElement;

import javax.lang.model.element.VariableElement;

/**
 * @author Oleg Marchenko
 */
public class ParameterMetadata extends NativeElement<VariableElement> {
    public ParameterMetadata(VariableElement element) {
        super(element);
    }
}
