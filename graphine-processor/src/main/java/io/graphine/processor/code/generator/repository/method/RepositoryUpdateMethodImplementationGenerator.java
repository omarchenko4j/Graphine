package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.MethodSpec;
import io.graphine.processor.code.renderer.PreparedStatementParameterRenderer;
import io.graphine.processor.code.renderer.parameter.NumericParameterIndexProvider;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
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

/**
 * @author Oleg Marchenko
 */
public final class RepositoryUpdateMethodImplementationGenerator extends RepositoryMethodImplementationGenerator {
    @Override
    public MethodSpec generate(MethodMetadata method, NativeQuery query) {
        ExecutableElement methodElement = method.getNativeElement();

        MethodSpec.Builder methodBuilder = MethodSpec.overriding(methodElement);
        methodBuilder
                .beginControlFlow("try ($T connection = dataSource.getConnection())", Connection.class);
        methodBuilder
                .addStatement("String query = $S", query.getValue());
        methodBuilder
                .beginControlFlow("try ($T statement = connection.prepareStatement(query))", PreparedStatement.class);

        VariableElement methodParameterElement = methodElement.getParameters().get(0);
        String methodParameterName = methodParameterElement.getSimpleName().toString();
        TypeMirror methodParameterType = methodParameterElement.asType();

        Parameter consumedParameter = query.getConsumedParameters().get(0);

        String parameterName = consumedParameter.getName();
        switch (methodParameterType.getKind()) {
            case ARRAY:
                ArrayType arrayType = (ArrayType) methodParameterType;
                TypeMirror componentType = arrayType.getComponentType();
                methodBuilder
                        .beginControlFlow("for ($T $L : $L)",
                                          componentType, parameterName, methodParameterName);
                break;
            case DECLARED:
                DeclaredType declaredType = (DeclaredType) methodParameterType;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                switch (typeElement.getQualifiedName().toString()) {
                    case "java.lang.Iterable":
                    case "java.util.Collection":
                    case "java.util.List":
                    case "java.util.Set":
                        TypeMirror genericType = declaredType.getTypeArguments().get(0);
                        methodBuilder
                                .beginControlFlow("for ($T $L : $L)",
                                                  genericType, parameterName, methodParameterName);
                        break;
                }
                break;
        }

        methodBuilder.addCode(consumedParameter.accept(
                new PreparedStatementParameterRenderer(new NumericParameterIndexProvider()))
        );

        switch (methodParameterType.getKind()) {
            case ARRAY:
                methodBuilder
                        .addStatement("statement.addBatch()")
                        .endControlFlow()
                        .addStatement("statement.executeBatch()");
                break;
            case DECLARED:
                DeclaredType declaredType = (DeclaredType) methodParameterType;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                switch (typeElement.getQualifiedName().toString()) {
                    case "java.lang.Iterable":
                    case "java.util.Collection":
                    case "java.util.List":
                    case "java.util.Set":
                        methodBuilder
                                .addStatement("statement.addBatch()")
                                .endControlFlow()
                                .addStatement("statement.executeBatch()");
                        break;
                    default:
                        methodBuilder
                                .addStatement("statement.executeUpdate()");
                        break;
                }
                break;
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
