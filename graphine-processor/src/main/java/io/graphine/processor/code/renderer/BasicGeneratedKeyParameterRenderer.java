package io.graphine.processor.code.renderer;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.parameter.ParameterIndexProvider;
import io.graphine.processor.query.model.parameter.ComplexParameter;
import io.graphine.processor.query.model.parameter.Parameter;

import java.util.List;
import java.util.function.Function;

import static io.graphine.processor.util.MethodUtils.setter;

/**
 * @author Oleg Marchenko
 */
public class BasicGeneratedKeyParameterRenderer extends ResultSetParameterRenderer {
    public BasicGeneratedKeyParameterRenderer(ParameterIndexProvider parameterIndexProvider) {
        this(Function.identity(), parameterIndexProvider);
    }

    public BasicGeneratedKeyParameterRenderer(Function<CodeBlock, CodeBlock> snippetMerger,
                                              ParameterIndexProvider parameterIndexProvider) {
        super(snippetMerger, "generatedKeys", parameterIndexProvider);
    }

    @Override
    public CodeBlock visit(ComplexParameter parameter) {
        String parameterName = parameter.getName();

        CodeBlock.Builder builder = CodeBlock.builder();

        List<Parameter> childParameters = parameter.getChildParameters();
        for (Parameter childParameter : childParameters) {
            GeneratedKeyParameterRenderer generatedKeyParameterRenderer =
                    new GeneratedKeyParameterRenderer(snippet -> CodeBlock.builder()
                                                                          .addStatement("$L.$L($L)",
                                                                                        parameterName,
                                                                                        setter(childParameter.getName()),
                                                                                        snippet)
                                                                          .build(),
                                                      parameterIndexProvider);
            builder.add(childParameter.accept(generatedKeyParameterRenderer));
        }

        return builder.build();
    }
}
