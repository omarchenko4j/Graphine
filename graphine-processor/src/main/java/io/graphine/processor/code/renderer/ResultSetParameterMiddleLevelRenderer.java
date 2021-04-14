package io.graphine.processor.code.renderer;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.parameter.ParameterIndexProvider;
import io.graphine.processor.query.model.parameter.ComplexParameter;
import io.graphine.processor.query.model.parameter.IterableParameter;
import io.graphine.processor.query.model.parameter.Parameter;

import javax.lang.model.type.TypeMirror;
import java.util.function.Function;

/**
 * @author Oleg Marchenko
 */
public class ResultSetParameterMiddleLevelRenderer extends ResultSetParameterRenderer {
    public ResultSetParameterMiddleLevelRenderer(Function<CodeBlock, CodeBlock> snippetMerger,
                                                 ParameterIndexProvider parameterIndexProvider) {
        super(snippetMerger, parameterIndexProvider);
    }

    @Override
    public CodeBlock visit(Parameter parameter) {
        return parameter.accept(new ResultSetParameterLowLevelRenderer(snippetMerger, parameterIndexProvider));
    }

    @Override
    public CodeBlock visit(ComplexParameter parameter) {
        String parameterName = parameter.getName();
        TypeMirror parameterType = parameter.getType();
        return CodeBlock.builder()
                        .addStatement("$T $L = new $T()",
                                      parameterType, parameterName, parameterType)
                        .add(parameter.accept(new ResultSetParameterLowLevelRenderer(snippetMerger, parameterIndexProvider)))
                        .add(snippetMerger.apply(CodeBlock.of("$L", parameterName)))
                        .build();
    }

    @Override
    public CodeBlock visit(IterableParameter parameter) {
        return parameter.getIteratedParameter().accept(this);
    }
}
