package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.parameter.index_provider.NumericParameterIndexProvider;
import io.graphine.processor.code.renderer.parameter.prepared_statement.PreparedStatementParameterHighLevelRenderer;
import io.graphine.processor.code.renderer.parameter.result_set.GeneratedKeyParameterHighLevelRenderer;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.query.model.NativeQuery;
import io.graphine.processor.query.model.parameter.Parameter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static io.graphine.processor.code.renderer.parameter.result_set.GeneratedKeyParameterHighLevelRenderer.GENERATED_KEY_VARIABLE_NAME;

/**
 * @author Oleg Marchenko
 */
public final class RepositorySaveMethodImplementationGenerator extends RepositoryMethodImplementationGenerator {
    @Override
    protected CodeBlock renderQuery(MethodMetadata method, NativeQuery query) {
        return CodeBlock.builder()
                        .addStatement("String $L = $S", QUERY_VARIABLE_NAME, query.getValue())
                        .build();
    }

    @Override
    protected CodeBlock renderStatement(MethodMetadata method, NativeQuery query) {
        CodeBlock.Builder builder = CodeBlock.builder();

        List<Parameter> producedParameters = query.getProducedParameters();
        if (producedParameters.isEmpty()) {
            builder.beginControlFlow("try ($T $L = $L.prepareStatement($L))",
                                     PreparedStatement.class,
                                     STATEMENT_VARIABLE_NAME,
                                     CONNECTION_VARIABLE_NAME,
                                     QUERY_VARIABLE_NAME);
        }
        else {
            builder.beginControlFlow("try ($T $L = $L.prepareStatement($L, $T.RETURN_GENERATED_KEYS))",
                                     PreparedStatement.class,
                                     STATEMENT_VARIABLE_NAME,
                                     CONNECTION_VARIABLE_NAME,
                                     QUERY_VARIABLE_NAME,
                                     Statement.class);
        }

        return builder.add(renderStatementParameters(method, query))
                      .endControlFlow()
                      .build();
    }

    @Override
    protected CodeBlock renderStatementParameters(MethodMetadata method, NativeQuery query) {
        Parameter consumedParameter = query.getConsumedParameters().get(0);
        return CodeBlock.builder()
                        .add(consumedParameter.accept(
                                new PreparedStatementParameterHighLevelRenderer(new NumericParameterIndexProvider())
                        ))
                        .add(renderResultSet(method, query))
                        .build();
    }

    @Override
    protected CodeBlock renderResultSet(MethodMetadata method, NativeQuery query) {
        CodeBlock.Builder builder = CodeBlock.builder();

        List<Parameter> producedParameters = query.getProducedParameters();
        if (!producedParameters.isEmpty()) {
            builder
                    .beginControlFlow("try ($T $L = $L.getGeneratedKeys())",
                                      ResultSet.class,
                                      GENERATED_KEY_VARIABLE_NAME,
                                      STATEMENT_VARIABLE_NAME)
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
