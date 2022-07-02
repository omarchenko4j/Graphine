package io.graphine.processor.code.renderer.mapping;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import static io.graphine.processor.code.generator.entity.AttributeMappingGenerator.AttributeMappers;
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
                                .addStatement("$T.setBoolean($L, $L, $L)",
                                              AttributeMappers, STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                .build();
            case BYTE:
                return CodeBlock.builder()
                                .addStatement("$T.setByte($L, $L, $L)",
                                              AttributeMappers, STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                .build();
            case SHORT:
                return CodeBlock.builder()
                                .addStatement("$T.setShort($L, $L, $L)",
                                              AttributeMappers, STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                .build();
            case INT:
                return CodeBlock.builder()
                                .addStatement("$T.setInt($L, $L, $L)",
                                              AttributeMappers, STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                .build();
            case LONG:
                return CodeBlock.builder()
                                .addStatement("$T.setLong($L, $L, $L)",
                                              AttributeMappers, STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                .build();
            case FLOAT:
                return CodeBlock.builder()
                                .addStatement("$T.setFloat($L, $L, $L)",
                                              AttributeMappers, STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                .build();
            case DOUBLE:
                return CodeBlock.builder()
                                .addStatement("$T.setDouble($L, $L, $L)",
                                              AttributeMappers, STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                .build();
            case ARRAY:
                ArrayType arrayType = (ArrayType) type;
                TypeMirror componentType = arrayType.getComponentType();
                if (componentType.getKind() == BYTE) {
                    return CodeBlock.builder()
                                    .addStatement("$T.setBytes($L, $L, $L)",
                                                  AttributeMappers, STATEMENT_VARIABLE_NAME, index, valueSnippet)
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
                    case "java.lang.Boolean":
                        return CodeBlock.builder()
                                .addStatement("$T.setBooleanWrapper($L, $L, $L)",
                                            AttributeMappers, STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                .build();
                    case "java.lang.Byte":
                        return CodeBlock.builder()
                                .addStatement("$T.setByteWrapper($L, $L, $L)",
                                            AttributeMappers, STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                .build();
                    case "java.lang.Short":
                        return CodeBlock.builder()
                                .addStatement("$T.setShortWrapper($L, $L, $L)",
                                            AttributeMappers, STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                .build();
                    case "java.lang.Integer":
                        return CodeBlock.builder()
                                .addStatement("$T.setIntWrapper($L, $L, $L)",
                                            AttributeMappers, STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                        .build();
                    case "java.lang.Long":
                        return CodeBlock.builder()
                                .addStatement("$T.setLongWrapper($L, $L, $L)",
                                            AttributeMappers, STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                        .build();
                    case "java.lang.Float":
                        return CodeBlock.builder()
                                .addStatement("$T.setFloatWrapper($L, $L, $L)",
                                            AttributeMappers, STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                        .build();
                    case "java.lang.Double":
                        return CodeBlock.builder()
                                .addStatement("$T.setDoubleWrapper($L, $L, $L)",
                                            AttributeMappers, STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                        .build();
                    case "java.math.BigDecimal":
                        return CodeBlock.builder()
                                .addStatement("$T.setBigDecimal($L, $L, $L)",
                                            AttributeMappers, STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                        .build();
                    case "java.math.BigInteger":
                        return CodeBlock.builder()
                                .addStatement("$T.setBigInteger($L, $L, $L)",
                                            AttributeMappers, STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                        .build();

                    case "java.lang.String":
                        return CodeBlock.builder()
                                        .addStatement("$T.setString($L, $L, $L)",
                                                      AttributeMappers, STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                        .build();
                    case "java.sql.Date":
                        return CodeBlock.builder()
                                        .addStatement("$T.setDate($L, $L, $L)",
                                                      AttributeMappers, STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                        .build();
                    case "java.sql.Time":
                        return CodeBlock.builder()
                                        .addStatement("$T.setTime($L, $L, $L)",
                                                      AttributeMappers, STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                        .build();
                    case "java.sql.Timestamp":
                        return CodeBlock.builder()
                                        .addStatement("$T.setTimestamp($L, $L, $L)",
                                                      AttributeMappers, STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                        .build();
                    case "java.time.Instant":
                        return CodeBlock.builder()
                                        .addStatement("$T.setInstant($L, $L, $L)",
                                                      AttributeMappers, STATEMENT_VARIABLE_NAME,
                                                      index, valueSnippet)
                                        .build();
                    case "java.time.LocalDate":
                        return CodeBlock.builder()
                                        .addStatement("$T.setLocalDate($L, $L, $L)",
                                                      AttributeMappers, STATEMENT_VARIABLE_NAME,
                                                      index, valueSnippet)
                                        .build();
                    case "java.time.LocalTime":
                        return CodeBlock.builder()
                                        .addStatement("$T.setLocalTime($L, $L, $L)",
                                                      AttributeMappers, STATEMENT_VARIABLE_NAME,
                                                      index, valueSnippet)
                                        .build();
                    case "java.time.LocalDateTime":
                        return CodeBlock.builder()
                                        .addStatement("$T.setLocalDateTime($L, $L, $L)",
                                                      AttributeMappers, STATEMENT_VARIABLE_NAME,
                                                      index, valueSnippet)
                                        .build();
                    case "java.util.UUID":
                        return CodeBlock.builder()
                                        .addStatement("$T.setUuid($L, $L, $L)",
                                                      AttributeMappers, STATEMENT_VARIABLE_NAME,
                                                      index, valueSnippet)
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
                                            .addStatement("$T.setEnum($L, $L, $L)",
                                                          AttributeMappers, STATEMENT_VARIABLE_NAME,
                                                          index, valueSnippet)
                                            .build();
                        }
                        return CodeBlock.builder()
                                        .addStatement("$L.setObject($L, $L)",
                                                      STATEMENT_VARIABLE_NAME, index, valueSnippet)
                                        .build();
                }
            case NULL:
                return CodeBlock.builder()
                                .addStatement("$T.setNull($L, $L)",
                                              AttributeMappers, STATEMENT_VARIABLE_NAME, index)
                                .build();
            default:
                return CodeBlock.builder().build();
        }
    }
}
