package io.graphine.processor.code.renderer;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import static io.graphine.processor.code.generator.repository.method.RepositoryMethodImplementationGenerator.STATEMENT_VARIABLE_NAME;
import static javax.lang.model.element.ElementKind.ENUM;
import static javax.lang.model.type.TypeKind.BYTE;

/**
 * @author Oleg Marchenko
 */
public final class PreparedStatementMethodMappingRenderer {
    public CodeBlock render(TypeMirror parameterType, String parameterIndex, String parameterValue) {
        return render(parameterType, parameterIndex, CodeBlock.of(parameterValue));
    }

    public CodeBlock render(TypeMirror parameterType, String parameterIndex, CodeBlock parameterValueSnippet) {
        switch (parameterType.getKind()) {
            case BOOLEAN:
                return CodeBlock.builder()
                                .addStatement("$L.setBoolean($L, $L)",
                                              STATEMENT_VARIABLE_NAME, parameterIndex, parameterValueSnippet)
                                .build();
            case BYTE:
                return CodeBlock.builder()
                                .addStatement("$L.setByte($L, $L)",
                                              STATEMENT_VARIABLE_NAME, parameterIndex, parameterValueSnippet)
                                .build();
            case SHORT:
                return CodeBlock.builder()
                                .addStatement("$L.setShort($L, $L)",
                                              STATEMENT_VARIABLE_NAME, parameterIndex, parameterValueSnippet)
                                .build();
            case INT:
                return CodeBlock.builder()
                                .addStatement("$L.setInt($L, $L)",
                                              STATEMENT_VARIABLE_NAME, parameterIndex, parameterValueSnippet)
                                .build();
            case LONG:
                return CodeBlock.builder()
                                .addStatement("$L.setLong($L, $L)",
                                              STATEMENT_VARIABLE_NAME, parameterIndex, parameterValueSnippet)
                                .build();
            case FLOAT:
                return CodeBlock.builder()
                                .addStatement("$L.setFloat($L, $L)",
                                              STATEMENT_VARIABLE_NAME, parameterIndex, parameterValueSnippet)
                                .build();
            case DOUBLE:
                return CodeBlock.builder()
                                .addStatement("$L.setDouble($L, $L)",
                                              STATEMENT_VARIABLE_NAME, parameterIndex, parameterValueSnippet)
                                .build();
            case ARRAY:
                ArrayType arrayType = (ArrayType) parameterType;
                TypeMirror componentType = arrayType.getComponentType();
                if (componentType.getKind() == BYTE) {
                    return CodeBlock.builder()
                                    .addStatement("$L.setBytes($L, $L)",
                                                  STATEMENT_VARIABLE_NAME, parameterIndex, parameterValueSnippet)
                                    .build();
                }
                else {
                    return CodeBlock.builder()
                                    .beginControlFlow("for ($T $L : $L)",
                                                      componentType, "element", parameterValueSnippet)
                                    .add(render(componentType, parameterIndex, "element"))
                                    .endControlFlow()
                                    .build();
                }
            case DECLARED:
                DeclaredType declaredType = (DeclaredType) parameterType;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                switch (typeElement.getQualifiedName().toString()) {
                    case "java.lang.String":
                        return CodeBlock.builder()
                                        .addStatement("$L.setString($L, $L)",
                                                      STATEMENT_VARIABLE_NAME, parameterIndex, parameterValueSnippet)
                                        .build();
                    case "java.math.BigDecimal":
                        return CodeBlock.builder()
                                        .addStatement("$L.setBigDecimal($L, $L)",
                                                      STATEMENT_VARIABLE_NAME, parameterIndex, parameterValueSnippet)
                                        .build();
                    case "java.sql.Date":
                        return CodeBlock.builder()
                                        .addStatement("$L.setDate($L, $L)",
                                                      STATEMENT_VARIABLE_NAME, parameterIndex, parameterValueSnippet)
                                        .build();
                    case "java.sql.Time":
                        return CodeBlock.builder()
                                        .addStatement("$L.setTime($L, $L)",
                                                      STATEMENT_VARIABLE_NAME, parameterIndex, parameterValueSnippet)
                                        .build();
                    case "java.sql.Timestamp":
                        return CodeBlock.builder()
                                        .addStatement("$L.setTimestamp($L, $L)",
                                                      STATEMENT_VARIABLE_NAME, parameterIndex, parameterValueSnippet)
                                        .build();
                    case "java.time.Instant":
                        return CodeBlock.builder()
                                        .addStatement("$L.setTimestamp($L, $L != null ? $T.from($L) : null)",
                                                      STATEMENT_VARIABLE_NAME,
                                                      parameterIndex, parameterValueSnippet, Timestamp.class, parameterValueSnippet)
                                        .build();
                    case "java.time.LocalDate":
                        return CodeBlock.builder()
                                        .addStatement("$L.setDate($L, $L != null ? $T.valueOf($L) : null)",
                                                      STATEMENT_VARIABLE_NAME,
                                                      parameterIndex, parameterValueSnippet, Date.class, parameterValueSnippet)
                                        .build();
                    case "java.time.LocalTime":
                        return CodeBlock.builder()
                                        .addStatement("$L.setTime($L, $L != null ? $T.valueOf($L) : null)",
                                                      STATEMENT_VARIABLE_NAME,
                                                      parameterIndex, parameterValueSnippet, Time.class, parameterValueSnippet)
                                        .build();
                    case "java.time.LocalDateTime":
                        return CodeBlock.builder()
                                        .addStatement("$L.setTimestamp($L, $L != null ? $T.valueOf($L) : null)",
                                                      STATEMENT_VARIABLE_NAME,
                                                      parameterIndex, parameterValueSnippet, Timestamp.class, parameterValueSnippet)
                                        .build();
                    case "java.util.UUID":
                        return CodeBlock.builder()
                                        .addStatement("$L.setString($L, $L != null ? $L.toString() : null)",
                                                      STATEMENT_VARIABLE_NAME,
                                                      parameterIndex, parameterValueSnippet, parameterValueSnippet)
                                        .build();
                    case "java.lang.Iterable":
                    case "java.util.Collection":
                    case "java.util.List":
                    case "java.util.Set":
                        TypeMirror genericType = declaredType.getTypeArguments().get(0);
                        return CodeBlock.builder()
                                        .beginControlFlow("for ($T $L : $L)",
                                                          genericType, "element", parameterValueSnippet)
                                        .add(render(genericType, parameterIndex, "element"))
                                        .endControlFlow()
                                        .build();
                    default:
                        if (typeElement.getKind() == ENUM) {
                            return CodeBlock.builder()
                                            .addStatement("$L.setString($L, $L != null ? $L.name() : null)",
                                                          STATEMENT_VARIABLE_NAME,
                                                          parameterIndex, parameterValueSnippet, parameterValueSnippet)
                                            .build();
                        }
                        return CodeBlock.builder()
                                        .addStatement("$L.setObject($L, $L)",
                                                      STATEMENT_VARIABLE_NAME, parameterIndex, parameterValueSnippet)
                                        .build();
                }
            default:
                throw new IllegalStateException("Unsupported parameter type: " + parameterType);
        }
    }
}
