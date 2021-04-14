package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.parameter.index_provider.NumericParameterIndexProvider;
import io.graphine.processor.code.renderer.parameter.index_provider.ParameterIndexProvider;
import io.graphine.processor.code.renderer.parameter.result_set.ResultSetParameterHighLevelRenderer;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.query.model.NativeQuery;
import io.graphine.processor.query.model.parameter.Parameter;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryFindMethodImplementationGenerator extends RepositoryMethodImplementationGenerator {
    @Override
    protected CodeBlock renderResultSetParameters(MethodMetadata method, NativeQuery query) {
        CodeBlock.Builder builder = CodeBlock.builder();

        ExecutableElement methodElement = method.getNativeElement();
        TypeMirror returnType = methodElement.getReturnType();

        ParameterIndexProvider parameterIndexProvider = new NumericParameterIndexProvider();

        List<Parameter> producedParameters = query.getProducedParameters();
        for (Parameter parameter : producedParameters) {
            builder.add(parameter.accept(
                    new ResultSetParameterHighLevelRenderer(snippet -> {
                        switch (returnType.getKind()) {
                            case ARRAY:
                                return CodeBlock.builder()
                                                .addStatement("elements.add($L)", snippet)
                                                .build();
                            case DECLARED:
                                DeclaredType declaredType = (DeclaredType) returnType;
                                TypeElement typeElement = (TypeElement) declaredType.asElement();
                                switch (typeElement.getQualifiedName().toString()) {
                                    case "java.util.Optional":
                                        return CodeBlock.builder()
                                                        .addStatement("return $T.of($L)", Optional.class, snippet)
                                                        .build();
                                    case "java.lang.Iterable":
                                    case "java.util.Collection":
                                    case "java.util.List":
                                    case "java.util.Set":
                                        return CodeBlock.builder()
                                                        .addStatement("elements.add($L)", snippet)
                                                        .build();
                                    default:
                                        return CodeBlock.builder()
                                                        .addStatement("return $L", snippet)
                                                        .build();
                                }
                            default:
                                return CodeBlock.builder().build();
                        }
                    }, parameterIndexProvider)
            ));
        }

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
