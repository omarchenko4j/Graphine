package io.graphine.processor.metadata.validator.repository.method;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment.AndPredicate;
import static io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment.OperatorType;
import static io.graphine.processor.support.EnvironmentContext.messager;
import static io.graphine.processor.support.EnvironmentContext.typeUtils;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static javax.tools.Diagnostic.Kind;

/**
 * @author Oleg Marchenko
 */
public abstract class MethodMetadataValidator {
    protected final EntityMetadata entity;

    protected MethodMetadataValidator(EntityMetadata entity) {
        this.entity = entity;
    }

    public boolean validate(MethodMetadata method) {
        return validate(method.getNativeElement(), method.getQueryableName());
    }

    private boolean validate(ExecutableElement methodElement, QueryableMethodName queryableName) {
        boolean valid = true;
        if (!validateReturnType(methodElement, queryableName.getQualifier())) {
            valid = false;
        }
        if (!validateSignature(methodElement, queryableName)) {
            valid = false;
        }
        return valid;
    }

    protected abstract boolean validateReturnType(ExecutableElement methodElement, QualifierFragment qualifier);

    protected abstract boolean validateSignature(ExecutableElement methodElement, QueryableMethodName queryableName);

    protected boolean validateConditionParameters(ExecutableElement methodElement, ConditionFragment condition) {
        boolean valid = true;

        List<AndPredicate> predicates =
                condition.getOrPredicates()
                         .stream()
                         .flatMap(orPredicate -> orPredicate.getAndPredicates().stream())
                         .collect(toList());

        int numberOfConditionParameters =
                predicates
                        .stream()
                        .map(AndPredicate::getOperator)
                        .mapToInt(OperatorType::getParameterCount)
                        .sum();

        List<? extends VariableElement> parameters = methodElement.getParameters();

        int numberOfMethodParameters = parameters.size();
        if (numberOfConditionParameters != numberOfMethodParameters) {
            messager.printMessage(Kind.ERROR,
                                  "Number of condition parameters (" + numberOfConditionParameters +
                                  ") is not equal to the number of method parameters (" +
                                  numberOfMethodParameters + ")",
                                  methodElement);
            return false;
        }

        int parameterIndex = 0;
        for (AndPredicate predicate : predicates) {
            String attributeName = predicate.getAttributeName();
            OperatorType operator = predicate.getOperator();

            AttributeMetadata attribute = entity.getAttribute(attributeName);
            if (isNull(attribute)) {
                valid = false;
                messager.printMessage(Kind.ERROR,
                                      "Condition parameter (" + attributeName + ") not found as entity attribute",
                                      methodElement);
            }
            else {
                TypeMirror attributeType = attribute.getNativeType();

                int parameterCount = parameterIndex + operator.getParameterCount();
                while (parameterIndex < parameterCount) {
                    VariableElement parameterElement = parameters.get(parameterIndex);
                    TypeMirror parameterType = parameterElement.asType();

                    if (operator == OperatorType.IN || operator == OperatorType.NOT_IN) {
                        switch (parameterType.getKind()) {
                            case ARRAY:
                                ArrayType arrayType = (ArrayType) parameterType;
                                parameterType = arrayType.getComponentType();
                                if (!typeUtils.isSameType(parameterType, attributeType)) {
                                    if (parameterType.getKind().isPrimitive()) {
                                        PrimitiveType primitiveType = typeUtils.getPrimitiveType(parameterType.getKind());
                                        parameterType = typeUtils.boxedClass(primitiveType).asType();
                                        if (!typeUtils.isSameType(parameterType, attributeType)) {
                                            valid = false;
                                            messager.printMessage(Kind.ERROR,
                                                                  "Method parameter (" + parameterElement.getSimpleName() +
                                                                  ") has an incompatible array type with entity attribute type (" +
                                                                  attributeName + ")",
                                                                  parameterElement);
                                        }
                                    }
                                    else if (attributeType.getKind().isPrimitive()) {
                                        PrimitiveType primitiveType = typeUtils.getPrimitiveType(attributeType.getKind());
                                        attributeType = typeUtils.boxedClass(primitiveType).asType();
                                        if (!typeUtils.isSameType(parameterType, attributeType)) {
                                            valid = false;
                                            messager.printMessage(Kind.ERROR,
                                                                  "Method parameter (" + parameterElement.getSimpleName() +
                                                                  ") has an incompatible array type with entity attribute type (" +
                                                                  attributeName + ")",
                                                                  parameterElement);
                                        }
                                    }
                                    else {
                                        valid = false;
                                        messager.printMessage(Kind.ERROR,
                                                              "Method parameter (" + parameterElement.getSimpleName() +
                                                              ") has an incompatible array type with entity attribute type (" +
                                                              attributeName + ")",
                                                              parameterElement);
                                    }
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
                                    if (!typeUtils.isSameType(parameterType, attributeType)) {
                                        if (attributeType.getKind().isPrimitive()) {
                                            PrimitiveType primitiveType = typeUtils.getPrimitiveType(attributeType.getKind());
                                            attributeType = typeUtils.boxedClass(primitiveType).asType();
                                            if (!typeUtils.isSameType(parameterType, attributeType)) {
                                                valid = false;
                                                messager.printMessage(Kind.ERROR,
                                                                      "Method parameter (" + parameterElement.getSimpleName() +
                                                                      ") has an incompatible argument type in collection with entity attribute type (" +
                                                                      attributeName + ")",
                                                                      parameterElement);
                                            }
                                        }
                                        else {
                                            valid = false;
                                            messager.printMessage(Kind.ERROR,
                                                                  "Method parameter (" + parameterElement.getSimpleName() +
                                                                  ") has an incompatible argument type in collection with entity attribute type (" +
                                                                  attributeName + ")",
                                                                  parameterElement);
                                        }
                                    }
                                }
                                else {
                                    valid = false;
                                    messager.printMessage(Kind.ERROR,
                                                          "Condition parameter with the predicate (" + operator.getKeyword() +
                                                          ") must match the array or collection type in the method parameter",
                                                          parameterElement);
                                }
                                break;
                            default:
                                valid = false;
                                messager.printMessage(Kind.ERROR,
                                                      "Condition parameter with the predicate (" + operator.getKeyword() +
                                                      ") must match the array or collection type in the method parameter",
                                                      parameterElement);
                                break;
                        }
                    }
                    else {
                        if (!typeUtils.isSameType(parameterType, attributeType)) {
                            if (parameterType.getKind().isPrimitive()) {
                                PrimitiveType primitiveType = typeUtils.getPrimitiveType(parameterType.getKind());
                                parameterType = typeUtils.boxedClass(primitiveType).asType();
                                if (!typeUtils.isSameType(parameterType, attributeType)) {
                                    valid = false;
                                    messager.printMessage(Kind.ERROR,
                                                          "Method parameter (" + parameterElement.getSimpleName() +
                                                          ") has an incompatible type with entity attribute type (" +
                                                          attributeName + ")",
                                                          parameterElement);
                                }
                            }
                            else if (attributeType.getKind().isPrimitive()) {
                                PrimitiveType primitiveType = typeUtils.getPrimitiveType(attributeType.getKind());
                                attributeType = typeUtils.boxedClass(primitiveType).asType();
                                if (!typeUtils.isSameType(parameterType, attributeType)) {
                                    valid = false;
                                    messager.printMessage(Kind.ERROR,
                                                          "Method parameter (" + parameterElement.getSimpleName() +
                                                          ") has an incompatible type with entity attribute type (" +
                                                          attributeName + ")",
                                                          parameterElement);
                                }
                                else {
                                    messager.printMessage(Kind.MANDATORY_WARNING,
                                                          "Method parameter (" + parameterElement.getSimpleName() +
                                                          ") can be primitive because entity attribute (" +
                                                          attributeName + ") is primitive",
                                                          parameterElement);
                                }
                            }
                            else {
                                valid = false;
                                messager.printMessage(Kind.ERROR,
                                                      "Method parameter (" + parameterElement.getSimpleName() +
                                                      ") has an incompatible type with entity attribute type (" +
                                                      attributeName + ")",
                                                      parameterElement);
                            }
                        }
                    }

                    parameterIndex++;
                }
            }
        }
        return valid;
    }
}
