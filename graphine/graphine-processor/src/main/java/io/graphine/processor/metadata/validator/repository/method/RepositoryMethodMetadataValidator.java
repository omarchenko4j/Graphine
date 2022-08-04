package io.graphine.processor.metadata.validator.repository.method;

import io.graphine.processor.metadata.model.entity.EmbeddableEntityMetadata;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.EmbeddedAttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.EmbeddedIdentifierAttributeMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.AttributeChain;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.parameter.ParameterMetadata;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment.AndPredicate;
import static io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment.OperatorType;
import static io.graphine.processor.support.EnvironmentContext.logger;
import static io.graphine.processor.support.EnvironmentContext.typeUtils;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

/**
 * @author Oleg Marchenko
 */
public abstract class RepositoryMethodMetadataValidator {
    protected final EntityMetadataRegistry entityMetadataRegistry;

    protected RepositoryMethodMetadataValidator(EntityMetadataRegistry entityMetadataRegistry) {
        this.entityMetadataRegistry = entityMetadataRegistry;
    }

    public final boolean validate(MethodMetadata method, EntityMetadata entity) {
        boolean valid = true;
        if (!validateReturnType(method, entity)) {
            valid = false;
        }
        if (!validateSignature(method, entity)) {
            valid = false;
        }
        return valid;
    }

    protected abstract boolean validateReturnType(MethodMetadata method, EntityMetadata entity);

    protected abstract boolean validateSignature(MethodMetadata method, EntityMetadata entity);

    protected boolean validateConditionParameters(MethodMetadata method, EntityMetadata entity) {
        boolean valid = true;

        QueryableMethodName queryableName = method.getQueryableName();

        ConditionFragment condition = queryableName.getCondition();
        List<AndPredicate> andPredicates =
                condition.getOrPredicates()
                         .stream()
                         .flatMap(orPredicate -> orPredicate.getAndPredicates().stream())
                         .collect(toList());

        int numberOfConditionParameters =
                andPredicates
                        .stream()
                        .map(AndPredicate::getOperator)
                        .mapToInt(OperatorType::getParameterCount)
                        .sum();

        List<ParameterMetadata> methodParameters = method.getParameters();

        int numberOfMethodParameters = methodParameters.size();
        if (numberOfConditionParameters != numberOfMethodParameters) {
            logger.error("Number of condition parameters (" + numberOfConditionParameters +
                                 ") is not equal to the number of method parameters (" + numberOfMethodParameters + ")",
                         method.getNativeElement());
            return false;
        }

        int parameterIndex = 0;
        for (AndPredicate predicate : andPredicates) {
            AttributeChain attributeChain = predicate.getAttributeChain();
            List<String> attributeNames = attributeChain.getAttributeNames();
            // Predicate must have at least one attribute.
            String attributeName = attributeNames.get(0);

            AttributeMetadata attribute = entity.getAttribute(attributeName);
            if (isNull(attribute)) {
                valid = false;
                logger.error("Condition parameter (" + attributeName + ") not found as entity attribute", method.getNativeElement());
                continue;
            }
            if (attribute instanceof EmbeddedIdentifierAttributeMetadata && attributeNames.size() > 1) {
                valid = false;
                logger.error("Condition parameter (" + attributeName +
                                     ") is embedded identifier attribute which is atomic (nested attributes cannot be used)",
                             method.getNativeElement());
                continue;
            }

            for (int i = 1; i < attributeNames.size(); i++) {
                attributeName = attributeNames.get(i);

                if (attribute instanceof EmbeddedAttributeMetadata) {
                    EmbeddableEntityMetadata embeddableEntity =
                            entityMetadataRegistry.getEmbeddableEntity(attribute.getNativeType().toString());
                    AttributeMetadata innerAttribute = embeddableEntity.getAttribute(attributeName);
                    if (isNull(innerAttribute)) {
                        valid = false;
                        logger.error("Condition parameter (" + attributeName + ") not found as entity attribute",
                                     method.getNativeElement());
                        break;
                    }

                    attribute = innerAttribute;
                }
                else {
                    valid = false;
                    logger.error("Condition parameter (" + attribute.getName() + ") is not an embeddable entity type",
                                 method.getNativeElement());
                    break;
                }
            }

            // Skip parameter and attribute type checking because the last attribute was not found.
            if (!valid) continue;

            TypeMirror attributeType = attribute.getNativeType();

            OperatorType operator = predicate.getOperator();
            int parameterCount = parameterIndex + operator.getParameterCount();
            while (parameterIndex < parameterCount) {
                ParameterMetadata methodParameter = methodParameters.get(parameterIndex);
                TypeMirror parameterType = methodParameter.getNativeType();

                switch (operator) {
                    case IN:
                    case NOT_IN:
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
                                            logger.error("Method parameter (" + methodParameter.getName() +
                                                                 ") has an incompatible array type with entity attribute type (" +
                                                                 attributeName + ")",
                                                         methodParameter.getNativeElement());
                                        }
                                    }
                                    else if (attributeType.getKind().isPrimitive()) {
                                        PrimitiveType primitiveType = typeUtils.getPrimitiveType(attributeType.getKind());
                                        attributeType = typeUtils.boxedClass(primitiveType).asType();
                                        if (!typeUtils.isSameType(parameterType, attributeType)) {
                                            valid = false;
                                            logger.error("Method parameter (" + methodParameter.getName() +
                                                                 ") has an incompatible array type with entity attribute type (" +
                                                                 attributeName + ")",
                                                         methodParameter.getNativeElement());
                                        }
                                    }
                                    else {
                                        valid = false;
                                        logger.error("Method parameter (" + methodParameter.getName() +
                                                             ") has an incompatible array type with entity attribute type (" +
                                                             attributeName + ")",
                                                     methodParameter.getNativeElement());
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
                                                logger.error("Method parameter (" + methodParameter.getName() +
                                                                     ") has an incompatible argument type in collection with entity attribute type (" +
                                                                     attributeName + ")",
                                                             methodParameter.getNativeElement());
                                            }
                                        }
                                        else {
                                            valid = false;
                                            logger.error("Method parameter (" + methodParameter.getName() +
                                                                 ") has an incompatible argument type in collection with entity attribute type (" +
                                                                 attributeName + ")",
                                                         methodParameter.getNativeElement());
                                        }
                                    }
                                }
                                else {
                                    valid = false;
                                    logger.error("Condition parameter with the predicate (" + operator.getKeyword() +
                                                         ") must match the array or collection type in the method parameter",
                                                 methodParameter.getNativeElement());
                                }
                                break;
                            default:
                                valid = false;
                                logger.error("Condition parameter with the predicate (" + operator.getKeyword() +
                                                     ") must match the array or collection type in the method parameter",
                                             methodParameter.getNativeElement());
                                break;
                        }
                        break;
                    case STARTING_WITH:
                    case ENDING_WITH:
                    case CONTAINING:
                    case NOT_CONTAINING:
                    case LIKE:
                    case NOT_LIKE:
                        if (attributeType.getKind() == TypeKind.DECLARED) {
                            DeclaredType declaredType = (DeclaredType) attributeType;
                            String qualifiedName = ((TypeElement) declaredType.asElement()).getQualifiedName().toString();
                            if (!qualifiedName.equals(String.class.getName())) {
                                logger.mandatoryWarn("Operator '" + operator.getKeyword() +
                                                             "' cannot be used with not string entity attribute (" +
                                                             attributeType + " " + attributeName + ")",
                                                     method.getNativeElement());
                            }
                        }
                        else {
                            logger.mandatoryWarn("Operator '" + operator.getKeyword() +
                                                         "' cannot be used with not string entity attribute (" +
                                                         attributeType + " " + attributeName + ")",
                                                 method.getNativeElement());
                        }
                        break;
                    case BEFORE:
                    case AFTER:
                        if (attributeType.getKind() == TypeKind.DECLARED) {
                            DeclaredType declaredType = (DeclaredType) attributeType;
                            String qualifiedName = ((TypeElement) declaredType.asElement()).getQualifiedName().toString();
                            if (!qualifiedName.equals(Date.class.getName()) &&
                                !qualifiedName.equals(Time.class.getName()) &&
                                !qualifiedName.equals(Timestamp.class.getName()) &&
                                !qualifiedName.equals(Instant.class.getName()) &&
                                !qualifiedName.equals(LocalDate.class.getName()) &&
                                !qualifiedName.equals(LocalTime.class.getName()) &&
                                !qualifiedName.equals(LocalDateTime.class.getName())) {
                                logger.mandatoryWarn("Operator '" + operator.getKeyword() +
                                                             "' cannot be used with not temporary entity attribute (" +
                                                             attributeType + " " + attributeName + ")",
                                                     method.getNativeElement());
                            }
                        }
                        else {
                            logger.mandatoryWarn("Operator '" + operator.getKeyword() +
                                                         "' cannot be used with not temporary entity attribute (" +
                                                         attributeType + " " + attributeName + ")",
                                                 method.getNativeElement());
                        }
                        break;
                    default:
                        if (!typeUtils.isSameType(parameterType, attributeType)) {
                            if (parameterType.getKind().isPrimitive()) {
                                PrimitiveType primitiveType = typeUtils.getPrimitiveType(parameterType.getKind());
                                parameterType = typeUtils.boxedClass(primitiveType).asType();
                                if (!typeUtils.isSameType(parameterType, attributeType)) {
                                    valid = false;
                                    logger.error("Method parameter (" + methodParameter.getName() +
                                                         ") has an incompatible type with entity attribute type (" +
                                                         attributeName + ")",
                                                 methodParameter.getNativeElement());
                                }
                            }
                            else if (attributeType.getKind().isPrimitive()) {
                                PrimitiveType primitiveType = typeUtils.getPrimitiveType(attributeType.getKind());
                                attributeType = typeUtils.boxedClass(primitiveType).asType();
                                if (!typeUtils.isSameType(parameterType, attributeType)) {
                                    valid = false;
                                    logger.error("Method parameter (" + methodParameter.getName() +
                                                         ") has an incompatible type with entity attribute type (" +
                                                         attributeName + ")",
                                                 methodParameter.getNativeElement());
                                }
                                else {
                                    logger.mandatoryWarn("Method parameter (" + methodParameter.getName() +
                                                                 ") can be primitive because entity attribute (" +
                                                                 attributeName + ") is primitive",
                                                         methodParameter.getNativeElement());
                                }
                            }
                            else {
                                valid = false;
                                logger.error("Method parameter (" + methodParameter.getName() +
                                                     ") has an incompatible type with entity attribute type (" +
                                                     attributeName + ")",
                                             methodParameter.getNativeElement());
                            }
                        }
                        break;
                }

                parameterIndex++;
            }

            switch (operator) {
                case EMPTY:
                case NOT_EMPTY:
                    if (attributeType.getKind() == TypeKind.DECLARED) {
                        DeclaredType declaredType = (DeclaredType) attributeType;
                        String qualifiedName = ((TypeElement) declaredType.asElement()).getQualifiedName().toString();
                        if (!qualifiedName.equals(String.class.getName())) {
                            logger.mandatoryWarn("Operator '" + operator.getKeyword() +
                                                         "' cannot be used with not string entity attribute (" +
                                                         attributeType + " " + attributeName + ")",
                                                 method.getNativeElement());
                        }
                    }
                    else {
                        logger.mandatoryWarn("Operator '" + operator.getKeyword() +
                                                     "' cannot be used with not string entity attribute (" +
                                                     attributeType + " " + attributeName + ")",
                                             method.getNativeElement());
                    }
                    break;
                case TRUE:
                case FALSE:
                    if (attributeType.getKind() != TypeKind.BOOLEAN) {
                        if (attributeType.getKind() == TypeKind.DECLARED) {
                            DeclaredType declaredType = (DeclaredType) attributeType;
                            String qualifiedName = ((TypeElement) declaredType.asElement()).getQualifiedName().toString();
                            if (!qualifiedName.equals(Boolean.class.getName())) {
                                logger.mandatoryWarn("Operator '" + operator.getKeyword() +
                                                             "' cannot be used with not boolean entity attribute (" +
                                                             attributeType + " " + attributeName + ")",
                                                     method.getNativeElement());
                            }
                        }
                        else {
                            logger.mandatoryWarn("Operator '" + operator.getKeyword() +
                                                         "' cannot be used with not boolean entity attribute (" +
                                                         attributeType + " " + attributeName + ")",
                                                 method.getNativeElement());
                        }
                    }
                    break;
                case NULL:
                case NOT_NULL:
                    if (attributeType.getKind().isPrimitive()) {
                        logger.mandatoryWarn("Operator '" + operator.getKeyword() +
                                                     "' cannot be used with primitive entity attribute (" +
                                                     attributeType + " " + attributeName + ")",
                                             method.getNativeElement());
                    }
                    break;
            }
        }
        return valid;
    }
}
