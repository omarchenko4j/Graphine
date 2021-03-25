package io.graphine.processor.code.renderer;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.parameter.ParameterIndexProvider;
import io.graphine.processor.query.model.parameter.ComplexParameter;
import io.graphine.processor.query.model.parameter.IterableParameter;
import io.graphine.processor.query.model.parameter.Parameter;
import io.graphine.processor.query.model.parameter.ParameterVisitor;

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
public class PreparedStatementParameterRenderer implements ParameterVisitor<CodeBlock> {
    private final ParameterIndexProvider parameterIndexProvider;
    private final Function<Parameter, CodeBlock> parameterNameMapper;

    public PreparedStatementParameterRenderer(ParameterIndexProvider parameterIndexProvider) {
        this(parameterIndexProvider, parameter -> CodeBlock.of(parameter.getName()));
    }

    public PreparedStatementParameterRenderer(ParameterIndexProvider parameterIndexProvider,
                                              Function<Parameter, CodeBlock> parameterNameMapper) {
        this.parameterIndexProvider = parameterIndexProvider;
        this.parameterNameMapper = parameterNameMapper;
    }

    @Override
    public CodeBlock visit(Parameter parameter) {
        String parameterIndex = parameterIndexProvider.getParameterIndex();
        CodeBlock parameterName = parameterNameMapper.apply(parameter);
        TypeMirror parameterType = parameter.getType();
        switch (parameterType.getKind()) {
            case BOOLEAN:
                return CodeBlock.builder()
                                .addStatement("statement.setBoolean($L, $L)", parameterIndex, parameterName)
                                .build();
            case BYTE:
                return CodeBlock.builder()
                                .addStatement("statement.setByte($L, $L)", parameterIndex, parameterName)
                                .build();
            case SHORT:
                return CodeBlock.builder()
                                .addStatement("statement.setShort($L, $L)", parameterIndex, parameterName)
                                .build();
            case INT:
                return CodeBlock.builder()
                                .addStatement("statement.setInt($L, $L)", parameterIndex, parameterName)
                                .build();
            case LONG:
                return CodeBlock.builder()
                                .addStatement("statement.setLong($L, $L)", parameterIndex, parameterName)
                                .build();
            case FLOAT:
                return CodeBlock.builder()
                                .addStatement("statement.setFloat($L, $L)", parameterIndex, parameterName)
                                .build();
            case DOUBLE:
                return CodeBlock.builder()
                                .addStatement("statement.setDouble($L, $L)", parameterIndex, parameterName)
                                .build();
            case DECLARED:
                DeclaredType declaredType = (DeclaredType) parameterType;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                switch (typeElement.getQualifiedName().toString()) {
                    case "java.lang.String":
                        return CodeBlock.builder()
                                        .addStatement("statement.setString($L, $L)", parameterIndex, parameterName)
                                        .build();
                    case "java.math.BigDecimal":
                        return CodeBlock.builder()
                                        .addStatement("statement.setBigDecimal($L, $L)", parameterIndex, parameterName)
                                        .build();
                    case "java.sql.Date":
                        return CodeBlock.builder()
                                        .addStatement("statement.setDate($L, $L)", parameterIndex, parameterName)
                                        .build();
                    case "java.sql.Time":
                        return CodeBlock.builder()
                                        .addStatement("statement.setTime($L, $L)", parameterIndex, parameterName)
                                        .build();
                    case "java.sql.Timestamp":
                        return CodeBlock.builder()
                                        .addStatement("statement.setTimestamp($L, $L)", parameterIndex, parameterName)
                                        .build();
                    case "java.time.LocalDate":
                        return CodeBlock.builder()
                                        .addStatement("statement.setDate($L, $L != null ? $T.valueOf($L) : null)",
                                                      parameterIndex, parameterName, Date.class, parameterName)
                                        .build();
                    case "java.time.LocalTime":
                        return CodeBlock.builder()
                                        .addStatement("statement.setTime($L, $L != null ? $T.valueOf($L) : null)",
                                                      parameterIndex, parameterName, Time.class, parameterName)
                                        .build();
                    case "java.time.LocalDateTime":
                        return CodeBlock.builder()
                                        .addStatement("statement.setTimestamp($L, $L != null ? $T.valueOf($L) : null)",
                                                      parameterIndex, parameterName, Timestamp.class, parameterName)
                                        .build();
                    default:
                        return CodeBlock.builder()
                                        .addStatement("statement.setObject($L, $L)", parameterIndex, parameterName)
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
                            new PreparedStatementParameterRenderer(
                                    parameterIndexProvider,
                                    innerParameter -> CodeBlock.of("$L.$L()",
                                                                   parameter.getName(),
                                                                   getter(innerParameter.getType(), innerParameter.getName()))
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
