package io.graphine.processor.code.generator.infrastructure;

import com.squareup.javapoet.*;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import java.util.StringJoiner;

import static io.graphine.processor.support.EnvironmentContext.javaFiler;

/**
 * @author Oleg Marchenko
 */
public class WildcardRepeaterGenerator {
    public static final ClassName WildcardRepeater = ClassName.get("io.graphine.core", "WildcardRepeater");

    public void generate() {
        TypeSpec.Builder classBuilder = TypeSpec
                .classBuilder(WildcardRepeater)
                .addAnnotation(AnnotationSpec.builder(Generated.class)
                                             .addMember("value", "$S", "io.graphine.processor.GraphineProcessor")
                                             .build())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                                     .addModifiers(Modifier.PRIVATE)
                                     .build());
        classBuilder
                .addField(FieldSpec.builder(String.class, "PARAMETER_SEPARATOR", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                                   .initializer("$S", ", ")
                                   .build());
        classBuilder
                .addField(FieldSpec.builder(String.class, "PARAMETER_WILDCARD", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                                   .initializer("$S", "?")
                                   .build());
        classBuilder
                .addMethod(MethodSpec.methodBuilder("repeat")
                                     .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                     .returns(String.class)
                                     .addParameter(int.class, "count")
                                     .addStatement("$T joiner = new $T(PARAMETER_SEPARATOR)",
                                                   StringJoiner.class, StringJoiner.class)
                                     .beginControlFlow("for (int i = 1; i <= count; i++)")
                                     .addStatement("joiner.add(PARAMETER_WILDCARD)")
                                     .endControlFlow()
                                     .addStatement("return joiner.toString()")
                                     .build());
        classBuilder
                .addMethod(MethodSpec.methodBuilder("repeatFor")
                                     .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                     .returns(String.class)
                                     .addParameter(ParameterizedTypeName.get(ClassName.get(Iterable.class),
                                                                             WildcardTypeName.subtypeOf(Object.class)),
                                                   "elements")
                                     .addStatement("$T joiner = new $T(PARAMETER_SEPARATOR)",
                                                   StringJoiner.class, StringJoiner.class)
                                     .beginControlFlow("for (Object element : elements)")
                                     .addStatement("joiner.add(PARAMETER_WILDCARD)")
                                     .endControlFlow()
                                     .addStatement("return joiner.toString()")
                                     .build());

        javaFiler.create(WildcardRepeater.packageName(), classBuilder.build());
    }
}
