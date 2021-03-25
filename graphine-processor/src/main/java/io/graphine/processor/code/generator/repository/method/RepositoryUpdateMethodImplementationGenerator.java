package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.PreparedStatementAddBatchMethodRenderer;
import io.graphine.processor.code.renderer.PreparedStatementExecuteMethodRenderer;
import io.graphine.processor.code.renderer.parameter.NumericParameterIndexProvider;
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
                        .addStatement("String query = $S", query.getValue())
                        .build();
    }

    @Override
    protected CodeBlock renderStatementParameters(MethodMetadata method, NativeQuery query) {
        CodeBlock.Builder builder = CodeBlock.builder();

        Parameter consumedParameter = query.getConsumedParameters().get(0);
        builder.add(consumedParameter.accept(
                new PreparedStatementAddBatchMethodRenderer(new NumericParameterIndexProvider()))
        );
        builder.add(consumedParameter.accept(new PreparedStatementExecuteMethodRenderer()));

        return builder.build();
    }

    @Override
    protected CodeBlock renderResultSet(MethodMetadata method, NativeQuery query) {
        return CodeBlock.builder().build();
    }
}
