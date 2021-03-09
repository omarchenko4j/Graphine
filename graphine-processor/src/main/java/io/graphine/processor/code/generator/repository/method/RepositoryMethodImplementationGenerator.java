package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.MethodSpec;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.query.model.NativeQuery;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.Optional;

/**
 * @author Oleg Marchenko
 */
public abstract class RepositoryMethodImplementationGenerator {
    protected final EntityMetadata entity;

    protected RepositoryMethodImplementationGenerator(EntityMetadata entity) {
        this.entity = entity;
    }

    public MethodSpec generate(MethodMetadata method, NativeQuery query) {
        ExecutableElement methodElement = method.getNativeElement();

        MethodSpec.Builder methodBuilder = MethodSpec.overriding(methodElement);

        TypeMirror returnType = methodElement.getReturnType();
        switch (returnType.getKind()) {
            case INT:
            case LONG:
                methodBuilder.addStatement("return 0");
                break;
            case DECLARED:
                DeclaredType declaredType = (DeclaredType) returnType;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                switch (typeElement.getQualifiedName().toString()) {
                    case "java.lang.Integer":
                        methodBuilder.addStatement("return 0");
                        break;
                    case "java.lang.Long":
                        methodBuilder.addStatement("return 0L");
                        break;
                    case "java.util.Optional":
                        methodBuilder.addStatement("return $T.empty()", Optional.class);
                        break;
                    case "java.lang.Iterable":
                    case "java.util.Collection":
                    case "java.util.List":
                        methodBuilder.addStatement("return $T.emptyList()", Collections.class);
                        break;
                    case "java.util.Set":
                        methodBuilder.addStatement("return $T.emptySet()", Collections.class);
                        break;
                    default:
                        methodBuilder.addStatement("return null");
                        break;
                }
        }

        return methodBuilder.build();
    }
}
