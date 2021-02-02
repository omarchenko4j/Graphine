package io.graphine.processor.metadata.validator;

import io.graphine.core.annotation.Attribute;
import io.graphine.core.annotation.Id;
import io.graphine.processor.util.MethodUtils;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.graphine.processor.support.EnvironmentContext.messager;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.util.ElementFilter.fieldsIn;
import static javax.lang.model.util.ElementFilter.methodsIn;
import static javax.tools.Diagnostic.Kind;

/**
 * @author Oleg Marchenko
 */
public final class EntityMetadataValidator {
    public boolean validate(TypeElement element) {
        boolean valid = true;

        if (element.getKind() != CLASS) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Entity must be a class", element);
        }

        Set<Modifier> modifiers = element.getModifiers();
        if (!modifiers.contains(PUBLIC)) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Entity class must be public", element);
        }
        if (modifiers.contains(ABSTRACT)) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Entity class should not be abstract", element);
        }

        List<VariableElement> fields = fieldsIn(element.getEnclosedElements());

        List<VariableElement> identifiers = fields
                .stream()
                .filter(field -> nonNull(field.getAnnotation(Id.class)))
                .collect(toList());
        if (identifiers.isEmpty()) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Entity class must have identifier", element);
        }
        else if (identifiers.size() > 1) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Entity class must have one identifier", element);
        }

        List<ExecutableElement> methods = methodsIn(element.getEnclosedElements());
        Map<String, ExecutableElement> methodNameToMethodMap = methods
                .stream()
                .collect(toMap(method -> method.getSimpleName().toString(), identity()));

        List<VariableElement> attributes = fields
                .stream()
                .filter(field -> nonNull(field.getAnnotation(Attribute.class)))
                .collect(toList());
        attributes.addAll(identifiers);
        for (VariableElement attribute : attributes) {
            String attributeName = attribute.getSimpleName().toString();

            String getterName = MethodUtils.getter(attributeName);
            String setterName = MethodUtils.setter(attributeName);

            boolean hasGetter = methodNameToMethodMap.containsKey(getterName);
            boolean hasSetter = methodNameToMethodMap.containsKey(setterName);

            if (!hasGetter && !hasSetter) {
                valid = false;
                messager.printMessage(Kind.ERROR, "Attribute must have a getter and setter", attribute);
            }
            else if (!hasGetter) {
                valid = false;
                messager.printMessage(Kind.ERROR, "Attribute must have a getter", attribute);
            }
            else if (!hasSetter) {
                valid = false;
                messager.printMessage(Kind.ERROR, "Attribute must have a setter", attribute);
            }

            ExecutableElement getter = methodNameToMethodMap.get(getterName);
            ExecutableElement setter = methodNameToMethodMap.get(setterName);

            if (!getter.getReturnType().equals(attribute.asType())) {
                valid = false;
                messager.printMessage(Kind.ERROR, "Getter must have the same return type as the attribute type", getter);
            }
            if (!getter.getParameters().isEmpty()) {
                valid = false;
                messager.printMessage(Kind.ERROR, "Getter must have no parameters", getter);
            }

            if (setter.getParameters().size() != 1) {
                valid = false;
                messager.printMessage(Kind.ERROR, "Setter must have one parameter", setter);
            }
            else {
                VariableElement parameter = setter.getParameters().iterator().next();
                if (!parameter.asType().equals(attribute.asType())) {
                    valid = false;
                    messager.printMessage(Kind.ERROR, "Setter parameter type must be the same as the attribute type", setter);
                }
            }
        }

        return valid;
    }
}
