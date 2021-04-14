package io.graphine.processor.code.renderer.parameter.result_set;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.parameter.index_provider.ParameterIndexProvider;
import io.graphine.processor.query.model.parameter.ComplexParameter;
import io.graphine.processor.query.model.parameter.IterableParameter;
import io.graphine.processor.query.model.parameter.Parameter;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Iterator;
import java.util.function.Function;

/**
 * @author Oleg Marchenko
 */
public class GeneratedKeyParameterHighLevelRenderer extends ResultSetParameterRenderer {
    public static final String DEFAULT_GENERATED_KEY_VARIABLE_NAME = "generatedKeys";

    public GeneratedKeyParameterHighLevelRenderer(ParameterIndexProvider parameterIndexProvider) {
        this(Function.identity(), parameterIndexProvider);
    }

    public GeneratedKeyParameterHighLevelRenderer(Function<CodeBlock, CodeBlock> snippetMerger,
                                                  ParameterIndexProvider parameterIndexProvider) {
        super(snippetMerger, DEFAULT_GENERATED_KEY_VARIABLE_NAME, parameterIndexProvider);
    }

    @Override
    public CodeBlock visit(Parameter parameter) {
        return CodeBlock.builder().build();
    }

    @Override
    public CodeBlock visit(ComplexParameter parameter) {
        return CodeBlock.builder()
                        .beginControlFlow("if ($L.next())", resultSetVariableName)
                        .add(parameter.accept(new ResultSetParameterLowLevelRenderer(snippetMerger, DEFAULT_GENERATED_KEY_VARIABLE_NAME, parameterIndexProvider)))
                        .endControlFlow()
                        .build();
    }

    @Override
    public CodeBlock visit(IterableParameter parameter) {
        CodeBlock.Builder builder = CodeBlock.builder();

        Parameter iteratedParameter = parameter.getIteratedParameter();

        TypeMirror parameterType = parameter.getType();
        switch (parameterType.getKind()) {
            case ARRAY:
                builder
                        .addStatement("int i = 0")
                        .beginControlFlow("while ($L.next())", resultSetVariableName)
                        .addStatement("$T $L = $L[i]",
                                      iteratedParameter.getType(), iteratedParameter.getName(), parameter.getName())
                        .add(parameter.accept(new ResultSetParameterLowLevelRenderer(snippetMerger, DEFAULT_GENERATED_KEY_VARIABLE_NAME, parameterIndexProvider)))
                        .addStatement("i++")
                        .endControlFlow();
                break;
            case DECLARED:
                DeclaredType declaredType = (DeclaredType) parameterType;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                switch (typeElement.getQualifiedName().toString()) {
                    case "java.lang.Iterable":
                    case "java.util.Collection":
                    case "java.util.List":
                    case "java.util.Set":
                        builder
                                .addStatement("$T<$T> iterator = $L.iterator()",
                                              Iterator.class, iteratedParameter.getType(), parameter.getName())
                                .beginControlFlow("while ($L.next() && iterator.hasNext())", resultSetVariableName)
                                .addStatement("$T $L = iterator.next()",
                                              iteratedParameter.getType(), iteratedParameter.getName())
                                .add(parameter.accept(new ResultSetParameterLowLevelRenderer(snippetMerger, DEFAULT_GENERATED_KEY_VARIABLE_NAME, parameterIndexProvider)))
                                .endControlFlow();
                        break;
                }
                break;
        }

        return builder.build();
    }
}
