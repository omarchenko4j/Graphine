package io.graphine.processor.code.generator.infrastructure;

import com.squareup.javapoet.*;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.time.*;
import java.util.UUID;

import static io.graphine.processor.support.EnvironmentContext.filer;
import static io.graphine.processor.support.EnvironmentContext.messager;

/**
 * TODO: quick sketch - refactoring candidate
 *
 * @author Oleg Marchenko
 */
public class AttributeMappingGenerator {
    public static final ClassName AttributeMappers = ClassName.get("io.graphine.core", "AttributeMappers");

    public void generate() {
        TypeSpec.Builder classBuilder = TypeSpec
                .classBuilder(AttributeMappers)
                .addAnnotation(AnnotationSpec.builder(Generated.class)
                                             .addMember("value", "$S", "io.graphine.processor.GraphineProcessor")
                                             .build())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                                   .addModifiers(Modifier.PRIVATE)
                                   .build());

        MethodSpec.Builder getBooleanMethodBuilder =
                MethodSpec.methodBuilder("getBoolean")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(boolean.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("return resultSet.getBoolean(columnIndex)");
        classBuilder.addMethod(getBooleanMethodBuilder.build());

        MethodSpec.Builder getByteMethodBuilder =
                MethodSpec.methodBuilder("getByte")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(byte.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("return resultSet.getByte(columnIndex)");
        classBuilder.addMethod(getByteMethodBuilder.build());

        MethodSpec.Builder getShortMethodBuilder =
                MethodSpec.methodBuilder("getShort")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(short.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("return resultSet.getShort(columnIndex)");
        classBuilder.addMethod(getShortMethodBuilder.build());

        MethodSpec.Builder getIntMethodBuilder =
                MethodSpec.methodBuilder("getInt")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(int.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("return resultSet.getInt(columnIndex)");
        classBuilder.addMethod(getIntMethodBuilder.build());

        MethodSpec.Builder getLongMethodBuilder =
                MethodSpec.methodBuilder("getLong")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(long.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("return resultSet.getLong(columnIndex)");
        classBuilder.addMethod(getLongMethodBuilder.build());

        MethodSpec.Builder getFloatMethodBuilder =
                MethodSpec.methodBuilder("getFloat")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(float.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("return resultSet.getFloat(columnIndex)");
        classBuilder.addMethod(getFloatMethodBuilder.build());

        MethodSpec.Builder getDoubleMethodBuilder =
                MethodSpec.methodBuilder("getDouble")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(double.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("return resultSet.getDouble(columnIndex)");
        classBuilder.addMethod(getDoubleMethodBuilder.build());

        MethodSpec.Builder getBooleanWrapperMethodBuilder =
                MethodSpec.methodBuilder("getBooleanWrapper")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(Boolean.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("$T columnValue = resultSet.getObject(columnIndex)", Object.class)
                        .beginControlFlow("if (columnValue instanceof $T)", Boolean.class)
                        .addStatement("return ($T) columnValue", Boolean.class)
                        .endControlFlow()
                        .beginControlFlow("if (columnValue instanceof $T)", String.class)
                        .addStatement("return $T.valueOf(($T) columnValue)", Boolean.class, String.class)
                        .endControlFlow()
                        .addStatement("return null");
        classBuilder.addMethod(getBooleanWrapperMethodBuilder.build());

        MethodSpec.Builder getByteWrapperMethodBuilder =
                MethodSpec.methodBuilder("getByteWrapper")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(Byte.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("$T columnValue = resultSet.getObject(columnIndex)", Object.class)
                        .beginControlFlow("if (columnValue instanceof $T)", Byte.class)
                        .addStatement("return ($T) columnValue", Byte.class)
                        .endControlFlow()
                        .beginControlFlow("if (columnValue instanceof $T)", Number.class)
                        .addStatement("$T byteValue = (($T) columnValue).byteValue()", byte.class, Number.class)
                        .addStatement("return $T.valueOf(byteValue)", Byte.class)
                        .endControlFlow()
                        .addStatement("return null");
        classBuilder.addMethod(getByteWrapperMethodBuilder.build());

        MethodSpec.Builder getShortWrapperMethodBuilder =
                MethodSpec.methodBuilder("getShortWrapper")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(Short.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("$T columnValue = resultSet.getObject(columnIndex)", Object.class)
                        .beginControlFlow("if (columnValue instanceof $T)", Short.class)
                        .addStatement("return ($T) columnValue", Short.class)
                        .endControlFlow()
                        .beginControlFlow("if (columnValue instanceof $T)", Number.class)
                        .addStatement("$T shortValue = (($T) columnValue).shortValue()", short.class, Number.class)
                        .addStatement("return $T.valueOf(shortValue)", Short.class)
                        .endControlFlow()
                        .addStatement("return null");
        classBuilder.addMethod(getShortWrapperMethodBuilder.build());

        MethodSpec.Builder getIntWrapperMethodBuilder =
                MethodSpec.methodBuilder("getIntWrapper")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(Integer.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("$T columnValue = resultSet.getObject(columnIndex)", Object.class)
                        .beginControlFlow("if (columnValue instanceof $T)", Integer.class)
                        .addStatement("return ($T) columnValue", Integer.class)
                        .endControlFlow()
                        .beginControlFlow("if (columnValue instanceof $T)", Number.class)
                        .addStatement("$T intValue = (($T) columnValue).intValue()", int.class, Number.class)
                        .addStatement("return $T.valueOf(intValue)", Integer.class)
                        .endControlFlow()
                        .addStatement("return null");
        classBuilder.addMethod(getIntWrapperMethodBuilder.build());

        MethodSpec.Builder getLongWrapperMethodBuilder =
                MethodSpec.methodBuilder("getLongWrapper")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(Long.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("$T columnValue = resultSet.getObject(columnIndex)", Object.class)
                        .beginControlFlow("if (columnValue instanceof $T)", Long.class)
                        .addStatement("return ($T) columnValue", Long.class)
                        .endControlFlow()
                        .beginControlFlow("if (columnValue instanceof $T)", Number.class)
                        .addStatement("$T longValue = (($T) columnValue).longValue()", long.class, Number.class)
                        .addStatement("return $T.valueOf(longValue)", Long.class)
                        .endControlFlow()
                        .addStatement("return null");
        classBuilder.addMethod(getLongWrapperMethodBuilder.build());

        MethodSpec.Builder getFloatWrapperMethodBuilder =
                MethodSpec.methodBuilder("getFloatWrapper")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(Float.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("$T columnValue = resultSet.getObject(columnIndex)", Object.class)
                        .beginControlFlow("if (columnValue instanceof $T)", Float.class)
                        .addStatement("return ($T) columnValue", Float.class)
                        .endControlFlow()
                        .beginControlFlow("if (columnValue instanceof $T)", Number.class)
                        .addStatement("$T floatValue = (($T) columnValue).floatValue()", float.class, Number.class)
                        .addStatement("return $T.valueOf(floatValue)", Float.class)
                        .endControlFlow()
                        .addStatement("return null");
        classBuilder.addMethod(getFloatWrapperMethodBuilder.build());

        MethodSpec.Builder getDoubleWrapperMethodBuilder =
                MethodSpec.methodBuilder("getDoubleWrapper")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(Double.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("$T columnValue = resultSet.getObject(columnIndex)", Object.class)
                        .beginControlFlow("if (columnValue instanceof $T)", Double.class)
                        .addStatement("return ($T) columnValue", Double.class)
                        .endControlFlow()
                        .beginControlFlow("if (columnValue instanceof $T)", Number.class)
                        .addStatement("$T doubleValue = (($T) columnValue).doubleValue()", double.class, Number.class)
                        .addStatement("return $T.valueOf(doubleValue)", Double.class)
                        .endControlFlow()
                        .addStatement("return null");
        classBuilder.addMethod(getDoubleWrapperMethodBuilder.build());

        MethodSpec.Builder getBigDecimalMethodBuilder =
                MethodSpec.methodBuilder("getBigDecimal")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(BigDecimal.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("return resultSet.getBigDecimal(columnIndex)");
        classBuilder.addMethod(getBigDecimalMethodBuilder.build());

        MethodSpec.Builder getBigIntegerMethodBuilder =
                MethodSpec.methodBuilder("getBigInteger")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(BigInteger.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("$T columnValue = resultSet.getObject(columnIndex)", Object.class)
                        .beginControlFlow("if (columnValue instanceof $T)", BigInteger.class)
                        .addStatement("return ($T) columnValue", BigInteger.class)
                        .endControlFlow()
                        .beginControlFlow("if (columnValue instanceof $T)", BigDecimal.class)
                        .addStatement("return (($T) columnValue).toBigInteger()", BigDecimal.class)
                        .endControlFlow()
                        .beginControlFlow("if (columnValue instanceof $T)", Number.class)
                        .addStatement("$T longValue = (($T) columnValue).longValue()", long.class, Number.class)
                        .addStatement("return $T.valueOf(longValue)", BigInteger.class)
                        .endControlFlow()
                        .addStatement("return null");
        classBuilder.addMethod(getBigIntegerMethodBuilder.build());

        MethodSpec.Builder getStringMethodBuilder =
                MethodSpec.methodBuilder("getString")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(String.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("return resultSet.getString(columnIndex)");
        classBuilder.addMethod(getStringMethodBuilder.build());

        MethodSpec.Builder getBytesMethodBuilder =
                MethodSpec.methodBuilder("getBytes")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(byte[].class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("return resultSet.getBytes(columnIndex)");
        classBuilder.addMethod(getBytesMethodBuilder.build());

        MethodSpec.Builder getDateMethodBuilder =
                MethodSpec.methodBuilder("getDate")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(Date.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("return resultSet.getDate(columnIndex)");
        classBuilder.addMethod(getDateMethodBuilder.build());

        MethodSpec.Builder getTimeMethodBuilder =
                MethodSpec.methodBuilder("getTime")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(Time.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("return resultSet.getTime(columnIndex)");
        classBuilder.addMethod(getTimeMethodBuilder.build());

        MethodSpec.Builder getTimestampMethodBuilder =
                MethodSpec.methodBuilder("getTimestamp")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(Timestamp.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("return resultSet.getTimestamp(columnIndex)");
        classBuilder.addMethod(getTimestampMethodBuilder.build());

        MethodSpec.Builder getInstantMethodBuilder =
                MethodSpec.methodBuilder("getInstant")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(Instant.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("$T columnValue = resultSet.getTimestamp(columnIndex)", Timestamp.class)
                        .beginControlFlow("if (columnValue != null)")
                        .addStatement("return columnValue.toInstant()")
                        .endControlFlow()
                        .addStatement("return null");
        classBuilder.addMethod(getInstantMethodBuilder.build());

        MethodSpec.Builder getLocalDateMethodBuilder =
                MethodSpec.methodBuilder("getLocalDate")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(LocalDate.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("$T columnValue = resultSet.getDate(columnIndex)", Date.class)
                        .beginControlFlow("if (columnValue != null)")
                        .addStatement("return columnValue.toLocalDate()")
                        .endControlFlow()
                        .addStatement("return null");
        classBuilder.addMethod(getLocalDateMethodBuilder.build());

        MethodSpec.Builder getLocalTimeMethodBuilder =
                MethodSpec.methodBuilder("getLocalTime")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(LocalTime.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("$T columnValue = resultSet.getTime(columnIndex)", Time.class)
                        .beginControlFlow("if (columnValue != null)")
                        .addStatement("return columnValue.toLocalTime()")
                        .endControlFlow()
                        .addStatement("return null");
        classBuilder.addMethod(getLocalTimeMethodBuilder.build());

        MethodSpec.Builder getLocalDateTimeMethodBuilder =
                MethodSpec.methodBuilder("getLocalDateTime")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(LocalDateTime.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("$T columnValue = resultSet.getTimestamp(columnIndex)", Timestamp.class)
                        .beginControlFlow("if (columnValue != null)")
                        .addStatement("return columnValue.toLocalDateTime()")
                        .endControlFlow()
                        .addStatement("return null");
        classBuilder.addMethod(getLocalDateTimeMethodBuilder.build());

        MethodSpec.Builder getYearMethodBuilder =
                MethodSpec.methodBuilder("getYear")
                          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                          .returns(Year.class)
                          .addParameter(ResultSet.class, "resultSet")
                          .addParameter(int.class, "columnIndex")
                          .addException(SQLException.class)
                          .addStatement("$T columnValue = resultSet.getInt(columnIndex)", int.class)
                          .addStatement("return $T.of(columnValue)", Year.class);
        classBuilder.addMethod(getYearMethodBuilder.build());

        MethodSpec.Builder getYearMonthMethodBuilder =
                MethodSpec.methodBuilder("getYearMonth")
                          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                          .returns(YearMonth.class)
                          .addParameter(ResultSet.class, "resultSet")
                          .addParameter(int.class, "columnIndex")
                          .addException(SQLException.class)
                          .addStatement("$T columnValue = resultSet.getString(columnIndex)", String.class)
                          .addStatement("return $T.parse(columnValue)", YearMonth.class);
        classBuilder.addMethod(getYearMonthMethodBuilder.build());

        MethodSpec.Builder getMonthDayMethodBuilder =
                MethodSpec.methodBuilder("getMonthDay")
                          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                          .returns(MonthDay.class)
                          .addParameter(ResultSet.class, "resultSet")
                          .addParameter(int.class, "columnIndex")
                          .addException(SQLException.class)
                          .addStatement("$T columnValue = resultSet.getString(columnIndex)", String.class)
                          .addStatement("return $T.parse(columnValue)", MonthDay.class);
        classBuilder.addMethod(getMonthDayMethodBuilder.build());

        MethodSpec.Builder getPeriodMethodBuilder =
                MethodSpec.methodBuilder("getPeriod")
                          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                          .returns(Period.class)
                          .addParameter(ResultSet.class, "resultSet")
                          .addParameter(int.class, "columnIndex")
                          .addException(SQLException.class)
                          .addStatement("$T columnValue = resultSet.getInt(columnIndex)", int.class)
                          .addStatement("return $T.ofDays(columnValue)", Period.class);
        classBuilder.addMethod(getPeriodMethodBuilder.build());

        MethodSpec.Builder getDurationMethodBuilder =
                MethodSpec.methodBuilder("getDuration")
                          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                          .returns(Duration.class)
                          .addParameter(ResultSet.class, "resultSet")
                          .addParameter(int.class, "columnIndex")
                          .addException(SQLException.class)
                          .addStatement("$T columnValue = resultSet.getLong(columnIndex)", long.class)
                          .addStatement("return $T.ofSeconds(columnValue)", Duration.class);
        classBuilder.addMethod(getDurationMethodBuilder.build());

        MethodSpec.Builder getUuidMethodBuilder =
                MethodSpec.methodBuilder("getUuid")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(UUID.class)
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("$T columnValue = resultSet.getObject(columnIndex)", Object.class)
                        .beginControlFlow("if (columnValue instanceof $T)", UUID.class)
                        .addStatement("return ($T) columnValue", UUID.class)
                        .endControlFlow()
                        .beginControlFlow("if (columnValue instanceof $T)", String.class)
                        .addStatement("return $T.fromString(($T) columnValue)", UUID.class, String.class)
                        .endControlFlow()
                        .addStatement("return null");
        classBuilder.addMethod(getUuidMethodBuilder.build());

        MethodSpec.Builder getEnumMethodBuilder =
                MethodSpec.methodBuilder("getEnum")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addTypeVariable(TypeVariableName.get("T", ParameterizedTypeName.get(ClassName.get(Enum.class), TypeVariableName.get("T"))))
                        .returns(TypeVariableName.get("T"))
                        .addParameter(ResultSet.class, "resultSet")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), TypeVariableName.get("T")), "classType")
                        .addException(SQLException.class)
                        .addStatement("$T columnValue = resultSet.getString(columnIndex)", String.class)
                        .beginControlFlow("if (columnValue != null)")
                        .addStatement("return $T.valueOf(classType, columnValue)", Enum.class)
                        .endControlFlow()
                        .addStatement("return null");
        classBuilder.addMethod(getEnumMethodBuilder.build());

        MethodSpec.Builder setBooleanMethodBuilder =
                MethodSpec.methodBuilder("setBoolean")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(boolean.class, "value")
                        .addException(SQLException.class)
                        .addStatement("statement.setBoolean(columnIndex, value)");
        classBuilder.addMethod(setBooleanMethodBuilder.build());

        MethodSpec.Builder setByteMethodBuilder =
                MethodSpec.methodBuilder("setByte")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(byte.class, "value")
                        .addException(SQLException.class)
                        .addStatement("statement.setByte(columnIndex, value)");
        classBuilder.addMethod(setByteMethodBuilder.build());

        MethodSpec.Builder setShortMethodBuilder =
                MethodSpec.methodBuilder("setShort")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(short.class, "value")
                        .addException(SQLException.class)
                        .addStatement("statement.setShort(columnIndex, value)");
        classBuilder.addMethod(setShortMethodBuilder.build());

        MethodSpec.Builder setIntMethodBuilder =
                MethodSpec.methodBuilder("setInt")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(int.class, "value")
                        .addException(SQLException.class)
                        .addStatement("statement.setInt(columnIndex, value)");
        classBuilder.addMethod(setIntMethodBuilder.build());

        MethodSpec.Builder setLongMethodBuilder =
                MethodSpec.methodBuilder("setLong")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(long.class, "value")
                        .addException(SQLException.class)
                        .addStatement("statement.setLong(columnIndex, value)");
        classBuilder.addMethod(setLongMethodBuilder.build());

        MethodSpec.Builder setFloatMethodBuilder =
                MethodSpec.methodBuilder("setFloat")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(float.class, "value")
                        .addException(SQLException.class)
                        .addStatement("statement.setFloat(columnIndex, value)");
        classBuilder.addMethod(setFloatMethodBuilder.build());

        MethodSpec.Builder setDoubleMethodBuilder =
                MethodSpec.methodBuilder("setDouble")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(double.class, "value")
                        .addException(SQLException.class)
                        .addStatement("statement.setDouble(columnIndex, value)");
        classBuilder.addMethod(setDoubleMethodBuilder.build());

        MethodSpec.Builder setBooleanWrapperMethodBuilder =
                MethodSpec.methodBuilder("setBooleanWrapper")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(Boolean.class, "value")
                        .addException(SQLException.class)
                        .beginControlFlow("if (value != null)")
                        .addStatement("statement.setBoolean(columnIndex, value.booleanValue())")
                        .nextControlFlow("else")
                        .addStatement("statement.setNull(columnIndex, $T.BOOLEAN)", Types.class)
                        .endControlFlow();
        classBuilder.addMethod(setBooleanWrapperMethodBuilder.build());

        MethodSpec.Builder setByteWrapperMethodBuilder =
                MethodSpec.methodBuilder("setByteWrapper")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(Byte.class, "value")
                        .addException(SQLException.class)
                        .beginControlFlow("if (value != null)")
                        .addStatement("statement.setByte(columnIndex, value.byteValue())")
                        .nextControlFlow("else")
                        .addStatement("statement.setNull(columnIndex, $T.TINYINT)", Types.class)
                        .endControlFlow();
        classBuilder.addMethod(setByteWrapperMethodBuilder.build());

        MethodSpec.Builder setShortWrapperMethodBuilder =
                MethodSpec.methodBuilder("setShortWrapper")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(Short.class, "value")
                        .addException(SQLException.class)
                        .beginControlFlow("if (value != null)")
                        .addStatement("statement.setShort(columnIndex, value.shortValue())")
                        .nextControlFlow("else")
                        .addStatement("statement.setNull(columnIndex, $T.SMALLINT)", Types.class)
                        .endControlFlow();
        classBuilder.addMethod(setShortWrapperMethodBuilder.build());

        MethodSpec.Builder setIntWrapperMethodBuilder =
                MethodSpec.methodBuilder("setIntWrapper")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(Integer.class, "value")
                        .addException(SQLException.class)
                        .beginControlFlow("if (value != null)")
                        .addStatement("statement.setInt(columnIndex, value.intValue())")
                        .nextControlFlow("else")
                        .addStatement("statement.setNull(columnIndex, $T.INTEGER)", Types.class)
                        .endControlFlow();
        classBuilder.addMethod(setIntWrapperMethodBuilder.build());

        MethodSpec.Builder setLongWrapperMethodBuilder =
                MethodSpec.methodBuilder("setLongWrapper")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(Long.class, "value")
                        .addException(SQLException.class)
                        .beginControlFlow("if (value != null)")
                        .addStatement("statement.setLong(columnIndex, value.longValue())")
                        .nextControlFlow("else")
                        .addStatement("statement.setNull(columnIndex, $T.BIGINT)", Types.class)
                        .endControlFlow();
        classBuilder.addMethod(setLongWrapperMethodBuilder.build());

        MethodSpec.Builder setFloatWrapperMethodBuilder =
                MethodSpec.methodBuilder("setFloatWrapper")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(Float.class, "value")
                        .addException(SQLException.class)
                        .beginControlFlow("if (value != null)")
                        .addStatement("statement.setFloat(columnIndex, value.floatValue())")
                        .nextControlFlow("else")
                        .addStatement("statement.setNull(columnIndex, $T.FLOAT)", Types.class)
                        .endControlFlow();
        classBuilder.addMethod(setFloatWrapperMethodBuilder.build());

        MethodSpec.Builder setDoubleWrapperMethodBuilder =
                MethodSpec.methodBuilder("setDoubleWrapper")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(Double.class, "value")
                        .addException(SQLException.class)
                        .beginControlFlow("if (value != null)")
                        .addStatement("statement.setDouble(columnIndex, value.doubleValue())")
                        .nextControlFlow("else")
                        .addStatement("statement.setNull(columnIndex, $T.DOUBLE)", Types.class)
                        .endControlFlow();
        classBuilder.addMethod(setDoubleWrapperMethodBuilder.build());

        MethodSpec.Builder setBigDecimalMethodBuilder =
                MethodSpec.methodBuilder("setBigDecimal")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(BigDecimal.class, "value")
                        .addException(SQLException.class)
                        .beginControlFlow("if (value != null)")
                        .addStatement("statement.setBigDecimal(columnIndex, value)")
                        .nextControlFlow("else")
                        .addStatement("statement.setNull(columnIndex, $T.DECIMAL)", Types.class)
                        .endControlFlow();
        classBuilder.addMethod(setBigDecimalMethodBuilder.build());

        MethodSpec.Builder setBigIntegerMethodBuilder =
                MethodSpec.methodBuilder("setBigInteger")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(BigInteger.class, "value")
                        .addException(SQLException.class)
                        .beginControlFlow("if (value != null)")
                        .addStatement("statement.setBigDecimal(columnIndex, new $T(value))", BigDecimal.class)
                        .nextControlFlow("else")
                        .addStatement("statement.setNull(columnIndex, $T.DECIMAL)", Types.class)
                        .endControlFlow();
        classBuilder.addMethod(setBigIntegerMethodBuilder.build());

        MethodSpec.Builder setStringMethodBuilder =
                MethodSpec.methodBuilder("setString")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(String.class, "value")
                        .addException(SQLException.class)
                        .beginControlFlow("if (value != null)")
                        .addStatement("statement.setString(columnIndex, value)")
                        .nextControlFlow("else")
                        .addStatement("statement.setNull(columnIndex, $T.VARCHAR)", Types.class)
                        .endControlFlow();
        classBuilder.addMethod(setStringMethodBuilder.build());

        MethodSpec.Builder setBytesMethodBuilder =
                MethodSpec.methodBuilder("setBytes")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(byte[].class, "value")
                        .addException(SQLException.class)
                        .beginControlFlow("if (value != null)")
                        .addStatement("statement.setBytes(columnIndex, value)")
                        .nextControlFlow("else")
                        .addStatement("statement.setNull(columnIndex, $T.ARRAY)", Types.class)
                        .endControlFlow();
        classBuilder.addMethod(setBytesMethodBuilder.build());

        MethodSpec.Builder setDateMethodBuilder =
                MethodSpec.methodBuilder("setDate")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(Date.class, "value")
                        .addException(SQLException.class)
                        .beginControlFlow("if (value != null)")
                        .addStatement("statement.setDate(columnIndex, value)")
                        .nextControlFlow("else")
                        .addStatement("statement.setNull(columnIndex, $T.DATE)", Types.class)
                        .endControlFlow();
        classBuilder.addMethod(setDateMethodBuilder.build());

        MethodSpec.Builder setTimeMethodBuilder =
                MethodSpec.methodBuilder("setTime")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(Time.class, "value")
                        .addException(SQLException.class)
                        .beginControlFlow("if (value != null)")
                        .addStatement("statement.setTime(columnIndex, value)")
                        .nextControlFlow("else")
                        .addStatement("statement.setNull(columnIndex, $T.TIME)", Types.class)
                        .endControlFlow();
        classBuilder.addMethod(setTimeMethodBuilder.build());

        MethodSpec.Builder setTimestampMethodBuilder =
                MethodSpec.methodBuilder("setTimestamp")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(Timestamp.class, "value")
                        .addException(SQLException.class)
                        .beginControlFlow("if (value != null)")
                        .addStatement("statement.setTimestamp(columnIndex, value)")
                        .nextControlFlow("else")
                        .addStatement("statement.setNull(columnIndex, $T.TIMESTAMP)", Types.class)
                        .endControlFlow();
        classBuilder.addMethod(setTimestampMethodBuilder.build());

        MethodSpec.Builder setInstantMethodBuilder =
                MethodSpec.methodBuilder("setInstant")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(Instant.class, "value")
                        .addException(SQLException.class)
                        .beginControlFlow("if (value != null)")
                        .addStatement("statement.setTimestamp(columnIndex, $T.from(value))", Timestamp.class)
                        .nextControlFlow("else")
                        .addStatement("statement.setNull(columnIndex, $T.TIMESTAMP)", Types.class)
                        .endControlFlow();
        classBuilder.addMethod(setInstantMethodBuilder.build());

        MethodSpec.Builder setLocalDateMethodBuilder =
                MethodSpec.methodBuilder("setLocalDate")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(LocalDate.class, "value")
                        .addException(SQLException.class)
                        .beginControlFlow("if (value != null)")
                        .addStatement("statement.setDate(columnIndex, $T.valueOf(value))", Date.class)
                        .nextControlFlow("else")
                        .addStatement("statement.setNull(columnIndex, $T.DATE)", Types.class)
                        .endControlFlow();
        classBuilder.addMethod(setLocalDateMethodBuilder.build());

        MethodSpec.Builder setLocalTimeMethodBuilder =
                MethodSpec.methodBuilder("setLocalTime")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(LocalTime.class, "value")
                        .addException(SQLException.class)
                        .beginControlFlow("if (value != null)")
                        .addStatement("statement.setTime(columnIndex, $T.valueOf(value))", Time.class)
                        .nextControlFlow("else")
                        .addStatement("statement.setNull(columnIndex, $T.TIME)", Types.class)
                        .endControlFlow();
        classBuilder.addMethod(setLocalTimeMethodBuilder.build());

        MethodSpec.Builder setLocalDateTimeMethodBuilder =
                MethodSpec.methodBuilder("setLocalDateTime")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(LocalDateTime.class, "value")
                        .addException(SQLException.class)
                        .beginControlFlow("if (value != null)")
                        .addStatement("statement.setTimestamp(columnIndex, $T.valueOf(value))", Timestamp.class)
                        .nextControlFlow("else")
                        .addStatement("statement.setNull(columnIndex, $T.TIMESTAMP)", Types.class)
                        .endControlFlow();
        classBuilder.addMethod(setLocalDateTimeMethodBuilder.build());

        MethodSpec.Builder setYearMethodBuilder =
                MethodSpec.methodBuilder("setYear")
                          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                          .returns(void.class)
                          .addParameter(PreparedStatement.class, "statement")
                          .addParameter(int.class, "columnIndex")
                          .addParameter(Year.class, "value")
                          .addException(SQLException.class)
                          .beginControlFlow("if (value != null)")
                          .addStatement("statement.setInt(columnIndex, value.getValue())")
                          .nextControlFlow("else")
                          .addStatement("statement.setNull(columnIndex, $T.INTEGER)", Types.class)
                          .endControlFlow();
        classBuilder.addMethod(setYearMethodBuilder.build());

        MethodSpec.Builder setYearMonthMethodBuilder =
                MethodSpec.methodBuilder("setYearMonth")
                          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                          .returns(void.class)
                          .addParameter(PreparedStatement.class, "statement")
                          .addParameter(int.class, "columnIndex")
                          .addParameter(YearMonth.class, "value")
                          .addException(SQLException.class)
                          .beginControlFlow("if (value != null)")
                          .addStatement("$T preparedValue = value.getYear() + $S + value.getMonthValue()",
                                        String.class, "-")
                          .addStatement("statement.setString(columnIndex, preparedValue)")
                          .nextControlFlow("else")
                          .addStatement("statement.setNull(columnIndex, $T.VARCHAR)", Types.class)
                          .endControlFlow();
        classBuilder.addMethod(setYearMonthMethodBuilder.build());

        MethodSpec.Builder setMonthDayMethodBuilder =
                MethodSpec.methodBuilder("setMonthDay")
                          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                          .returns(void.class)
                          .addParameter(PreparedStatement.class, "statement")
                          .addParameter(int.class, "columnIndex")
                          .addParameter(MonthDay.class, "value")
                          .addException(SQLException.class)
                          .beginControlFlow("if (value != null)")
                          .addStatement("$T preparedValue = $S + value.getMonthValue() + $S + value.getDayOfMonth()",
                                        String.class, "--", "-")
                          .addStatement("statement.setString(columnIndex, preparedValue)")
                          .nextControlFlow("else")
                          .addStatement("statement.setNull(columnIndex, $T.VARCHAR)", Types.class)
                          .endControlFlow();
        classBuilder.addMethod(setMonthDayMethodBuilder.build());

        MethodSpec.Builder setPeriodMethodBuilder =
                MethodSpec.methodBuilder("setPeriod")
                          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                          .returns(void.class)
                          .addParameter(PreparedStatement.class, "statement")
                          .addParameter(int.class, "columnIndex")
                          .addParameter(Period.class, "value")
                          .addException(SQLException.class)
                          .beginControlFlow("if (value != null)")
                          .addStatement("statement.setInt(columnIndex, value.getDays())")
                          .nextControlFlow("else")
                          .addStatement("statement.setNull(columnIndex, $T.INTEGER)", Types.class)
                          .endControlFlow();
        classBuilder.addMethod(setPeriodMethodBuilder.build());

        MethodSpec.Builder setDurationMethodBuilder =
                MethodSpec.methodBuilder("setDuration")
                          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                          .returns(void.class)
                          .addParameter(PreparedStatement.class, "statement")
                          .addParameter(int.class, "columnIndex")
                          .addParameter(Duration.class, "value")
                          .addException(SQLException.class)
                          .beginControlFlow("if (value != null)")
                          .addStatement("statement.setLong(columnIndex, value.getSeconds())")
                          .nextControlFlow("else")
                          .addStatement("statement.setNull(columnIndex, $T.BIGINT)", Types.class)
                          .endControlFlow();
        classBuilder.addMethod(setDurationMethodBuilder.build());

        MethodSpec.Builder setUuidMethodBuilder =
                MethodSpec.methodBuilder("setUuid")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(UUID.class, "value")
                        .addException(SQLException.class)
                        .beginControlFlow("if (value != null)")
                        .addStatement("statement.setString(columnIndex, value.toString())")
                        .nextControlFlow("else")
                        .addStatement("statement.setNull(columnIndex, $T.VARCHAR)", Types.class)
                        .endControlFlow();
        classBuilder.addMethod(setUuidMethodBuilder.build());

        MethodSpec.Builder setEnumMethodBuilder =
                MethodSpec.methodBuilder("setEnum")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addTypeVariable(TypeVariableName.get("T", ParameterizedTypeName.get(ClassName.get(Enum.class), TypeVariableName.get("T"))))
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addParameter(TypeVariableName.get("T"), "value")
                        .addException(SQLException.class)
                        .beginControlFlow("if (value != null)")
                        .addStatement("statement.setString(columnIndex, value.name())")
                        .nextControlFlow("else")
                        .addStatement("statement.setNull(columnIndex, $T.VARCHAR)", Types.class)
                        .endControlFlow();
        classBuilder.addMethod(setEnumMethodBuilder.build());

        MethodSpec.Builder setNullMethodBuilder =
                MethodSpec.methodBuilder("setNull")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(PreparedStatement.class, "statement")
                        .addParameter(int.class, "columnIndex")
                        .addException(SQLException.class)
                        .addStatement("statement.setObject(columnIndex, null)"); // TODO: switch to using PreparedStatement.setNull(..)
        classBuilder.addMethod(setNullMethodBuilder.build());

        // TODO: move to a separate helper method
        JavaFile javaFile = JavaFile.builder(AttributeMappers.packageName(), classBuilder.build())
                .skipJavaLangImports(true)
                .indent("\t")
                .build();
        try {
            javaFile.writeTo(filer);
        }
        catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }
}
