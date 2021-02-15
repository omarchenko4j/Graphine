package io.graphine.processor;

import io.graphine.processor.metadata.collector.EntityMetadataCollector;
import io.graphine.processor.metadata.collector.RepositoryMetadataCollector;
import io.graphine.processor.metadata.factory.entity.AttributeMetadataFactory;
import io.graphine.processor.metadata.factory.entity.EntityMetadataFactory;
import io.graphine.processor.metadata.factory.repository.MethodMetadataFactory;
import io.graphine.processor.metadata.factory.repository.RepositoryMetadataFactory;
import io.graphine.processor.metadata.parser.RepositoryMethodNameParser;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;
import io.graphine.processor.metadata.registry.RepositoryMetadataRegistry;
import io.graphine.processor.metadata.validator.entity.EntityMetadataValidator;
import io.graphine.processor.metadata.validator.repository.RepositoryMetadataValidator;
import io.graphine.processor.support.EnvironmentContext;

import javax.annotation.processing.*;
import javax.lang.model.element.TypeElement;
import java.util.Set;

import static io.graphine.processor.support.EnvironmentContext.messager;
import static javax.lang.model.SourceVersion.RELEASE_11;
import static javax.tools.Diagnostic.Kind;

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
        if (roundEnv.processingOver() || annotations.isEmpty()) return false;

        // Step 1
        AttributeMetadataFactory attributeMetadataFactory = new AttributeMetadataFactory();
        EntityMetadataFactory entityMetadataFactory = new EntityMetadataFactory(attributeMetadataFactory);
        EntityMetadataCollector entityMetadataCollector = new EntityMetadataCollector(entityMetadataFactory);
        EntityMetadataRegistry entityMetadataRegistry = entityMetadataCollector.collect(roundEnv);

        EntityMetadataValidator entityMetadataValidator = new EntityMetadataValidator();
        if (!entityMetadataValidator.validate(entityMetadataRegistry.getEntities())) {
            return true;
        }
        entityMetadataRegistry.getEntities()
                              .forEach(entity -> messager.printMessage(Kind.NOTE, "Found entity: " + entity));

        // Step 2
        RepositoryMethodNameParser repositoryMethodNameParser = new RepositoryMethodNameParser();
        MethodMetadataFactory methodMetadataFactory = new MethodMetadataFactory(repositoryMethodNameParser);
        RepositoryMetadataFactory repositoryMetadataFactory =
                new RepositoryMetadataFactory(entityMetadataRegistry, methodMetadataFactory);
        RepositoryMetadataCollector repositoryMetadataCollector =
                new RepositoryMetadataCollector(repositoryMetadataFactory);
        RepositoryMetadataRegistry repositoryMetadataRegistry = repositoryMetadataCollector.collect(roundEnv);

        RepositoryMetadataValidator repositoryMetadataValidator = new RepositoryMetadataValidator();
        if (!repositoryMetadataValidator.validate(repositoryMetadataRegistry.getRepositories())) {
            return true;
        }
        repositoryMetadataRegistry.getRepositories()
                                  .forEach(repository -> messager.printMessage(Kind.NOTE, "Found repository: " + repository));

        return true;
    }
}
