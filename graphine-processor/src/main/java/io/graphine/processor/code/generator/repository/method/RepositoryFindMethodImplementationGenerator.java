package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.CodeBlock;
import io.graphine.core.NonUniqueResultException;
import io.graphine.processor.code.renderer.parameter.index_provider.NumericParameterIndexProvider;
import io.graphine.processor.code.renderer.parameter.result_set.ResultSetParameterHighLevelRenderer;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.query.model.NativeQuery;
import io.graphine.processor.query.model.parameter.Parameter;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
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

        Parameter producedParameter = query.getProducedParameters().get(0);
        builder.add(producedParameter.accept(
                new ResultSetParameterHighLevelRenderer(snippet -> {
                    switch (returnType.getKind()) {
                        case ARRAY:
                            return CodeBlock.builder()
                                            .addStatement("$L.add($L)", producedParameter.getName(), snippet)
                                            .build();
                        case DECLARED:
                            DeclaredType declaredType = (DeclaredType) returnType;
                            TypeElement typeElement = (TypeElement) declaredType.asElement();
                            String elementQualifiedName = typeElement.getQualifiedName().toString();
                            switch (elementQualifiedName) {
                                case "java.lang.Iterable":
                                case "java.util.Collection":
                                case "java.util.List":
                                case "java.util.Set":
                                case "java.util.stream.Stream":
                                    return CodeBlock.builder()
                                                    .addStatement("$L.add($L)", producedParameter.getName(), snippet)
                                                    .build();
                                default:
                                    CodeBlock.Builder snippetBuilder = CodeBlock.builder();

                                    QueryableMethodName queryableName = method.getQueryableName();
                                    QualifierFragment qualifier = queryableName.getQualifier();
                                    if (!qualifier.hasFirstSpecifier()) {
                                        snippetBuilder
                                                .beginControlFlow("if ($L.next())", RESULT_SET_VARIABLE_NAME)
                                                .addStatement("throw new $T()", NonUniqueResultException.class)
                                                .endControlFlow();
                                    }

                                    if (elementQualifiedName.equals("java.util.Optional")) {
                                        snippetBuilder
                                                .addStatement("return $T.of($L)", Optional.class, snippet);
                                    }
                                    else {
                                        snippetBuilder
                                                .addStatement("return $L", snippet);
                                    }

                                    return snippetBuilder.build();
                            }
                        default:
                            return CodeBlock.builder().build();
                    }
                }, new NumericParameterIndexProvider())
        ));

        switch (returnType.getKind()) {
            case ARRAY:
                ArrayType arrayType = (ArrayType) returnType;
                TypeMirror componentType = arrayType.getComponentType();
                builder.addStatement("return $L.toArray(new $T[0])", producedParameter.getName(), componentType);
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
                        builder.addStatement("return $L", producedParameter.getName());
                        break;
                    case "java.util.stream.Stream":
                        builder.addStatement("return $L.stream()", producedParameter.getName());
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
