package io.graphine.processor.metadata.validator.repository.method;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.SortingFragment;
import io.graphine.processor.metadata.model.repository.method.parameter.ParameterMetadata;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static io.graphine.processor.support.EnvironmentContext.messager;
import static io.graphine.processor.support.EnvironmentContext.typeUtils;
import static java.util.Objects.nonNull;
import static javax.tools.Diagnostic.Kind;

/**
 * @author Oleg Marchenko
 */
public abstract class RepositoryModifyingMethodMetadataValidator extends RepositoryMethodMetadataValidator {
    protected RepositoryModifyingMethodMetadataValidator(EntityMetadataRegistry entityMetadataRegistry) {
        super(entityMetadataRegistry);
    }

    @Override
    protected boolean validateReturnType(MethodMetadata method, EntityMetadata entity) {
        boolean valid = true;

        ExecutableElement methodElement = method.getNativeElement();

        TypeMirror returnType = methodElement.getReturnType();
        if (returnType.getKind() != TypeKind.VOID) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Method must return void", methodElement);
        }

        return valid;
    }

    @Override
    protected boolean validateSignature(MethodMetadata method, EntityMetadata entity) {
        boolean valid = true;

        QueryableMethodName queryableName = method.getQueryableName();

        QualifierFragment qualifier = queryableName.getQualifier();
        if (qualifier.hasDistinctSpecifier()) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Method name must not include 'Distinct' keyword", method.getNativeElement());
        }
        if (qualifier.hasFirstSpecifier()) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Method name must not include 'First' keyword", method.getNativeElement());
        }

        if (!validateMethodParameter(method, entity)) {
            valid = false;
        }

        ConditionFragment condition = queryableName.getCondition();
        if (nonNull(condition)) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Method name must not include conditions", method.getNativeElement());
        }

        SortingFragment sorting = queryableName.getSorting();
        if (nonNull(sorting)) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Method name must not include sorting", method.getNativeElement());
        }

        return valid;
    }

    protected boolean validateMethodParameter(MethodMetadata method, EntityMetadata entity) {
        boolean valid = true;

        List<ParameterMetadata> methodParameters = method.getParameters();
        if (methodParameters.size() != 1) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Method must consume one parameter", method.getNativeElement());
        }
        else {
            TypeMirror entityType = entity.getNativeType();

            ParameterMetadata methodParameter = methodParameters.get(0);
            TypeMirror parameterType = methodParameter.getNativeType();

            QueryableMethodName queryableName = method.getQueryableName();

            QualifierFragment qualifier = queryableName.getQualifier();
            switch (qualifier.getMethodForm()) {
                case SINGULAR:
                    if (!typeUtils.isSameType(parameterType, entityType)) {
                        valid = false;
                        messager.printMessage(Kind.ERROR,
                                              "Method must consume the entity class as a parameter",
                                              methodParameter.getNativeElement());
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
                                                      methodParameter.getNativeElement());
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
                                                          methodParameter.getNativeElement());
                                }
                            }
                            else {
                                valid = false;
                                messager.printMessage(Kind.ERROR,
                                                      "Method must consume an array or collection of entity classes as a parameter",
                                                      methodParameter.getNativeElement());
                            }
                            break;
                        default:
                            valid = false;
                            messager.printMessage(Kind.ERROR,
                                                  "Method must consume an array or collection of entity classes as a parameter",
                                                  methodParameter.getNativeElement());
                            break;
                    }
                    break;
            }
        }

        return valid;
    }
}
