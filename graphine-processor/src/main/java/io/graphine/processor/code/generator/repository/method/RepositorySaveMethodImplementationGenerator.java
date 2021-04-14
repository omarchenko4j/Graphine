package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.GeneratedKeyParameterHighLevelRenderer;
import io.graphine.processor.code.renderer.PreparedStatementAddBatchMethodRenderer;
import io.graphine.processor.code.renderer.PreparedStatementExecuteMethodRenderer;
import io.graphine.processor.code.renderer.parameter.NumericParameterIndexProvider;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.query.model.NativeQuery;
import io.graphine.processor.query.model.parameter.Parameter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static io.graphine.processor.code.renderer.GeneratedKeyParameterHighLevelRenderer.DEFAULT_GENERATED_KEY_VARIABLE_NAME;

/**
 * @author Oleg Marchenko
 */
public final class RepositorySaveMethodImplementationGenerator extends RepositoryMethodImplementationGenerator {
    @Override
    protected CodeBlock renderQuery(NativeQuery query) {
        return CodeBlock.builder()
                        .addStatement("String query = $S", query.getValue())
                        .build();
    }

    @Override
    protected CodeBlock renderStatement(MethodMetadata method, NativeQuery query) {
        CodeBlock.Builder builder = CodeBlock.builder();

        List<Parameter> producedParameters = query.getProducedParameters();
        if (producedParameters.isEmpty()) {
            builder.beginControlFlow("try ($T statement = connection.prepareStatement(query))",
                                     PreparedStatement.class);
        }
        else {
            builder.beginControlFlow(
                    "try ($T statement = connection.prepareStatement(query, $T.RETURN_GENERATED_KEYS))",
                    PreparedStatement.class, Statement.class
            );
        }

        return builder.add(renderStatementParameters(method, query))
                      .endControlFlow()
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
        builder.add(renderResultSet(method, query));

        return builder.build();
    }

    @Override
    protected CodeBlock renderResultSet(MethodMetadata method, NativeQuery query) {
        CodeBlock.Builder builder = CodeBlock.builder();

        List<Parameter> producedParameters = query.getProducedParameters();
        if (!producedParameters.isEmpty()) {
            builder
                    .beginControlFlow("try ($T $L = statement.getGeneratedKeys())",
                                      ResultSet.class, DEFAULT_GENERATED_KEY_VARIABLE_NAME)
                    .add(renderResultSetParameters(method, query))
                    .endControlFlow();
        }

        return builder.build();
    }

    @Override
    protected CodeBlock renderResultSetParameters(MethodMetadata method, NativeQuery query) {
        CodeBlock.Builder builder = CodeBlock.builder();

        List<Parameter> producedParameters = query.getProducedParameters();
        if (!producedParameters.isEmpty()) {
            Parameter producedParameter = producedParameters.get(0);
            builder.add(producedParameter.accept(
                    new GeneratedKeyParameterHighLevelRenderer(new NumericParameterIndexProvider())
            ));
        }

        return builder.build();
    }
}
