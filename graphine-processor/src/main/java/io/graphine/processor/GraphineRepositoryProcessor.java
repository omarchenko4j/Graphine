package io.graphine.processor;

import io.graphine.processor.metadata.collector.EntityMetadataCollector;
import io.graphine.processor.metadata.factory.entity.AttributeMetadataFactory;
import io.graphine.processor.metadata.factory.entity.EntityMetadataFactory;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;
import io.graphine.processor.metadata.validator.EntityMetadataValidator;
import io.graphine.processor.support.EnvironmentContext;

import javax.annotation.processing.*;
import javax.lang.model.element.TypeElement;
import java.util.Set;

import static javax.lang.model.SourceVersion.RELEASE_11;

/**
 * @author Oleg Marchenko
 */
@SupportedAnnotationTypes("io.graphine.core.annotation.Repository")
@SupportedSourceVersion(RELEASE_11)
public class GraphineRepositoryProcessor extends AbstractProcessor {
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        EnvironmentContext.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }
}
