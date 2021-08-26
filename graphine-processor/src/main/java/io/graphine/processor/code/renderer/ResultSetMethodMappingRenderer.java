package io.graphine.processor.code.renderer;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.UUID;

import static io.graphine.processor.code.generator.repository.method.RepositoryMethodImplementationGenerator.RESULT_SET_VARIABLE_NAME;
import static javax.lang.model.element.ElementKind.ENUM;
import static javax.lang.model.type.TypeKind.BYTE;

/**
 * @author Oleg Marchenko
 */
public class ResultSetMethodMappingRenderer {
    public CodeBlock render(TypeMirror parameterType, String parameterIndex) {
        switch (parameterType.getKind()) {
            case BOOLEAN:
                return CodeBlock.of("$L.getBoolean($L)",
                                    RESULT_SET_VARIABLE_NAME,
                                    parameterIndex);
            case BYTE:
                return CodeBlock.of("$L.getByte($L)",
                                    RESULT_SET_VARIABLE_NAME,
                                    parameterIndex);
            case SHORT:
                return CodeBlock.of("$L.getShort($L)",
                                    RESULT_SET_VARIABLE_NAME,
                                    parameterIndex);
            case INT:
                return CodeBlock.of("$L.getInt($L)",
                                    RESULT_SET_VARIABLE_NAME,
                                    parameterIndex);
            case LONG:
                return CodeBlock.of("$L.getLong($L)",
                                    RESULT_SET_VARIABLE_NAME,
                                    parameterIndex);
            case FLOAT:
                return CodeBlock.of("$L.getFloat($L)",
                                    RESULT_SET_VARIABLE_NAME,
                                    parameterIndex);
            case DOUBLE:
                return CodeBlock.of("$L.getDouble($L)",
                                    RESULT_SET_VARIABLE_NAME,
                                    parameterIndex);
            case ARRAY:
                ArrayType arrayType = (ArrayType) parameterType;
                TypeMirror componentType = arrayType.getComponentType();
                if (componentType.getKind() == BYTE) {
                    return CodeBlock.of("$L.getBytes($L)",
                                        RESULT_SET_VARIABLE_NAME,
                                        parameterIndex);
                }
                throw new IllegalStateException("Unsupported array type: " + parameterType);
            case DECLARED:
                DeclaredType declaredType = (DeclaredType) parameterType;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                switch (typeElement.getQualifiedName().toString()) {
                    case "java.lang.String":
                        return CodeBlock.of("$L.getString($L)",
                                            RESULT_SET_VARIABLE_NAME,
                                            parameterIndex);
                    case "java.math.BigDecimal":
                        return CodeBlock.of("$L.getBigDecimal($L)",
                                            RESULT_SET_VARIABLE_NAME,
                                            parameterIndex);
                    case "java.sql.Date":
                        return CodeBlock.of("$L.getDate($L)",
                                            RESULT_SET_VARIABLE_NAME,
                                            parameterIndex);
                    case "java.sql.Time":
                        return CodeBlock.of("$L.getTime($L)",
                                            RESULT_SET_VARIABLE_NAME,
                                            parameterIndex);
                    case "java.sql.Timestamp":
                        return CodeBlock.of("$L.getTimestamp($L)",
                                            RESULT_SET_VARIABLE_NAME,
                                            parameterIndex);
                    case "java.time.Instant":
                        return CodeBlock.of("$L.wasNull() ? null : $L.getTimestamp($L).toInstant()",
                                            RESULT_SET_VARIABLE_NAME,
                                            RESULT_SET_VARIABLE_NAME,
                                            parameterIndex);
                    case "java.time.LocalDate":
                        return CodeBlock.of("$L.wasNull() ? null : $L.getDate($L).toLocalDate()",
                                            RESULT_SET_VARIABLE_NAME,
                                            RESULT_SET_VARIABLE_NAME,
                                            parameterIndex);
                    case "java.time.LocalTime":
                        return CodeBlock.of("$L.wasNull() ? null : $L.getTime($L).toLocalTime()",
                                            RESULT_SET_VARIABLE_NAME,
                                            RESULT_SET_VARIABLE_NAME,
                                            parameterIndex);
                    case "java.time.LocalDateTime":
                        return CodeBlock.of("$L.wasNull() ? null : $L.getTimestamp($L).toLocalDateTime()",
                                            RESULT_SET_VARIABLE_NAME,
                                            RESULT_SET_VARIABLE_NAME,
                                            parameterIndex);
                    case "java.util.UUID":
                        return CodeBlock.of("$L.wasNull() ? null : $T.fromString($L.getString($L))",
                                            RESULT_SET_VARIABLE_NAME,
                                            UUID.class,
                                            RESULT_SET_VARIABLE_NAME,
                                            parameterIndex);
                    default:
                        if (typeElement.getKind() == ENUM) {
                            return CodeBlock.of("$L.wasNull() ? null : $T.valueOf($L.getString($L))",
                                                RESULT_SET_VARIABLE_NAME,
                                                parameterType,
                                                RESULT_SET_VARIABLE_NAME,
                                                parameterIndex);
                        }
                        return CodeBlock.of("($T) $L.getObject($L)",
                                            parameterType,
                                            RESULT_SET_VARIABLE_NAME,
                                            parameterIndex);
                }
            default:
                throw new IllegalStateException("Unsupported parameter type: " + parameterType);
        }
    }
}
