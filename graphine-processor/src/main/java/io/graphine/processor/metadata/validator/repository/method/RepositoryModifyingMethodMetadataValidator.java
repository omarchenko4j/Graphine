package io.graphine.processor.metadata.validator.repository.method;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.SortingFragment;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment.MethodForm;
import static io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment.SpecifierType;
import static io.graphine.processor.support.EnvironmentContext.messager;
import static io.graphine.processor.support.EnvironmentContext.typeUtils;
import static java.util.Objects.nonNull;
import static javax.tools.Diagnostic.Kind;

/**
 * @author Oleg Marchenko
 */
public abstract class RepositoryModifyingMethodMetadataValidator extends MethodMetadataValidator {
    protected RepositoryModifyingMethodMetadataValidator(EntityMetadata entity) {
        super(entity);
    }

    @Override
    protected boolean validateReturnType(ExecutableElement methodElement, MethodForm methodForm) {
        boolean valid = true;

        TypeMirror returnType = methodElement.getReturnType();
        if (returnType.getKind() != TypeKind.VOID) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Method must return void", methodElement);
        }

        return valid;
    }

    @Override
    protected boolean validateSignature(ExecutableElement methodElement, QueryableMethodName queryableName) {
        boolean valid = true;

        QualifierFragment qualifier = queryableName.getQualifier();
        Set<SpecifierType> specifiers = qualifier.getSpecifiers();
        if (specifiers.contains(SpecifierType.DISTINCT)) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Method name must not include 'Distinct' keyword", methodElement);
        }
        if (specifiers.contains(SpecifierType.FIRST)) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Method name must not include 'First' keyword", methodElement);
        }

        if (!validateConsumedParameter(methodElement, qualifier)) {
            valid = false;
        }

        ConditionFragment condition = queryableName.getCondition();
        if (nonNull(condition)) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Method name must not include conditions", methodElement);
        }

        SortingFragment sorting = queryableName.getSorting();
        if (nonNull(sorting)) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Method name must not include sorting", methodElement);
        }

        return valid;
    }

    protected boolean validateConsumedParameter(ExecutableElement methodElement, QualifierFragment qualifier) {
        boolean valid = true;

        List<? extends VariableElement> parameters = methodElement.getParameters();
        if (parameters.size() != 1) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Method must consume one parameter", methodElement);
        }
        else {
            TypeMirror entityType = entity.getNativeType();

            VariableElement parameterElement = parameters.get(0);
            TypeMirror parameterType = parameterElement.asType();

            switch (qualifier.getMethodForm()) {
                case SINGULAR:
                    if (!typeUtils.isSameType(parameterType, entityType)) {
                        valid = false;
                        messager.printMessage(Kind.ERROR,
                                              "Method must consume the entity class as a parameter",
                                              parameterElement);
                    }
                    break;
                case PLURAL:
                    switch (parameterType.getKind()) {
                        case ARRAY:
                            ArrayType arrayType = (ArrayType) parameterType;
                            parameterType = arrayType.getComponentType();
                            if (!typeUtils.isSameType(parameterType, entityType)) {
                                valid = false;
                                messager.printMessage(Kind.ERROR,
                                                      "Method must consume entity class as array type",
                                                      parameterElement);
                            }
                            break;
                        case DECLARED:
                            DeclaredType declaredType = (DeclaredType) parameterType;
                            String qualifiedName = ((TypeElement) declaredType.asElement()).getQualifiedName().toString();
                            if (qualifiedName.equals(Iterable.class.getName()) ||
                                    qualifiedName.equals(Collection.class.getName()) ||
                                    qualifiedName.equals(List.class.getName()) ||
                                    qualifiedName.equals(Set.class.getName())) {
                                parameterType = declaredType.getTypeArguments().get(0);
                                if (!typeUtils.isSameType(parameterType, entityType)) {
                                    valid = false;
                                    messager.printMessage(Kind.ERROR,
                                                          "Method must consume entity class as argument type in collection",
                                                          parameterElement);
                                }
                            }
                            else {
                                valid = false;
                                messager.printMessage(Kind.ERROR,
                                                      "Method must consume an array or collection of entity classes as a parameter",
                                                      parameterElement);
                            }
                            break;
                        default:
                            valid = false;
                            messager.printMessage(Kind.ERROR,
                                                  "Method must consume an array or collection of entity classes as a parameter",
                                                  parameterElement);
                            break;
                    }
                    break;
            }
        }

        return valid;
    }
}
