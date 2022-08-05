package io.graphine.processor.support;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Map;

import static io.graphine.processor.GraphineOptions.DEBUG;
import static javax.tools.Diagnostic.Kind;

/**
 * @author Oleg Marchenko
 */
public final class EnvironmentContext {
    public static Elements elementUtils;
    public static Types typeUtils;
    public static Logger logger;
    public static JavaFiler javaFiler;
    public static Map<String, String> options;

    public static void init(ProcessingEnvironment environment) {
        elementUtils = environment.getElementUtils();
        typeUtils = environment.getTypeUtils();
        logger = new Logger(environment.getMessager());
        javaFiler = new JavaFiler(environment.getFiler());
        options = environment.getOptions();
    }

    public static final class Logger {
        private final Messager messager;

        private Logger(Messager messager) {
            this.messager = messager;
        }

        public void debug(String message) {
            boolean debug = DEBUG.value(Boolean::parseBoolean);
            if (debug) {
                messager.printMessage(Kind.NOTE, message);
            }
        }

        public void warn(String message, Element element) {
            messager.printMessage(Kind.WARNING, message, element);
        }

        public void mandatoryWarn(String message, Element element) {
            messager.printMessage(Kind.MANDATORY_WARNING, message, element);
        }

        public void error(String message) {
            messager.printMessage(Kind.ERROR, message);
        }

        public void error(String message, Element element) {
            messager.printMessage(Kind.ERROR, message, element);
        }

        public void error(String message, Element element, AnnotationMirror annotation, AnnotationValue annotationValue) {
            messager.printMessage(Kind.ERROR, message, element, annotation, annotationValue);
        }
    }

    public static final class JavaFiler {
        private final Filer filer;

        private JavaFiler(Filer filer) {
            this.filer = filer;
        }

        public void create(String packageName, TypeSpec typeSpec) {
            JavaFile javaFile =
                    JavaFile.builder(packageName, typeSpec)
                            .skipJavaLangImports(true)
                            .indent("\t")
                            .build();
            try {
                javaFile.writeTo(filer);
            }
            catch (Exception e) {
                logger.error("An error occurred while creating Java file. Details: " + e.getMessage());
            }
        }
    }

    private EnvironmentContext() {
    }
}
