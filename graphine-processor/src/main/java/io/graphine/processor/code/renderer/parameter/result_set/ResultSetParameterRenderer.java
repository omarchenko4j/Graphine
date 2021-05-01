package io.graphine.processor.code.renderer.parameter.result_set;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.parameter.index_provider.ParameterIndexProvider;
import io.graphine.processor.query.model.parameter.ParameterVisitor;

import java.util.function.Function;

import static io.graphine.processor.code.generator.repository.method.RepositoryMethodImplementationGenerator.RESULT_SET_VARIABLE_NAME;

/**
 * @author Oleg Marchenko
 */
public abstract class ResultSetParameterRenderer implements ParameterVisitor<CodeBlock> {
    protected final Function<CodeBlock, CodeBlock> snippetMerger;
    protected final String resultSetVariableName;
    protected final ParameterIndexProvider parameterIndexProvider;

    public ResultSetParameterRenderer(Function<CodeBlock, CodeBlock> snippetMerger,
                                      ParameterIndexProvider parameterIndexProvider) {
        this(snippetMerger, RESULT_SET_VARIABLE_NAME, parameterIndexProvider);
    }

    protected ResultSetParameterRenderer(Function<CodeBlock, CodeBlock> snippetMerger,
                                         String resultSetVariableName,
                                         ParameterIndexProvider parameterIndexProvider) {
        this.snippetMerger = snippetMerger;
        this.resultSetVariableName = resultSetVariableName;
        this.parameterIndexProvider = parameterIndexProvider;
    }
}
