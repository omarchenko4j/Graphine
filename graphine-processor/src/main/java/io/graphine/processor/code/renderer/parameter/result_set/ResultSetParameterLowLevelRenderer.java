package io.graphine.processor.code.renderer.parameter.result_set;

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
import java.util.UUID;
import java.util.function.Function;

import static io.graphine.processor.util.AccessorUtils.setter;
import static javax.lang.model.element.ElementKind.ENUM;

/**
 * @author Oleg Marchenko
 */
public final class ResultSetParameterLowLevelRenderer extends ResultSetParameterRenderer {
    public ResultSetParameterLowLevelRenderer(Function<CodeBlock, CodeBlock> snippetMerger,
                                              ParameterIndexProvider parameterIndexProvider) {
        super(snippetMerger, parameterIndexProvider);
    }

    public ResultSetParameterLowLevelRenderer(Function<CodeBlock, CodeBlock> snippetMerger,
                                              String resultSetVariableName,
                                              ParameterIndexProvider parameterIndexProvider) {
        super(snippetMerger, resultSetVariableName, parameterIndexProvider);
    }

    @Override
    public CodeBlock visit(Parameter parameter) {
        String parameterName = parameter.getName();
        String parameterIndex = parameterIndexProvider.getParameterIndex();
        TypeMirror parameterType = parameter.getType();
        switch (parameterType.getKind()) {
            case BOOLEAN:
                return CodeBlock.builder()
                                .add(snippetMerger.apply(CodeBlock.of("$L.getBoolean($L)",
                                                                      resultSetVariableName, parameterIndex)))
                                .build();
            case BYTE:
                return CodeBlock.builder()
                                .add(snippetMerger.apply(CodeBlock.of("$L.getByte($L)",
                                                                      resultSetVariableName, parameterIndex)))
                                .build();
            case SHORT:
                return CodeBlock.builder()
                                .add(snippetMerger.apply(CodeBlock.of("$L.getShort($L)",
                                                                      resultSetVariableName, parameterIndex)))
                                .build();
            case INT:
                return CodeBlock.builder()
                                .add(snippetMerger.apply(CodeBlock.of("$L.getInt($L)",
                                                                      resultSetVariableName, parameterIndex)))
                                .build();
            case LONG:
                return CodeBlock.builder()
                                .add(snippetMerger.apply(CodeBlock.of("$L.getLong($L)",
                                                                      resultSetVariableName, parameterIndex)))
                                .build();
            case FLOAT:
                return CodeBlock.builder()
                                .add(snippetMerger.apply(CodeBlock.of("$L.getFloat($L)",
                                                                      resultSetVariableName, parameterIndex)))
                                .build();
            case DOUBLE:
                return CodeBlock.builder()
                                .add(snippetMerger.apply(CodeBlock.of("$L.getDouble($L)",
                                                                      resultSetVariableName, parameterIndex)))
                                .build();
            case DECLARED:
                DeclaredType declaredType = (DeclaredType) parameterType;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                switch (typeElement.getQualifiedName().toString()) {
                    // TODO: use default behavior option for mapping wrapper types
                    case "java.lang.Boolean":
                        return CodeBlock.builder()
                                        .add(snippetMerger.apply(CodeBlock.of("$L.getBoolean($L)",
                                                                              resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.lang.Byte":
                        return CodeBlock.builder()
                                        .add(snippetMerger.apply(CodeBlock.of("$L.getByte($L)",
                                                                              resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.lang.Short":
                        return CodeBlock.builder()
                                        .add(snippetMerger.apply(CodeBlock.of("$L.getShort($L)",
                                                                              resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.lang.Integer":
                        return CodeBlock.builder()
                                        .add(snippetMerger.apply(CodeBlock.of("$L.getInt($L)",
                                                                              resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.lang.Long":
                        return CodeBlock.builder()
                                        .add(snippetMerger.apply(CodeBlock.of("$L.getLong($L)",
                                                                              resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.lang.Float":
                        return CodeBlock.builder()
                                        .add(snippetMerger.apply(CodeBlock.of("$L.getFloat($L)",
                                                                              resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.lang.Double":
                        return CodeBlock.builder()
                                        .add(snippetMerger.apply(CodeBlock.of("$L.getDouble($L)",
                                                                              resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.math.BigDecimal":
                        return CodeBlock.builder()
                                        .add(snippetMerger.apply(CodeBlock.of("$L.getBigDecimal($L)",
                                                                              resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.lang.String":
                        return CodeBlock.builder()
                                        .add(snippetMerger.apply(CodeBlock.of("$L.getString($L)",
                                                                              resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.sql.Date":
                        return CodeBlock.builder()
                                        .add(snippetMerger.apply(CodeBlock.of("$L.getDate($L)",
                                                                              resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.sql.Time":
                        return CodeBlock.builder()
                                        .add(snippetMerger.apply(CodeBlock.of("$L.getTime($L)",
                                                                              resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.sql.Timestamp":
                        return CodeBlock.builder()
                                        .add(snippetMerger.apply(CodeBlock.of("$L.getTimestamp($L)",
                                                                              resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.time.Instant":
                        return CodeBlock.builder()
                                        .addStatement("$T $L = $L.getTimestamp($L)",
                                                      Timestamp.class, parameterName, resultSetVariableName, parameterIndex)
                                        .beginControlFlow("if ($L != null)", parameterName)
                                        .add(snippetMerger.apply(CodeBlock.of("$L.toInstant()", parameterName)))
                                        .endControlFlow()
                                        .build();
                    case "java.time.LocalDate":
                        return CodeBlock.builder()
                                        .addStatement("$T $L = $L.getDate($L)",
                                                      Date.class, parameterName, resultSetVariableName, parameterIndex)
                                        .beginControlFlow("if ($L != null)", parameterName)
                                        .add(snippetMerger.apply(CodeBlock.of("$L.toLocalDate()", parameterName)))
                                        .endControlFlow()
                                        .build();
                    case "java.time.LocalTime":
                        return CodeBlock.builder()
                                        .addStatement("$T $L = $L.getTime($L)",
                                                      Time.class, parameterName, resultSetVariableName, parameterIndex)
                                        .beginControlFlow("if ($L != null)", parameterName)
                                        .add(snippetMerger.apply(CodeBlock.of("$L.toLocalTime()", parameterName)))
                                        .endControlFlow()
                                        .build();
                    case "java.time.LocalDateTime":
                        return CodeBlock.builder()
                                        .addStatement("$T $L = $L.getTimestamp($L)",
                                                      Timestamp.class, parameterName, resultSetVariableName, parameterIndex)
                                        .beginControlFlow("if ($L != null)", parameterName)
                                        .add(snippetMerger.apply(CodeBlock.of("$L.toLocalDateTime()", parameterName)))
                                        .endControlFlow()
                                        .build();
                    case "java.util.UUID":
                        return CodeBlock.builder()
                                        .addStatement("$T $L = $L.getString($L)",
                                                      String.class, parameterName, resultSetVariableName, parameterIndex)
                                        .beginControlFlow("if ($L != null)", parameterName)
                                        .add(snippetMerger.apply(CodeBlock.of("$T.fromString($L)",
                                                                              UUID.class, parameterName)))
                                        .endControlFlow()
                                        .build();
                    default:
                        if (typeElement.getKind() == ENUM) {
                            return CodeBlock.builder()
                                            .addStatement("$T $L = $L.getString($L)",
                                                          String.class, parameterName, resultSetVariableName, parameterIndex)
                                            .beginControlFlow("if ($L != null)", parameterName)
                                            .add(snippetMerger.apply(CodeBlock.of("$T.valueOf($L)",
                                                                                  parameterType, parameterName)))
                                            .endControlFlow()
                                            .build();
                        }
                        return CodeBlock.builder()
                                        .add(snippetMerger.apply(CodeBlock.of("($T) $L.getObject($L)",
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
        CodeBlock.Builder builder = CodeBlock.builder();

        String parameterName = parameter.getName();

        List<Parameter> childParameters = parameter.getChildParameters();
        for (Parameter childParameter : childParameters) {
            builder.add(childParameter.accept(
                    new ResultSetParameterLowLevelRenderer(
                            snippet -> CodeBlock.builder()
                                                .addStatement("$L.$L($L)",
                                                              parameterName, setter(childParameter), snippet)
                                                .build(),
                            resultSetVariableName,
                            parameterIndexProvider)
            ));
        }

        return builder.build();
    }

    @Override
    public CodeBlock visit(IterableParameter parameter) {
        throw new UnsupportedOperationException();
    }
}
