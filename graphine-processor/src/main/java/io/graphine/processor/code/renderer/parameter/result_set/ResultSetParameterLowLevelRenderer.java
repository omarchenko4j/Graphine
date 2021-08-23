package io.graphine.processor.code.renderer.parameter.result_set;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.parameter.index_provider.ParameterIndexProvider;
import io.graphine.processor.query.model.parameter.ComplexParameter;
import io.graphine.processor.query.model.parameter.IterableParameter;
import io.graphine.processor.query.model.parameter.Parameter;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static io.graphine.processor.util.AccessorUtils.setter;
import static javax.lang.model.element.ElementKind.ENUM;
import static javax.lang.model.type.TypeKind.BYTE;

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
            case ARRAY:
                ArrayType arrayType = (ArrayType) parameterType;
                TypeMirror componentType = arrayType.getComponentType();
                if (componentType.getKind() == BYTE) {
                    return CodeBlock.builder()
                                    .add(snippetMerger.apply(CodeBlock.of("$L.getBytes($L)",
                                                                          resultSetVariableName, parameterIndex)))
                                    .build();
                }
                throw new IllegalStateException("Unsupported array type: " + parameterType);
            case DECLARED:
                DeclaredType declaredType = (DeclaredType) parameterType;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                switch (typeElement.getQualifiedName().toString()) {
                    case "java.lang.String":
                        return CodeBlock.builder()
                                        .add(snippetMerger.apply(CodeBlock.of("$L.getString($L)",
                                                                              resultSetVariableName, parameterIndex)))
                                        .build();
                    case "java.math.BigDecimal":
                        return CodeBlock.builder()
                                        .add(snippetMerger.apply(CodeBlock.of("$L.getBigDecimal($L)",
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
                    case "java.time.Instant": {
                        return CodeBlock.builder()
                                        .add(snippetMerger.apply(CodeBlock.of("$L.wasNull() ? null : $L.getTimestamp($L).toInstant()",
                                                                              resultSetVariableName,
                                                                              resultSetVariableName,
                                                                              parameterIndex)))
                                        .build();
                    }
                    case "java.time.LocalDate": {
                        return CodeBlock.builder()
                                        .add(snippetMerger.apply(CodeBlock.of("$L.wasNull() ? null : $L.getDate($L).toLocalDate()",
                                                                              resultSetVariableName,
                                                                              resultSetVariableName,
                                                                              parameterIndex)))
                                        .build();
                    }
                    case "java.time.LocalTime": {
                        return CodeBlock.builder()
                                        .add(snippetMerger.apply(CodeBlock.of("$L.wasNull() ? null : $L.getTime($L).toLocalTime()",
                                                                              resultSetVariableName,
                                                                              resultSetVariableName,
                                                                              parameterIndex)))
                                        .build();
                    }
                    case "java.time.LocalDateTime": {
                        return CodeBlock.builder()
                                        .add(snippetMerger.apply(CodeBlock.of("$L.wasNull() ? null : $L.getTimestamp($L).toLocalDateTime()",
                                                                              resultSetVariableName,
                                                                              resultSetVariableName,
                                                                              parameterIndex)))
                                        .build();
                    }
                    case "java.util.UUID": {
                        return CodeBlock.builder()
                                        .add(snippetMerger.apply(CodeBlock.of("$L.wasNull() ? null : $T.fromString($L.getString($L))",
                                                                              resultSetVariableName,
                                                                              UUID.class,
                                                                              resultSetVariableName,
                                                                              parameterIndex)))
                                        .build();
                    }
                    default:
                        if (typeElement.getKind() == ENUM) {
                            return CodeBlock.builder()
                                            .add(snippetMerger.apply(CodeBlock.of("$L.wasNull() ? null : $T.valueOf($L.getString($L))",
                                                                                  resultSetVariableName,
                                                                                  parameterType,
                                                                                  resultSetVariableName,
                                                                                  parameterIndex)))
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
