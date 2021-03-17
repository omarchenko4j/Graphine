package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import io.graphine.core.util.UnnamedParameterUnwrapper;
import io.graphine.processor.code.renderer.PreparedStatementParameterRenderer;
import io.graphine.processor.code.renderer.ResultSetParameterRenderer;
import io.graphine.processor.code.renderer.parameter.IncrementalParameterIndexProvider;
import io.graphine.processor.code.renderer.parameter.NumericParameterIndexProvider;
import io.graphine.processor.code.renderer.parameter.ParameterIndexProvider;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.query.model.NativeQuery;
import io.graphine.processor.query.model.parameter.Parameter;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryCountMethodImplementationGenerator extends RepositoryMethodImplementationGenerator {
    public RepositoryCountMethodImplementationGenerator(EntityMetadata entity) {
        super(entity);
    }

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

        List<Parameter> consumedParameters = query.getConsumedParameters();
        if (!consumedParameters.isEmpty()) {
            ParameterIndexProvider parameterIndexProvider;
            if (deferredParameters.isEmpty()) {
                parameterIndexProvider = new NumericParameterIndexProvider();
            }
            else {
                methodBuilder
                        .addStatement("int index = 1");
                parameterIndexProvider = new IncrementalParameterIndexProvider("index");
            }

            for (Parameter parameter : consumedParameters) {
                Parameter targetParameter = parameter;

                String parameterName = targetParameter.getName();
                TypeMirror parameterType = targetParameter.getType();
                switch (parameterType.getKind()) {
                    case ARRAY:
                        ArrayType arrayType = (ArrayType) parameterType;
                        TypeMirror componentType = arrayType.getComponentType();
                        methodBuilder
                                .beginControlFlow("for ($T $L : $L)",
                                                  componentType, "element", parameterName);
                        targetParameter = new Parameter("element", componentType);
                        break;
                    case DECLARED:
                        DeclaredType declaredType = (DeclaredType) parameterType;
                        TypeElement typeElement = (TypeElement) declaredType.asElement();
                        switch (typeElement.getQualifiedName().toString()) {
                            case "java.lang.Iterable":
                            case "java.util.Collection":
                            case "java.util.List":
                            case "java.util.Set":
                                TypeMirror genericType = declaredType.getTypeArguments().get(0);
                                methodBuilder
                                        .beginControlFlow("for ($T $L : $L)",
                                                          genericType, "element", parameterName);
                                targetParameter = new Parameter("element", genericType);
                                break;
                        }
                        break;
                }

                methodBuilder.addCode(
                        targetParameter.accept(
                                new PreparedStatementParameterRenderer(parameterIndexProvider)
                        )
                );

                switch (parameterType.getKind()) {
                    case ARRAY:
                        methodBuilder
                                .endControlFlow();
                        break;
                    case DECLARED:
                        DeclaredType declaredType = (DeclaredType) parameterType;
                        TypeElement typeElement = (TypeElement) declaredType.asElement();
                        switch (typeElement.getQualifiedName().toString()) {
                            case "java.lang.Iterable":
                            case "java.util.Collection":
                            case "java.util.List":
                            case "java.util.Set":
                                methodBuilder
                                        .endControlFlow();
                                break;
                        }
                        break;
                }
            }
        }

        methodBuilder
                .beginControlFlow("try ($T resultSet = statement.executeQuery())", ResultSet.class);
        methodBuilder
                .beginControlFlow("if (resultSet.next())");

        Parameter parameter = query.getProducedParameters().get(0);
        methodBuilder.addCode(parameter.accept(
                new ResultSetParameterRenderer(code -> CodeBlock.builder()
                                                                .addStatement("return $L", code)
                                                                .build(),
                                               () -> "1")
        ));

        methodBuilder
                .endControlFlow();

        TypeMirror returnType = methodElement.getReturnType();
        switch (returnType.getKind()) {
            case INT:
            case LONG:
                methodBuilder
                        .addStatement("return 0");
                break;
            case DECLARED:
                DeclaredType declaredType = (DeclaredType) returnType;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                switch (typeElement.getQualifiedName().toString()) {
                    case "java.lang.Integer":
                        methodBuilder
                                .addStatement("return 0");
                        break;
                    case "java.lang.Long":
                        methodBuilder
                                .addStatement("return 0L");
                        break;
                }
                break;
        }

        methodBuilder
                .endControlFlow();
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
