package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import io.graphine.core.util.UnnamedParameterUnwrapper;
import io.graphine.processor.code.renderer.PreparedStatementParameterRenderer;
import io.graphine.processor.code.renderer.parameter.IncrementalParameterIndexProvider;
import io.graphine.processor.code.renderer.parameter.NumericParameterIndexProvider;
import io.graphine.processor.code.renderer.parameter.ParameterIndexProvider;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.query.model.NativeQuery;
import io.graphine.processor.query.model.parameter.Parameter;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryDeleteMethodImplementationGenerator extends RepositoryMethodImplementationGenerator {
    @Override
    public MethodSpec generate(MethodMetadata method, NativeQuery query) {
        ExecutableElement methodElement = method.getNativeElement();
        List<? extends VariableElement> parameters = methodElement.getParameters();

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

        QueryableMethodName queryableName = method.getQueryableName();

        ConditionFragment condition = queryableName.getCondition();
        if (isNull(condition)) {
            VariableElement parameterElement = parameters.get(0);
            String parameterName = parameterElement.getSimpleName().toString();

            Parameter parameter = consumedParameters.get(0);

            TypeMirror parameterType = parameterElement.asType();
            switch (parameterType.getKind()) {
                case ARRAY:
                    ArrayType arrayType = (ArrayType) parameterType;
                    TypeMirror componentType = arrayType.getComponentType();
                    methodBuilder
                            .addStatement("int index = 1")
                            .beginControlFlow("for ($T $L : $L)",
                                              componentType, parameter.getName(), parameterName)
                            .addCode(parameter.accept(
                                    new PreparedStatementParameterRenderer(
                                            new IncrementalParameterIndexProvider("index")
                                    )
                            ))
                            .endControlFlow()
                            .addStatement("statement.executeUpdate()");
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
                                    .addStatement("int index = 1")
                                    .beginControlFlow("for ($T $L : $L)",
                                                      genericType, parameter.getName(), parameterName)
                                    .addCode(parameter.accept(
                                            new PreparedStatementParameterRenderer(
                                                    new IncrementalParameterIndexProvider("index")
                                            )
                                    ))
                                    .endControlFlow()
                                    .addStatement("statement.executeUpdate()");
                            break;
                        default:
                            methodBuilder
                                    .addCode(parameter.accept(
                                            new PreparedStatementParameterRenderer(new NumericParameterIndexProvider()))
                                    )
                                    .addStatement("statement.executeUpdate()");
                            break;
                    }
                    break;
            }
        }
        else {
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

            methodBuilder
                    .addStatement("statement.executeUpdate()");
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
