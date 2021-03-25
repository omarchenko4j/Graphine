package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import io.graphine.core.util.UnnamedParameterUnwrapper;
import io.graphine.processor.code.renderer.PreparedStatementParameterRenderer;
import io.graphine.processor.code.renderer.parameter.IncrementalParameterIndexProvider;
import io.graphine.processor.code.renderer.parameter.NumericParameterIndexProvider;
import io.graphine.processor.code.renderer.parameter.ParameterIndexProvider;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.query.model.NativeQuery;
import io.graphine.processor.query.model.parameter.Parameter;

import javax.lang.model.element.ExecutableElement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryDeleteMethodImplementationGenerator extends RepositoryMethodImplementationGenerator {
    @Override
    public MethodSpec generate(MethodMetadata method, NativeQuery query) {
        ExecutableElement methodElement = method.getNativeElement();

        MethodSpec.Builder methodBuilder = MethodSpec.overriding(methodElement);
        methodBuilder
                .beginControlFlow("try ($T connection = dataSource.getConnection())", Connection.class);

        List<Parameter> deferredParameters = query.getDeferredParameters();
        if (deferredParameters.isEmpty()) {
            methodBuilder
                    .addStatement("$T query = $S", String.class, query.getValue());
        }
        else {
            List<CodeBlock> unnamedParameters =
                    deferredParameters.stream()
                                      .map(Parameter::getName)
                                      .map(parameterName ->
                                                   CodeBlock.of("$T.unwrapFor($L)",
                                                                UnnamedParameterUnwrapper.class, parameterName))
                                      .collect(Collectors.toList());
            methodBuilder
                    .addStatement("$T query = $T.format($S, $L)",
                                  String.class, String.class, query.getValue(),
                                  CodeBlock.join(unnamedParameters, ", "));
        }

        methodBuilder
                .beginControlFlow("try ($T statement = connection.prepareStatement(query))", PreparedStatement.class);

        ParameterIndexProvider parameterIndexProvider;
        if (deferredParameters.isEmpty()) {
            parameterIndexProvider = new NumericParameterIndexProvider();
        }
        else {
            methodBuilder
                    .addStatement("int index = 1");
            parameterIndexProvider = new IncrementalParameterIndexProvider("index");
        }

        List<Parameter> consumedParameters = query.getConsumedParameters();
        for (Parameter parameter : consumedParameters) {
            methodBuilder.addCode(parameter.accept(new PreparedStatementParameterRenderer(parameterIndexProvider)));
        }
        methodBuilder
                .addStatement("statement.executeUpdate()");

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
