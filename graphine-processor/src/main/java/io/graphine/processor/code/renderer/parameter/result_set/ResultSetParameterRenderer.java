package io.graphine.processor.code.renderer.parameter.result_set;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.parameter.index_provider.ParameterIndexProvider;
import io.graphine.processor.query.model.parameter.ParameterVisitor;

import java.util.function.Function;

/**
 * @author Oleg Marchenko
 */
public abstract class ResultSetParameterRenderer implements ParameterVisitor<CodeBlock> {
    public static final String DEFAULT_RESULT_SET_VARIABLE_NAME = "resultSet";

    protected final Function<CodeBlock, CodeBlock> snippetMerger;
    protected final String resultSetVariableName;
    protected final ParameterIndexProvider parameterIndexProvider;

    public ResultSetParameterRenderer(Function<CodeBlock, CodeBlock> snippetMerger,
                                      ParameterIndexProvider parameterIndexProvider) {
        this(snippetMerger, DEFAULT_RESULT_SET_VARIABLE_NAME, parameterIndexProvider);
    }

    protected ResultSetParameterRenderer(Function<CodeBlock, CodeBlock> snippetMerger,
                                         String resultSetVariableName,
                                         ParameterIndexProvider parameterIndexProvider) {
        this.snippetMerger = snippetMerger;
        this.resultSetVariableName = resultSetVariableName;
        this.parameterIndexProvider = parameterIndexProvider;
    }
}
