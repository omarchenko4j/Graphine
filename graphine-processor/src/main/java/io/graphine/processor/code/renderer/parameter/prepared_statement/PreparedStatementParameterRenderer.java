package io.graphine.processor.code.renderer.parameter.prepared_statement;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.parameter.index_provider.ParameterIndexProvider;
import io.graphine.processor.query.model.parameter.Parameter;
import io.graphine.processor.query.model.parameter.ParameterVisitor;

import java.util.function.Function;

/**
 * @author Oleg Marchenko
 */
public abstract class PreparedStatementParameterRenderer implements ParameterVisitor<CodeBlock> {
    public static final String DEFAULT_STATEMENT_VARIABLE_NAME = "statement";

    protected final ParameterIndexProvider parameterIndexProvider;
    protected final Function<Parameter, CodeBlock> parameterNameMapper;

    public PreparedStatementParameterRenderer(ParameterIndexProvider parameterIndexProvider) {
        this(parameterIndexProvider, parameter -> CodeBlock.of(parameter.getName()));
    }

    protected PreparedStatementParameterRenderer(ParameterIndexProvider parameterIndexProvider,
                                                 Function<Parameter, CodeBlock> parameterNameMapper) {
        this.parameterIndexProvider = parameterIndexProvider;
        this.parameterNameMapper = parameterNameMapper;
    }
}
