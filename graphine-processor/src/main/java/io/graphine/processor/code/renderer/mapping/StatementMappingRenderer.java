package io.graphine.processor.code.renderer.mapping;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import static io.graphine.processor.code.generator.repository.method.RepositoryMethodImplementationGenerator.STATEMENT_VARIABLE_NAME;
import static javax.lang.model.element.ElementKind.ENUM;
import static javax.lang.model.type.TypeKind.BYTE;

/**
 * @author Oleg Marchenko
 */
public final class StatementMappingRenderer {
    public CodeBlock render(TypeMirror type, String index, String value) {
        return render(type, index, CodeBlock.of(value));
    }

    public CodeBlock render(TypeMirror type, String index, CodeBlock valueSnippet) {
        switch (type.getKind()) {
            case BOOLEAN:
                return CodeBlock.builder()
                                .addStatement("$L.setBoolean($L, $L)",
                                              STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                .build();
            case BYTE:
                return CodeBlock.builder()
                                .addStatement("$L.setByte($L, $L)",
                                              STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                .build();
            case SHORT:
                return CodeBlock.builder()
                                .addStatement("$L.setShort($L, $L)",
                                              STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                .build();
            case INT:
                return CodeBlock.builder()
                                .addStatement("$L.setInt($L, $L)",
                                              STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                .build();
            case LONG:
                return CodeBlock.builder()
                                .addStatement("$L.setLong($L, $L)",
                                              STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                .build();
            case FLOAT:
                return CodeBlock.builder()
                                .addStatement("$L.setFloat($L, $L)",
                                              STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                .build();
            case DOUBLE:
                return CodeBlock.builder()
                                .addStatement("$L.setDouble($L, $L)",
                                              STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                .build();
            case ARRAY:
                ArrayType arrayType = (ArrayType) type;
                TypeMirror componentType = arrayType.getComponentType();
                if (componentType.getKind() == BYTE) {
                    return CodeBlock.builder()
                                    .addStatement("$L.setBytes($L, $L)",
                                                  STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                    .build();
                }
                return CodeBlock.builder()
                                .beginControlFlow("for ($T $L : $L)",
                                                  componentType, "element", valueSnippet)
                                .add(render(componentType, index, "element"))
                                .endControlFlow()
                                .build();
            case DECLARED:
                DeclaredType declaredType = (DeclaredType) type;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                switch (typeElement.getQualifiedName().toString()) {
                    case "java.lang.String":
                        return CodeBlock.builder()
                                        .addStatement("$L.setString($L, $L)",
                                                      STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                        .build();
                    case "java.math.BigInteger":
                        return CodeBlock.builder()
                                        .addStatement("$L.setBigDecimal($L, new $T($L))",
                                                      STATEMENT_VARIABLE_NAME, index, BigDecimal.class, valueSnippet)
                                        .build();
                    case "java.math.BigDecimal":
                        return CodeBlock.builder()
                                        .addStatement("$L.setBigDecimal($L, $L)",
                                                      STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                        .build();
                    case "java.sql.Date":
                        return CodeBlock.builder()
                                        .addStatement("$L.setDate($L, $L)",
                                                      STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                        .build();
                    case "java.sql.Time":
                        return CodeBlock.builder()
                                        .addStatement("$L.setTime($L, $L)",
                                                      STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                        .build();
                    case "java.sql.Timestamp":
                        return CodeBlock.builder()
                                        .addStatement("$L.setTimestamp($L, $L)",
                                                      STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                        .build();
                    case "java.time.Instant":
                        return CodeBlock.builder()
                                        .addStatement("$L.setTimestamp($L, $L != null ? $T.from($L) : null)",
                                                      STATEMENT_VARIABLE_NAME,
                                                      index, valueSnippet, Timestamp.class, valueSnippet)
                                        .build();
                    case "java.time.LocalDate":
                        return CodeBlock.builder()
                                        .addStatement("$L.setDate($L, $L != null ? $T.valueOf($L) : null)",
                                                      STATEMENT_VARIABLE_NAME,
                                                      index, valueSnippet, Date.class, valueSnippet)
                                        .build();
                    case "java.time.LocalTime":
                        return CodeBlock.builder()
                                        .addStatement("$L.setTime($L, $L != null ? $T.valueOf($L) : null)",
                                                      STATEMENT_VARIABLE_NAME,
                                                      index, valueSnippet, Time.class, valueSnippet)
                                        .build();
                    case "java.time.LocalDateTime":
                        return CodeBlock.builder()
                                        .addStatement("$L.setTimestamp($L, $L != null ? $T.valueOf($L) : null)",
                                                      STATEMENT_VARIABLE_NAME,
                                                      index, valueSnippet, Timestamp.class, valueSnippet)
                                        .build();
                    case "java.util.UUID":
                        return CodeBlock.builder()
                                        .addStatement("$L.setString($L, $L != null ? $L.toString() : null)",
                                                      STATEMENT_VARIABLE_NAME,
                                                      index, valueSnippet, valueSnippet)
                                        .build();
                    case "java.lang.Iterable":
                    case "java.util.Collection":
                    case "java.util.List":
                    case "java.util.Set":
                        TypeMirror genericType = declaredType.getTypeArguments().get(0);
                        return CodeBlock.builder()
                                        .beginControlFlow("for ($T $L : $L)",
                                                          genericType, "element", valueSnippet)
                                        .add(render(genericType, index, "element"))
                                        .endControlFlow()
                                        .build();
                    default:
                        if (typeElement.getKind() == ENUM) {
                            return CodeBlock.builder()
                                            .addStatement("$L.setString($L, $L != null ? $L.name() : null)",
                                                          STATEMENT_VARIABLE_NAME,
                                                          index, valueSnippet, valueSnippet)
                                            .build();
                        }
                        return CodeBlock.builder()
                                        .addStatement("$L.setObject($L, $L)",
                                                      STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                        .build();
                }
            case NULL:
                return CodeBlock.builder()
                                // TODO: switch to using PreparedStatement.setNull(..)
                                .addStatement("$L.setObject($L, null)",
                                              STATEMENT_VARIABLE_NAME, index)
                                .build();
            default:
                return CodeBlock.builder().build();
        }
    }
}
