package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.graphine.processor.code.renderer.ResultSetParameterRenderer;
import io.graphine.processor.code.renderer.parameter.NumericParameterIndexProvider;
import io.graphine.processor.code.renderer.parameter.ParameterIndexProvider;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.query.model.NativeQuery;
import io.graphine.processor.query.model.parameter.Parameter;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.*;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryFindMethodImplementationGenerator extends RepositoryMethodImplementationGenerator {
    @Override
    protected CodeBlock renderResultSetParameters(MethodMetadata method, NativeQuery query) {
        CodeBlock.Builder builder = CodeBlock.builder();

        ExecutableElement methodElement = method.getNativeElement();
        TypeMirror returnType = methodElement.getReturnType();
        switch (returnType.getKind()) {
            case ARRAY:
                ArrayType arrayType = (ArrayType) returnType;
                TypeMirror componentType = arrayType.getComponentType();
                builder.addStatement("$T $L = new $T<>()",
                                     ParameterizedTypeName.get(ClassName.get(Collection.class),
                                                               TypeName.get(componentType)),
                                     "elements",
                                     ArrayList.class);
                break;
            case DECLARED:
                DeclaredType declaredType = (DeclaredType) returnType;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                switch (typeElement.getQualifiedName().toString()) {
                    case "java.lang.Iterable":
                        TypeMirror genericType = declaredType.getTypeArguments().get(0);
                        builder.addStatement("$T $L = new $T<>()",
                                             ParameterizedTypeName.get(ClassName.get(Collection.class),
                                                                       TypeName.get(genericType)),
                                             "elements",
                                             ArrayList.class);
                        break;
                    case "java.util.Collection":
                    case "java.util.List":
                        builder.addStatement("$T $L = new $T<>()",
                                             ParameterizedTypeName.get(returnType), "elements", ArrayList.class);
                        break;
                    case "java.util.Set":
                        builder.addStatement("$T $L = new $T<>()",
                                             ParameterizedTypeName.get(returnType), "elements", HashSet.class);
                        break;
                }
                break;
        }

        switch (returnType.getKind()) {
            case ARRAY:
                builder.beginControlFlow("while (resultSet.next())");
                break;
            case DECLARED:
                DeclaredType declaredType = (DeclaredType) returnType;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                switch (typeElement.getQualifiedName().toString()) {
                    case "java.lang.Iterable":
                    case "java.util.Collection":
                    case "java.util.List":
                    case "java.util.Set":
                        builder.beginControlFlow("while (resultSet.next())");
                        break;
                    default:
                        builder.beginControlFlow("if (resultSet.next())");
                        break;
                }
                break;
        }

        ParameterIndexProvider parameterIndexProvider = new NumericParameterIndexProvider();

        List<Parameter> producedParameters = query.getProducedParameters();
        for (Parameter parameter : producedParameters) {
            builder.add(parameter.accept(
                    new ResultSetParameterRenderer(code -> {
                        switch (returnType.getKind()) {
                            case ARRAY:
                                return CodeBlock.builder()
                                                .addStatement("elements.add($L)", code)
                                                .build();
                            case DECLARED:
                                DeclaredType declaredType = (DeclaredType) returnType;
                                TypeElement typeElement = (TypeElement) declaredType.asElement();
                                switch (typeElement.getQualifiedName().toString()) {
                                    case "java.util.Optional":
                                        return CodeBlock.builder()
                                                        .addStatement("return $T.of($L)", Optional.class, code)
                                                        .build();
                                    case "java.lang.Iterable":
                                    case "java.util.Collection":
                                    case "java.util.List":
                                    case "java.util.Set":
                                        return CodeBlock.builder()
                                                        .addStatement("elements.add($L)", code)
                                                        .build();
                                    default:
                                        return CodeBlock.builder()
                                                        .addStatement("return $L", code)
                                                        .build();
                                }
                            default:
                                return CodeBlock.builder().build();
                        }
                    }, parameterIndexProvider)
            ));
        }

        builder.endControlFlow();

        switch (returnType.getKind()) {
            case ARRAY:
                ArrayType arrayType = (ArrayType) returnType;
                TypeMirror componentType = arrayType.getComponentType();
                builder.addStatement("return elements.toArray(new $T[0])", componentType);
                break;
            case DECLARED:
                DeclaredType declaredType = (DeclaredType) returnType;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                switch (typeElement.getQualifiedName().toString()) {
                    case "java.util.Optional":
                        builder.addStatement("return $T.empty()", Optional.class);
                        break;
                    case "java.lang.Iterable":
                    case "java.util.Collection":
                    case "java.util.List":
                    case "java.util.Set":
                        builder.addStatement("return elements");
                        break;
                    default:
                        builder.addStatement("return null");
                        break;
                }
                break;
        }

        return builder.build();
    }
}
