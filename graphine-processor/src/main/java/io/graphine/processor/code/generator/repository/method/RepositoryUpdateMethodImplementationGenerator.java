package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.parameter.index_provider.NumericParameterIndexProvider;
import io.graphine.processor.code.renderer.parameter.prepared_statement.PreparedStatementParameterHighLevelRenderer;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.query.model.NativeQuery;
import io.graphine.processor.query.model.parameter.Parameter;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryUpdateMethodImplementationGenerator extends RepositoryMethodImplementationGenerator {
    @Override
    protected CodeBlock renderQuery(NativeQuery query) {
        return CodeBlock.builder()
                        .addStatement("String $L = $S", QUERY_VARIABLE_NAME, query.getValue())
                        .build();
    }

    @Override
    protected CodeBlock renderStatementParameters(MethodMetadata method, NativeQuery query) {
        Parameter consumedParameter = query.getConsumedParameters().get(0);
        return CodeBlock.builder()
                        .add(consumedParameter.accept(
                                new PreparedStatementParameterHighLevelRenderer(new NumericParameterIndexProvider())
                        ))
                        .build();
    }

    @Override
    protected CodeBlock renderResultSet(MethodMetadata method, NativeQuery query) {
        return CodeBlock.builder().build();
    }
}
