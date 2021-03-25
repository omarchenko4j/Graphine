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

import static io.graphine.processor.util.MethodUtils.setter;

/**
 * @author Oleg Marchenko
 */
public class ResultSetParameterRenderer implements ParameterVisitor<CodeBlock> {
    public static final String DEFAULT_RESULT_SET_VARIABLE_NAME = "resultSet";

    private final Function<CodeBlock, CodeBlock> resultInserter;
    private final String resultSetVariableName;
    protected final ParameterIndexProvider parameterIndexProvider;

    public ResultSetParameterRenderer(Function<CodeBlock, CodeBlock> resultInserter,
                                      ParameterIndexProvider parameterIndexProvider) {
        this(resultInserter, DEFAULT_RESULT_SET_VARIABLE_NAME, parameterIndexProvider);
    }

    protected ResultSetParameterRenderer(Function<CodeBlock, CodeBlock> resultInserter,
                                         String resultSetVariableName,
                                         ParameterIndexProvider parameterIndexProvider) {
        this.resultInserter = resultInserter;
        this.resultSetVariableName = resultSetVariableName;
        this.parameterIndexProvider = parameterIndexProvider;
    }

    @Override
    public CodeBlock visit(Parameter parameter) {
        String parameterName = parameter.getName();
        String parameterIndex = parameterIndexProvider.getParameterIndex();
        TypeMirror parameterType = parameter.getType();
        switch (parameterType.getKind()) {
            case BOOLEAN:
                return CodeBlock.builder()
                                .add(resultInserter.apply(CodeBlock.of("$L.getBoolean($L)",
                                                                       resultSetVariableName, parameterIndex)))
                                .build();
            case BYTE:
                return CodeBlock.builder()
                                .add(resultInserter.apply(CodeBlock.of("$L.getByte($L)",
                                                                       resultSetVariableName, parameterIndex)))
                                .build();
            case SHORT:
                return CodeBlock.builder()
                                .add(resultInserter.apply(CodeBlock.of("$L.getShort($L)",
                                                                       resultSetVariableName, parameterIndex)))
                                .build();
            case INT:
                return CodeBlock.builder()
                                .add(resultInserter.apply(CodeBlock.of("$L.getInt($L)",
                                                                       resultSetVariableName, parameterIndex)))
                                .build();
            case LONG:
                return CodeBlock.builder()
                                .add(resultInserter.apply(CodeBlock.of("$L.getLong($L)",
                                                                       resultSetVariableName, parameterIndex)))
                                .build();
            case FLOAT:
                return CodeBlock.builder()
                                .add(resultInserter.apply(CodeBlock.of("$L.getFloat($L)",
                                                                       resultSetVariableName, parameterIndex)))
                                .build();
            case DOUBLE:
                return CodeBlock.builder()
                                .add(resultInserter.apply(CodeBlock.of("$L.getDouble($L)",
                                                                       resultSetVariableName, parameterIndex)))
                                .build();
            case DECLARED:
                DeclaredType declaredType = (DeclaredType) parameterType;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                switch (typeElement.getQualifiedName().toString()) {
                    // TODO: use default behavior option for mapping wrapper types
                    case "java.lang.Boolean":
                        return CodeBlock.builder()
                                        .add(resultInserter.apply(CodeBlock.of("$L.getBoolean($L)",
                                                                               resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.lang.Byte":
                        return CodeBlock.builder()
                                        .add(resultInserter.apply(CodeBlock.of("$L.getByte($L)",
                                                                               resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.lang.Short":
                        return CodeBlock.builder()
                                        .add(resultInserter.apply(CodeBlock.of("$L.getShort($L)",
                                                                               resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.lang.Integer":
                        return CodeBlock.builder()
                                        .add(resultInserter.apply(CodeBlock.of("$L.getInt($L)",
                                                                               resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.lang.Long":
                        return CodeBlock.builder()
                                        .add(resultInserter.apply(CodeBlock.of("$L.getLong($L)",
                                                                               resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.lang.Float":
                        return CodeBlock.builder()
                                        .add(resultInserter.apply(CodeBlock.of("$L.getFloat($L)",
                                                                               resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.lang.Double":
                        return CodeBlock.builder()
                                        .add(resultInserter.apply(CodeBlock.of("$L.getDouble($L)",
                                                                               resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.math.BigDecimal":
                        return CodeBlock.builder()
                                        .add(resultInserter.apply(CodeBlock.of("$L.getBigDecimal($L)",
                                                                               resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.lang.String":
                        return CodeBlock.builder()
                                        .add(resultInserter.apply(CodeBlock.of("$L.getString($L)",
                                                                               resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.sql.Date":
                        return CodeBlock.builder()
                                        .add(resultInserter.apply(CodeBlock.of("$L.getDate($L)",
                                                                               resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.sql.Time":
                        return CodeBlock.builder()
                                        .add(resultInserter.apply(CodeBlock.of("$L.getTime($L)",
                                                                               resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.sql.Timestamp":
                        return CodeBlock.builder()
                                        .add(resultInserter.apply(CodeBlock.of("$L.getTimestamp($L)",
                                                                               resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.time.LocalDate":
                        return CodeBlock.builder()
                                        .addStatement("$T $L = $L.getDate($L)",
                                                      Date.class, parameterName, resultSetVariableName, parameterIndex)
                                        .beginControlFlow("if ($L != null)", parameterName)
                                        .add(resultInserter.apply(CodeBlock.of("$L.toLocalDate()", parameterName)))
                                        .endControlFlow()
                                        .build();
                    case "java.time.LocalTime":
                        return CodeBlock.builder()
                                        .addStatement("$T $L = $L.getTime($L)",
                                                      Time.class, parameterName, resultSetVariableName, parameterIndex)
                                        .beginControlFlow("if ($L != null)", parameterName)
                                        .add(resultInserter.apply(CodeBlock.of("$L.toLocalTime()", parameterName)))
                                        .endControlFlow()
                                        .build();
                    case "java.time.LocalDateTime":
                        return CodeBlock.builder()
                                        .addStatement("$T $L = $L.getTimestamp($L)",
                                                      Timestamp.class, parameterName, resultSetVariableName, parameterIndex)
                                        .beginControlFlow("if ($L != null)", parameterName)
                                        .add(resultInserter.apply(CodeBlock.of("$L.toLocalDateTime()", parameterName)))
                                        .endControlFlow()
                                        .build();
                    default:
                        return CodeBlock.builder()
                                        .add(resultInserter.apply(CodeBlock.of("($T) $L.getObject($L)",
                                                                               parameterType,
                                                                               resultSetVariableName,
                                                                               parameterIndex)))
                                        .build();
                }
            default:
                throw new IllegalStateException("Unsupported parameter type: " + parameterType);
        }
    }

    @Override
    public CodeBlock visit(ComplexParameter parameter) {
        String parameterName = parameter.getName();
        TypeMirror parameterType = parameter.getType();

        CodeBlock.Builder builder =
                CodeBlock.builder()
                         .addStatement("$T $L = new $T()",
                                       parameterType, parameterName, parameterType);

        List<Parameter> childParameters = parameter.getChildParameters();
        for (Parameter childParameter : childParameters) {
            ResultSetParameterRenderer resultSetParameterRenderer =
                    new ResultSetParameterRenderer(code -> CodeBlock.builder()
                                                                    .addStatement("$L.$L($L)",
                                                                                  parameterName,
                                                                                  setter(childParameter.getName()),
                                                                                  code)
                                                                    .build(),
                                                   resultSetVariableName,
                                                   parameterIndexProvider);
            builder.add(childParameter.accept(resultSetParameterRenderer));
        }

        builder.add(resultInserter.apply(CodeBlock.of("$L", parameterName)));

        return builder.build();
    }

    @Override
    public CodeBlock visit(IterableParameter parameter) {
        // TODO: implement in higher order renderer
        return parameter.getIteratedParameter().accept(this);
    }
}
