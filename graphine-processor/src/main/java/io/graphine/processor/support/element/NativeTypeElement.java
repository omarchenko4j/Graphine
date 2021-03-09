package io.graphine.processor.support.element;

import javax.lang.model.element.TypeElement;

import static io.graphine.processor.support.EnvironmentContext.elementUtils;

/**
 * @author Oleg Marchenko
 */
public class NativeTypeElement extends NativeElement<TypeElement> {
    protected final String qualifiedName;
    protected final String packageName;

    protected NativeTypeElement(TypeElement element) {
        super(element);
        this.qualifiedName = element.getQualifiedName().toString();
        this.packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public String getPackageName() {
        return packageName;
    }

    @Override
    public String toString() {
        return qualifiedName;
    }
}
