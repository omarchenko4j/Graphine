package io.graphine.processor.metadata.validator.repository.method;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.SortingFragment;
import io.graphine.processor.metadata.model.repository.method.parameter.ParameterMetadata;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;

import static io.graphine.processor.support.EnvironmentContext.messager;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static javax.tools.Diagnostic.Kind;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryCountMethodMetadataValidator extends RepositoryMethodMetadataValidator {
    public RepositoryCountMethodMetadataValidator(EntityMetadata entity) {
        super(entity);
    }

    @Override
    protected boolean validateReturnType(MethodMetadata method) {
        boolean valid = true;

        ExecutableElement methodElement = method.getNativeElement();

        TypeMirror returnType = methodElement.getReturnType();
        if (returnType.getKind().isPrimitive()) {
            if (returnType.getKind() != TypeKind.INT && returnType.getKind() != TypeKind.LONG) {
                valid = false;
                messager.printMessage(Kind.ERROR, "Method must return a numeric type (int or long)", methodElement);
            }
        }
        else if (returnType.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) returnType;
            String qualifiedName = ((TypeElement) declaredType.asElement()).getQualifiedName().toString();
            if (!qualifiedName.equals(Integer.class.getName()) && !qualifiedName.equals(Long.class.getName())) {
                valid = false;
                messager.printMessage(Kind.ERROR, "Method must return a numeric type (int or long)", methodElement);
            }
            else {
                messager.printMessage(Kind.MANDATORY_WARNING,
                                      "Method can return a primitive numeric type (int or long)",
                                      methodElement);
            }
        }
        else {
            valid = false;
            messager.printMessage(Kind.ERROR, "Method must return a numeric type (int or long)", methodElement);
        }

        return valid;
    }

    @Override
    protected boolean validateSignature(MethodMetadata method) {
        boolean valid = true;

        QueryableMethodName queryableName = method.getQueryableName();

        QualifierFragment qualifier = queryableName.getQualifier();
        if (qualifier.isSingularForm()) {
            messager.printMessage(Kind.WARNING,
                                  "Use a semantic prefix (countAll) in the method name",
                                  method.getNativeElement());
        }
        if (qualifier.hasDistinctSpecifier()) {
            valid = false;
            messager.printMessage(Kind.ERROR,
                                  "Method name must not include 'Distinct' keyword",
                                  method.getNativeElement());
        }
        if (qualifier.hasFirstSpecifier()) {
            valid = false;
            messager.printMessage(Kind.ERROR,
                                  "Method name must not include 'First' keyword",
                                  method.getNativeElement());
        }

        ConditionFragment condition = queryableName.getCondition();
        if (isNull(condition)) {
            List<ParameterMetadata> methodParameters = method.getParameters();
            if (!methodParameters.isEmpty()) {
                valid = false;
                messager.printMessage(Kind.ERROR,
                                      "Method without condition parameters should not contain method parameters",
                                      method.getNativeElement());
            }
        }
        else {
            if (!validateConditionParameters(method)) {
                valid = false;
            }
        }

        SortingFragment sorting = queryableName.getSorting();
        if (nonNull(sorting)) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Method name must not include sorting", method.getNativeElement());
        }

        return valid;
    }
}
