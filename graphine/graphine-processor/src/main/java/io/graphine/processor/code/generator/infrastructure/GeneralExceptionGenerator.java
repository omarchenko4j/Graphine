package io.graphine.processor.code.generator.infrastructure;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;

import static io.graphine.processor.support.EnvironmentContext.javaFiler;

/**
 * TODO: quick sketch - refactoring candidate
 *
 * @author Oleg Marchenko
 */
public class GeneralExceptionGenerator {
    public static final ClassName GraphineException = ClassName.get("io.graphine.core", "GraphineException");
    public static final ClassName NonUniqueResultException = ClassName.get("io.graphine.core", "NonUniqueResultException");

    public void generate() {
        javaFiler.create(GraphineException.packageName(), generateGraphineException());
        javaFiler.create(NonUniqueResultException.packageName(), generateNonUniqueResultException());
    }

    private TypeSpec generateGraphineException() {
        return TypeSpec
                .classBuilder(GraphineException)
                .addAnnotation(AnnotationSpec.builder(Generated.class)
                                             .addMember("value", "$S", "io.graphine.processor.GraphineProcessor")
                                             .build())
                .addModifiers(Modifier.PUBLIC)
                .superclass(RuntimeException.class)
                .addMethod(MethodSpec.constructorBuilder()
                                     .addModifiers(Modifier.PUBLIC)
                                     .addParameter(String.class, "message")
                                     .addStatement("super(message)")
                                     .build())
                .addMethod(MethodSpec.constructorBuilder()
                                     .addModifiers(Modifier.PUBLIC)
                                     .addParameter(Exception.class, "e")
                                     .addStatement("super(e)")
                                     .build())
                .build();
    }

    private TypeSpec generateNonUniqueResultException() {
        return TypeSpec
                .classBuilder(NonUniqueResultException)
                .addAnnotation(AnnotationSpec.builder(Generated.class)
                                             .addMember("value", "$S", "io.graphine.processor.GraphineProcessor")
                                             .build())
                .superclass(GraphineException)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                                     .addModifiers(Modifier.PUBLIC)
                                     .addStatement("super($S)", "Query returned a non-unique result")
                                     .build())
                .build();
    }
}
