package io.graphine.processor.code.renderer.parameter.result_set;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.graphine.processor.code.renderer.parameter.index_provider.ParameterIndexProvider;
import io.graphine.processor.query.model.parameter.ComplexParameter;
import io.graphine.processor.query.model.parameter.IterableParameter;
import io.graphine.processor.query.model.parameter.Parameter;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Function;

/**
 * @author Oleg Marchenko
 */
public final class ResultSetParameterHighLevelRenderer extends ResultSetParameterRenderer {
    public ResultSetParameterHighLevelRenderer(Function<CodeBlock, CodeBlock> snippetMerger,
                                               ParameterIndexProvider parameterIndexProvider) {
        super(snippetMerger, parameterIndexProvider);
    }

    @Override
    public CodeBlock visit(Parameter parameter) {
        return CodeBlock.builder()
                        .beginControlFlow("if ($L.next())", resultSetVariableName)
                        .add(parameter.accept(
                                new ResultSetParameterMiddleLevelRenderer(snippetMerger, parameterIndexProvider)
                        ))
                        .endControlFlow()
                        .build();
    }

    @Override
    public CodeBlock visit(ComplexParameter parameter) {
        return CodeBlock.builder()
                        .beginControlFlow("if ($L.next())", resultSetVariableName)
                        .add(parameter.accept(
                                new ResultSetParameterMiddleLevelRenderer(snippetMerger, parameterIndexProvider)
                        ))
                        .endControlFlow()
                        .build();
    }

    @Override
    public CodeBlock visit(IterableParameter parameter) {
        CodeBlock.Builder builder = CodeBlock.builder();

        String parameterName = parameter.getName();
        TypeMirror parameterType = parameter.getType();
        switch (parameterType.getKind()) {
            case ARRAY:
                ArrayType arrayType = (ArrayType) parameterType;
                TypeMirror componentType = arrayType.getComponentType();
                builder.addStatement("$T $L = new $T<>()",
                                     ParameterizedTypeName.get(ClassName.get(Collection.class),
                                                               TypeName.get(componentType)),
                                     parameterName,
                                     ArrayList.class);
                break;
            case DECLARED:
                DeclaredType declaredType = (DeclaredType) parameterType;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                switch (typeElement.getQualifiedName().toString()) {
                    case "java.lang.Iterable":
                    case "java.util.stream.Stream":
                        TypeMirror genericType = declaredType.getTypeArguments().get(0);
                        builder.addStatement("$T $L = new $T<>()",
                                             ParameterizedTypeName.get(ClassName.get(Collection.class),
                                                                       TypeName.get(genericType)),
                                             parameterName,
                                             ArrayList.class);
                        break;
                    case "java.util.Collection":
                    case "java.util.List":
                        builder.addStatement("$T $L = new $T<>()",
                                             ParameterizedTypeName.get(parameterType), parameterName, ArrayList.class);
                        break;
                    case "java.util.Set":
                        builder.addStatement("$T $L = new $T<>()",
                                             ParameterizedTypeName.get(parameterType), parameterName, HashSet.class);
                        break;
                }
                break;
        }

        return builder.beginControlFlow("while ($L.next())", resultSetVariableName)
                      .add(parameter.getIteratedParameter().accept(
                              new ResultSetParameterMiddleLevelRenderer(snippetMerger, parameterIndexProvider)
                      ))
                      .endControlFlow()
                      .build();
    }
}
