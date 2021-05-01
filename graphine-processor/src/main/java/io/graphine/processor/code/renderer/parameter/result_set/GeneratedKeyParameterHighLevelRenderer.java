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

import static io.graphine.processor.util.VariableNameUniqueizer.uniqueize;

/**
 * @author Oleg Marchenko
 */
public final class GeneratedKeyParameterHighLevelRenderer extends ResultSetParameterRenderer {
    public static final String GENERATED_KEY_VARIABLE_NAME = uniqueize("generatedKeys");
    public static final String ITERATOR_VARIABLE_NAME = uniqueize("iterator");

    public GeneratedKeyParameterHighLevelRenderer(ParameterIndexProvider parameterIndexProvider) {
        this(Function.identity(), parameterIndexProvider);
    }

    public GeneratedKeyParameterHighLevelRenderer(Function<CodeBlock, CodeBlock> snippetMerger,
                                                  ParameterIndexProvider parameterIndexProvider) {
        super(snippetMerger, GENERATED_KEY_VARIABLE_NAME, parameterIndexProvider);
    }

    @Override
    public CodeBlock visit(Parameter parameter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CodeBlock visit(ComplexParameter parameter) {
        return CodeBlock.builder()
                        .beginControlFlow("if ($L.next())", resultSetVariableName)
                        .add(parameter.accept(
                                new ResultSetParameterLowLevelRenderer(snippetMerger,
                                                                       GENERATED_KEY_VARIABLE_NAME,
                                                                       parameterIndexProvider)
                        ))
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
                        .add(iteratedParameter.accept(
                                new ResultSetParameterLowLevelRenderer(snippetMerger,
                                                                       GENERATED_KEY_VARIABLE_NAME,
                                                                       parameterIndexProvider)
                        ))
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
                                .addStatement("$T<$T> $L = $L.iterator()",
                                              Iterator.class,
                                              iteratedParameter.getType(),
                                              ITERATOR_VARIABLE_NAME,
                                              parameter.getName())
                                .beginControlFlow("while ($L.next() && $L.hasNext())",
                                                  resultSetVariableName, ITERATOR_VARIABLE_NAME)
                                .addStatement("$T $L = $L.next()",
                                              iteratedParameter.getType(),
                                              iteratedParameter.getName(),
                                              ITERATOR_VARIABLE_NAME)
                                .add(iteratedParameter.accept(
                                        new ResultSetParameterLowLevelRenderer(snippetMerger,
                                                                               GENERATED_KEY_VARIABLE_NAME,
                                                                               parameterIndexProvider)
                                ))
                                .endControlFlow();
                        break;
                }
                break;
        }

        return builder.build();
    }
}
