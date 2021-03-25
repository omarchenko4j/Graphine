package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.MethodSpec;
import io.graphine.processor.code.renderer.GeneratedKeyParameterRenderer;
import io.graphine.processor.code.renderer.PreparedStatementAddBatchMethodRenderer;
import io.graphine.processor.code.renderer.PreparedStatementExecuteMethodRenderer;
import io.graphine.processor.code.renderer.parameter.NumericParameterIndexProvider;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.query.model.NativeQuery;
import io.graphine.processor.query.model.parameter.Parameter;

import javax.lang.model.element.ExecutableElement;
import java.sql.*;
import java.util.List;

/**
 * @author Oleg Marchenko
 */
public final class RepositorySaveMethodImplementationGenerator extends RepositoryMethodImplementationGenerator {
    @Override
    public MethodSpec generate(MethodMetadata method, NativeQuery query) {
        ExecutableElement methodElement = method.getNativeElement();

        MethodSpec.Builder methodBuilder = MethodSpec.overriding(methodElement);
        methodBuilder
                .beginControlFlow("try ($T connection = dataSource.getConnection())", Connection.class);
        methodBuilder
                .addStatement("String query = $S", query.getValue());

        List<Parameter> producedParameters = query.getProducedParameters();
        if (producedParameters.isEmpty()) {
            methodBuilder
                    .beginControlFlow("try ($T statement = connection.prepareStatement(query))",
                                      PreparedStatement.class);
        }
        else {
            methodBuilder
                    .beginControlFlow(
                            "try ($T statement = connection.prepareStatement(query, $T.RETURN_GENERATED_KEYS))",
                            PreparedStatement.class, Statement.class
                    );
        }

        Parameter consumedParameter = query.getConsumedParameters().get(0);
        methodBuilder.addCode(consumedParameter.accept(
                new PreparedStatementAddBatchMethodRenderer(new NumericParameterIndexProvider()))
        );
        methodBuilder.addCode(consumedParameter.accept(new PreparedStatementExecuteMethodRenderer()));

        if (!producedParameters.isEmpty()) {
            Parameter producedParameter = producedParameters.get(0);
            methodBuilder
                    .beginControlFlow("try ($T generatedKeys = statement.getGeneratedKeys())", ResultSet.class)
                    .addCode(producedParameter.accept(
                            new GeneratedKeyParameterRenderer(new NumericParameterIndexProvider())
                    ))
                    .endControlFlow();
        }

        methodBuilder
                .endControlFlow();
        methodBuilder
                .endControlFlow();
        methodBuilder
                .beginControlFlow("catch ($T e)", SQLException.class)
                .addStatement("throw new $T(e)", RuntimeException.class)
                .endControlFlow();
        return methodBuilder.build();
    }
}
