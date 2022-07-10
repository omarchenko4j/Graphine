package io.graphine.processor.code.generator.infrastructure;

import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.StringJoiner;

import static io.graphine.processor.support.EnvironmentContext.filer;
import static io.graphine.processor.support.EnvironmentContext.messager;

/**
 * @author Oleg Marchenko
 */
public class WildcardRepeaterGenerator {
    public static final ClassName WildcardRepeater = ClassName.get("io.graphine.core", "WildcardRepeater");

    public void generate() {
        TypeSpec.Builder classBuilder = TypeSpec
                .classBuilder(WildcardRepeater)
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

        // TODO: move to a separate helper method
        JavaFile javaFile = JavaFile.builder(WildcardRepeater.packageName(), classBuilder.build())
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
