package io.graphine.processor.code.renderer.mapping;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import static io.graphine.processor.code.generator.infrastructure.AttributeMappingGenerator.AttributeMappers;
import static io.graphine.processor.code.generator.repository.method.RepositoryMethodImplementationGenerator.RESULT_SET_VARIABLE_NAME;
import static javax.lang.model.element.ElementKind.ENUM;
import static javax.lang.model.type.TypeKind.BYTE;

/**
 * @author Oleg Marchenko
 */
public final class ResultSetMappingRenderer {
    public CodeBlock render(TypeMirror type, String index) {
        switch (type.getKind()) {
            case BOOLEAN:
                return CodeBlock.of("$T.getBoolean($L, $L)",
                                    AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
            case BYTE:
                return CodeBlock.of("$T.getByte($L, $L)",
                                    AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
            case SHORT:
                return CodeBlock.of("$T.getShort($L, $L)",
                                    AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
            case INT:
                return CodeBlock.of("$T.getInt($L, $L)",
                                    AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
            case LONG:
                return CodeBlock.of("$T.getLong($L, $L)",
                                    AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
            case FLOAT:
                return CodeBlock.of("$T.getFloat($L, $L)",
                                    AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
            case DOUBLE:
                return CodeBlock.of("$T.getDouble($L, $L)",
                                    AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
            case ARRAY:
                ArrayType arrayType = (ArrayType) type;
                TypeMirror componentType = arrayType.getComponentType();
                if (componentType.getKind() == BYTE) {
                    return CodeBlock.of("$T.getBytes($L, $L)",
                                        AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
                }
                return CodeBlock.builder().build();
            case DECLARED:
                DeclaredType declaredType = (DeclaredType) type;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                switch (typeElement.getQualifiedName().toString()) {
                    case "java.lang.Boolean":
                        return CodeBlock.of("$T.getBooleanWrapper($L, $L)",
                                            AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
                    case "java.lang.Byte":
                        return CodeBlock.of("$T.getByteWrapper($L, $L)",
                                            AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
                    case "java.lang.Short":
                        return CodeBlock.of("$T.getShortWrapper($L, $L)",
                                            AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
                    case "java.lang.Integer":
                        return CodeBlock.of("$T.getIntWrapper($L, $L)",
                                            AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
                    case "java.lang.Long":
                        return CodeBlock.of("$T.getLongWrapper($L, $L)",
                                            AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
                    case "java.lang.Float":
                        return CodeBlock.of("$T.getFloatWrapper($L, $L)",
                                            AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
                    case "java.lang.Double":
                        return CodeBlock.of("$T.getDoubleWrapper($L, $L)",
                                            AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
                    case "java.math.BigDecimal":
                        return CodeBlock.of("$T.getBigDecimal($L, $L)",
                                            AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
                    case "java.math.BigInteger":
                        return CodeBlock.of("$T.getBigInteger($L, $L)",
                                            AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
                    case "java.lang.String":
                        return CodeBlock.of("$T.getString($L, $L)",
                                            AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
                    case "java.sql.Date":
                        return CodeBlock.of("$T.getDate($L, $L)",
                                            AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
                    case "java.sql.Time":
                        return CodeBlock.of("$T.getTime($L, $L)",
                                            AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
                    case "java.sql.Timestamp":
                        return CodeBlock.of("$T.getTimestamp($L, $L)",
                                            AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
                    case "java.time.Instant":
                        return CodeBlock.of("$T.getInstant($L, $L)",
                                            AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
                    case "java.time.LocalDate":
                        return CodeBlock.of("$T.getLocalDate($L, $L)",
                                            AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
                    case "java.time.LocalTime":
                        return CodeBlock.of("$T.getLocalTime($L, $L)",
                                            AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
                    case "java.time.LocalDateTime":
                        return CodeBlock.of("$T.getLocalDateTime($L, $L)",
                                            AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
                    case "java.time.Year":
                        return CodeBlock.of("$T.getYear($L, $L)",
                                            AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
                    case "java.time.YearMonth":
                        return CodeBlock.of("$T.getYearMonth($L, $L)",
                                            AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
                    case "java.time.MonthDay":
                        return CodeBlock.of("$T.getMonthDay($L, $L)",
                                            AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
                    case "java.time.Period":
                        return CodeBlock.of("$T.getPeriod($L, $L)",
                                            AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
                    case "java.time.Duration":
                        return CodeBlock.of("$T.getDuration($L, $L)",
                                            AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
                    case "java.util.UUID":
                        return CodeBlock.of("$T.getUuid($L, $L)",
                                            AttributeMappers, RESULT_SET_VARIABLE_NAME, index);
                    default:
                        if (typeElement.getKind() == ENUM) {
                            return CodeBlock.of("$T.getEnum($L, $L, $T.class)",
                                                AttributeMappers, RESULT_SET_VARIABLE_NAME, index, type);
                        }
                        return CodeBlock.of("($T) $L.getObject($L)",
                                            type, RESULT_SET_VARIABLE_NAME, index);
                }
            default:
                return CodeBlock.builder().build();
        }
    }
}
