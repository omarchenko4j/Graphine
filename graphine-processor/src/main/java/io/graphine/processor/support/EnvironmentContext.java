package io.graphine.processor.support;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Map;

/**
 * @author Oleg Marchenko
 */
public final class EnvironmentContext {
    public static Elements elementUtils;
    public static Types typeUtils;
    public static Messager messager;
    public static Filer filer;
    public static Map<String, String> options;

    public static void init(ProcessingEnvironment environment) {
        elementUtils = environment.getElementUtils();
        typeUtils = environment.getTypeUtils();
        messager = environment.getMessager();
        filer = environment.getFiler();
        options = environment.getOptions();
    }

    private EnvironmentContext() {
    }
}
