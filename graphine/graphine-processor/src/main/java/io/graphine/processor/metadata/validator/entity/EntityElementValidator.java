package io.graphine.processor.metadata.validator.entity;

import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.IdentifierAttributeMetadata;
import io.graphine.processor.support.AttributeDetectionStrategy;
import io.graphine.processor.util.AccessorUtils;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.*;

import static io.graphine.processor.support.EnvironmentContext.logger;
import static io.graphine.processor.support.EnvironmentContext.typeUtils;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.util.ElementFilter.*;

/**
 * @author Oleg Marchenko
 */
public final class EntityElementValidator {
    public boolean validate(TypeElement entityElement) {
        boolean valid = true;

        if (entityElement.getKind() != CLASS) {
            valid = false;
            logger.error("Entity must be a class", entityElement);
        }

        Set<Modifier> modifiers = entityElement.getModifiers();
        if (!modifiers.contains(PUBLIC)) {
            valid = false;
            logger.error("Entity class must be public", entityElement);
        }
        if (modifiers.contains(ABSTRACT)) {
            valid = false;
            logger.error("Entity class should not be abstract", entityElement);
        }

        List<VariableElement> fields = fieldsIn(entityElement.getEnclosedElements());

        long numberOfIdentifiers = fields
                .stream()
                .filter(IdentifierAttributeMetadata::isIdentifier)
                .count();
        if (numberOfIdentifiers == 0) {
            valid = false;
            logger.error("Entity class must have identifier", entityElement);
        }
        else if (numberOfIdentifiers > 1) {
            valid = false;
            logger.error("Entity class must have one identifier", entityElement);
        }

        List<ExecutableElement> constructors = constructorsIn(entityElement.getEnclosedElements());

        long numberOfDefaultConstructors = constructors
                .stream()
                .filter(constructor -> constructor.getModifiers().contains(PUBLIC))
                .filter(constructor -> constructor.getParameters().isEmpty())
                .count();
        if (numberOfDefaultConstructors == 0) {
            valid = false;
            logger.error("Entity class must have a default constructor", entityElement);
        }

        List<ExecutableElement> methods = methodsIn(entityElement.getEnclosedElements());

        Map<String, List<ExecutableElement>> methodNameToMethods = new HashMap<>(methods.size() + 1, 1);
        for (ExecutableElement method : methods) {
            String methodName = method.getSimpleName().toString();
            methodNameToMethods.computeIfAbsent(methodName, m -> new ArrayList<>(4))
                               .add(method);
        }

        boolean detectOnlyAnnotatedFields = AttributeDetectionStrategy.onlyAnnotatedFields();
        for (VariableElement field : fields) {
            if (detectOnlyAnnotatedFields && !AttributeMetadata.isAttribute(field)) continue;

            String getterName = AccessorUtils.getter(field);
            String setterName = AccessorUtils.setter(field);

            boolean hasGetter = methodNameToMethods.containsKey(getterName);
            boolean hasSetter = methodNameToMethods.containsKey(setterName);

            if (!hasGetter && !hasSetter) {
                valid = false;
                logger.error("Attribute must have a getter and setter", field);
            }
            else if (!hasGetter) {
                valid = false;
                logger.error("Attribute must have a getter", field);
            }
            else if (!hasSetter) {
                valid = false;
                logger.error("Attribute must have a setter", field);
            }

            if (hasGetter) {
                List<ExecutableElement> getters = methodNameToMethods.get(getterName);

                boolean getterMatched =
                        getters.stream()
                               .anyMatch(getter -> typeUtils.isSameType(getter.getReturnType(), field.asType()) &&
                                                   getter.getParameters().isEmpty());
                if (!getterMatched) {
                    valid = false;
                    logger.error("Attribute must have a getter with the same return value and no parameters", field);
                }
            }

            if (hasSetter) {
                List<ExecutableElement> setters = methodNameToMethods.get(setterName);

                boolean setterMatched =
                        setters.stream()
                               .anyMatch(setter -> {
                                   if (setter.getParameters().size() == 1) {
                                       VariableElement parameter = setter.getParameters().iterator().next();
                                       return typeUtils.isSameType(parameter.asType(), field.asType());
                                   }
                                   return false;
                               });
                if (!setterMatched) {
                    valid = false;
                    logger.error("Attribute must have a setter with one parameter and with the same type", field);
                }
            }
        }

        return valid;
    }
}
