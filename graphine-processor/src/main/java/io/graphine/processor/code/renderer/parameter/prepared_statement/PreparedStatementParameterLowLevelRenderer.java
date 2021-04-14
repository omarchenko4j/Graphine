package io.graphine.processor.code.renderer.parameter.prepared_statement;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.parameter.index_provider.ParameterIndexProvider;
import io.graphine.processor.query.model.parameter.ComplexParameter;
import io.graphine.processor.query.model.parameter.IterableParameter;
import io.graphine.processor.query.model.parameter.Parameter;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.function.Function;

import static io.graphine.processor.util.MethodUtils.getter;

/**
 * @author Oleg Marchenko
 */
public class PreparedStatementParameterLowLevelRenderer extends PreparedStatementParameterRenderer {
    public PreparedStatementParameterLowLevelRenderer(ParameterIndexProvider parameterIndexProvider) {
        super(parameterIndexProvider);
    }

    protected PreparedStatementParameterLowLevelRenderer(ParameterIndexProvider parameterIndexProvider,
                                                         Function<Parameter, CodeBlock> parameterNameMapper) {
        super(parameterIndexProvider, parameterNameMapper);
    }

    @Override
    public CodeBlock visit(Parameter parameter) {
        String parameterIndex = parameterIndexProvider.getParameterIndex();
        CodeBlock parameterName = parameterNameMapper.apply(parameter);
        TypeMirror parameterType = parameter.getType();
        switch (parameterType.getKind()) {
            case BOOLEAN:
                return CodeBlock.builder()
                                .addStatement("$L.setBoolean($L, $L)",
                                              DEFAULT_STATEMENT_VARIABLE_NAME, parameterIndex, parameterName)
                                .build();
            case BYTE:
                return CodeBlock.builder()
                                .addStatement("$L.setByte($L, $L)",
                                              DEFAULT_STATEMENT_VARIABLE_NAME, parameterIndex, parameterName)
                                .build();
            case SHORT:
                return CodeBlock.builder()
                                .addStatement("$L.setShort($L, $L)",
                                              DEFAULT_STATEMENT_VARIABLE_NAME, parameterIndex, parameterName)
                                .build();
            case INT:
                return CodeBlock.builder()
                                .addStatement("$L.setInt($L, $L)",
                                              DEFAULT_STATEMENT_VARIABLE_NAME, parameterIndex, parameterName)
                                .build();
            case LONG:
                return CodeBlock.builder()
                                .addStatement("$L.setLong($L, $L)",
                                              DEFAULT_STATEMENT_VARIABLE_NAME, parameterIndex, parameterName)
                                .build();
            case FLOAT:
                return CodeBlock.builder()
                                .addStatement("$L.setFloat($L, $L)",
                                              DEFAULT_STATEMENT_VARIABLE_NAME, parameterIndex, parameterName)
                                .build();
            case DOUBLE:
                return CodeBlock.builder()
                                .addStatement("$L.setDouble($L, $L)",
                                              DEFAULT_STATEMENT_VARIABLE_NAME, parameterIndex, parameterName)
                                .build();
            case DECLARED:
                DeclaredType declaredType = (DeclaredType) parameterType;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                switch (typeElement.getQualifiedName().toString()) {
                    case "java.lang.String":
                        return CodeBlock.builder()
                                        .addStatement("$L.setString($L, $L)",
                                                      DEFAULT_STATEMENT_VARIABLE_NAME, parameterIndex, parameterName)
                                        .build();
                    case "java.math.BigDecimal":
                        return CodeBlock.builder()
                                        .addStatement("$L.setBigDecimal($L, $L)",
                                                      DEFAULT_STATEMENT_VARIABLE_NAME, parameterIndex, parameterName)
                                        .build();
                    case "java.sql.Date":
                        return CodeBlock.builder()
                                        .addStatement("$L.setDate($L, $L)",
                                                      DEFAULT_STATEMENT_VARIABLE_NAME, parameterIndex, parameterName)
                                        .build();
                    case "java.sql.Time":
                        return CodeBlock.builder()
                                        .addStatement("$L.setTime($L, $L)",
                                                      DEFAULT_STATEMENT_VARIABLE_NAME, parameterIndex, parameterName)
                                        .build();
                    case "java.sql.Timestamp":
                        return CodeBlock.builder()
                                        .addStatement("$L.setTimestamp($L, $L)",
                                                      DEFAULT_STATEMENT_VARIABLE_NAME, parameterIndex, parameterName)
                                        .build();
                    case "java.time.LocalDate":
                        return CodeBlock.builder()
                                        .addStatement("$L.setDate($L, $L != null ? $T.valueOf($L) : null)",
                                                      DEFAULT_STATEMENT_VARIABLE_NAME,
                                                      parameterIndex, parameterName, Date.class, parameterName)
                                        .build();
                    case "java.time.LocalTime":
                        return CodeBlock.builder()
                                        .addStatement("$L.setTime($L, $L != null ? $T.valueOf($L) : null)",
                                                      DEFAULT_STATEMENT_VARIABLE_NAME,
                                                      parameterIndex, parameterName, Time.class, parameterName)
                                        .build();
                    case "java.time.LocalDateTime":
                        return CodeBlock.builder()
                                        .addStatement("$L.setTimestamp($L, $L != null ? $T.valueOf($L) : null)",
                                                      DEFAULT_STATEMENT_VARIABLE_NAME,
                                                      parameterIndex, parameterName, Timestamp.class, parameterName)
                                        .build();
                    default:
                        return CodeBlock.builder()
                                        .addStatement("$L.setObject($L, $L)",
                                                      DEFAULT_STATEMENT_VARIABLE_NAME, parameterIndex, parameterName)
                                        .build();
                }
            default:
                throw new IllegalStateException("Unsupported parameter type: " + parameterType);
        }
    }

    @Override
    public CodeBlock visit(ComplexParameter parameter) {
        CodeBlock.Builder builder = CodeBlock.builder();

        List<Parameter> childParameters = parameter.getChildParameters();
        for (Parameter childParameter : childParameters) {
            builder.add(
                    childParameter.accept(
                            new PreparedStatementParameterLowLevelRenderer(
                                    parameterIndexProvider,
                                    innerParameter -> CodeBlock.of("$L.$L()",
                                                                   parameter.getName(),
                                                                   getter(innerParameter.getType(),
                                                                          innerParameter.getName()))
                            )
                    )
            );
        }

        return builder.build();
    }

    @Override
    public CodeBlock visit(IterableParameter parameter) {
        Parameter iteratedParameter = parameter.getIteratedParameter();
        return CodeBlock.builder()
                        .beginControlFlow("for ($T $L : $L)",
                                          iteratedParameter.getType(), iteratedParameter.getName(), parameter.getName())
                        .add(iteratedParameter.accept(this))
                        .endControlFlow()
                        .build();
    }
}
