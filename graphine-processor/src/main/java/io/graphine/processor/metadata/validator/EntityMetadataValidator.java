package io.graphine.processor.metadata.validator;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.IdentifierMetadata;
import io.graphine.processor.util.MethodUtils;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.graphine.processor.support.EnvironmentContext.messager;
import static io.graphine.processor.support.EnvironmentContext.typeUtils;
import static java.util.Objects.isNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.Modifier.*;
import static javax.lang.model.util.ElementFilter.*;
import static javax.tools.Diagnostic.Kind;

/**
 * @author Oleg Marchenko
 */
public final class EntityMetadataValidator {
    public boolean validate(Collection<EntityMetadata> entities) {
        boolean valid = true;
        for (EntityMetadata entity : entities) {
            if (!validate(entity)) {
                valid = false;
            }
        }
        return valid;
    }

    public boolean validate(EntityMetadata entity) {
        boolean valid = true;

        TypeElement entityElement = entity.getNativeElement();
        if (entityElement.getKind() != CLASS) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Entity must be a class", entityElement);
        }

        Set<Modifier> modifiers = entityElement.getModifiers();
        if (!modifiers.contains(PUBLIC)) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Entity class must be public", entityElement);
        }
        if (modifiers.contains(ABSTRACT)) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Entity class should not be abstract", entityElement);
        }

        List<VariableElement> fields = fieldsIn(entityElement.getEnclosedElements());
        List<VariableElement> identifiers = fields
                .stream()
                .filter(IdentifierMetadata::isIdentifier)
                .collect(toList());
        if (identifiers.isEmpty()) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Entity class must have identifier", entityElement);
        }
        else if (identifiers.size() > 1) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Entity class must have one identifier", entityElement);
        }

        List<ExecutableElement> constructors = constructorsIn(entityElement.getEnclosedElements());
        ExecutableElement basicConstructor = constructors
                .stream()
                .filter(constructor -> constructor.getModifiers().contains(PUBLIC))
                .filter(constructor -> constructor.getParameters().isEmpty())
                .findFirst()
                .orElse(null);
        if (isNull(basicConstructor)) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Entity class must have a public constructor without parameters", entityElement);
        }

        List<ExecutableElement> methods = methodsIn(entityElement.getEnclosedElements());

        Map<String, ExecutableElement> methodNameToMethodMap = methods
                .stream()
                .collect(toMap(method -> method.getSimpleName().toString(), identity(), (e1, e2) -> e1));

        List<AttributeMetadata> attributes = entity.getAttributes();
        for (AttributeMetadata attribute : attributes) {
            VariableElement attributeElement = attribute.getNativeElement();
            if (attributeElement.getModifiers().contains(FINAL)) {
                valid = false;
                messager.printMessage(Kind.ERROR, "Attribute must not be final", attributeElement);
            }

            String getterName = MethodUtils.getter(attributeElement);
            String setterName = MethodUtils.setter(attributeElement);

            boolean hasGetter = methodNameToMethodMap.containsKey(getterName);
            boolean hasSetter = methodNameToMethodMap.containsKey(setterName);

            if (!hasGetter && !hasSetter) {
                valid = false;
                messager.printMessage(Kind.ERROR, "Attribute must have a getter and setter", attributeElement);
            }
            else if (!hasGetter) {
                valid = false;
                messager.printMessage(Kind.ERROR, "Attribute must have a getter", attributeElement);
            }
            else if (!hasSetter) {
                valid = false;
                messager.printMessage(Kind.ERROR, "Attribute must have a setter", attributeElement);
            }

            if (hasGetter) {
                ExecutableElement getter = methodNameToMethodMap.get(getterName);
                if (!typeUtils.isSameType(getter.getReturnType(), attributeElement.asType())) {
                    valid = false;
                    messager.printMessage(Kind.ERROR, "Getter must have the same return type as the attribute type", getter);
                }
                if (!getter.getParameters().isEmpty()) {
                    valid = false;
                    messager.printMessage(Kind.ERROR, "Getter must have no parameters", getter);
                }
            }

            if (hasSetter) {
                ExecutableElement setter = methodNameToMethodMap.get(setterName);
                if (setter.getParameters().size() != 1) {
                    valid = false;
                    messager.printMessage(Kind.ERROR, "Setter must have one parameter", setter);
                }
                else {
                    VariableElement parameter = setter.getParameters().iterator().next();
                    if (!typeUtils.isSameType(parameter.asType(), attributeElement.asType())) {
                        valid = false;
                        messager.printMessage(Kind.ERROR, "Setter parameter type must be the same as the attribute type", setter);
                    }
                }
            }
        }

        return valid;
    }
}
