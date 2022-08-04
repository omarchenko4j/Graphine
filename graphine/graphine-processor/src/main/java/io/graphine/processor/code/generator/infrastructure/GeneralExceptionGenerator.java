package io.graphine.processor.code.generator.infrastructure;

import com.squareup.javapoet.*;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import java.io.IOException;

import static io.graphine.processor.support.EnvironmentContext.filer;
import static io.graphine.processor.support.EnvironmentContext.logger;

/**
 * TODO: quick sketch - refactoring candidate
 *
 * @author Oleg Marchenko
 */
public class GeneralExceptionGenerator {
    public static final ClassName GraphineException = ClassName.get("io.graphine.core", "GraphineException");
    public static final ClassName NonUniqueResultException = ClassName.get("io.graphine.core", "NonUniqueResultException");

    public void generate() {
        // TODO: move to a separate helper method
        JavaFile javaFile = JavaFile.builder(GraphineException.packageName(), generateGraphineException())
                                    .skipJavaLangImports(true)
                                    .indent("\t")
                                    .build();
        try {
            javaFile.writeTo(filer);
        }
        catch (IOException e) {
            logger.error(e.getMessage());
        }
        // TODO: move to a separate helper method
        javaFile = JavaFile.builder(NonUniqueResultException.packageName(), generateNonUniqueResultException())
                                    .skipJavaLangImports(true)
                                    .indent("\t")
                                    .build();
        try {
            javaFile.writeTo(filer);
        }
        catch (IOException e) {
            logger.error(e.getMessage());
        }
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
