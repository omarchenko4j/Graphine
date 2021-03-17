package io.graphine.processor;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import io.graphine.core.util.UnnamedParameterUnwrapper;
import io.graphine.processor.code.generator.repository.RepositoryImplementationGenerator;
import io.graphine.processor.metadata.collector.EntityMetadataCollector;
import io.graphine.processor.metadata.collector.RepositoryMetadataCollector;
import io.graphine.processor.metadata.factory.entity.AttributeMetadataFactory;
import io.graphine.processor.metadata.factory.entity.EntityMetadataFactory;
import io.graphine.processor.metadata.factory.repository.MethodMetadataFactory;
import io.graphine.processor.metadata.factory.repository.RepositoryMetadataFactory;
import io.graphine.processor.metadata.model.repository.RepositoryMetadata;
import io.graphine.processor.metadata.parser.RepositoryMethodNameParser;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;
import io.graphine.processor.metadata.registry.RepositoryMetadataRegistry;
import io.graphine.processor.metadata.validator.entity.EntityMetadataValidator;
import io.graphine.processor.metadata.validator.repository.RepositoryMetadataValidator;
import io.graphine.processor.query.generator.RepositoryNativeQueryGenerator;
import io.graphine.processor.query.registry.RepositoryNativeQueryRegistry;
import io.graphine.processor.support.EnvironmentContext;

import javax.annotation.processing.*;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Set;

import static io.graphine.processor.support.EnvironmentContext.filer;
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

        // Step 1. Collecting and validating entity metadata
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

        // Step 2. Collecting and validating repository metadata
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
                                  .forEach(repository -> messager.printMessage(Kind.NOTE,
                                                                               "Found repository: " + repository));

        // Step 3. Generating native queries and repository implementations
        RepositoryNativeQueryGenerator repositoryNativeQueryGenerator = new RepositoryNativeQueryGenerator();
        for (RepositoryMetadata repository : repositoryMetadataRegistry.getRepositories()) {
            RepositoryNativeQueryRegistry repositoryNativeQueryRegistry =
                    repositoryNativeQueryGenerator.generate(repository);
            repositoryNativeQueryRegistry.getQueries()
                                         .forEach(query -> messager.printMessage(Kind.NOTE,
                                                                                 "Generated query: " + query.getValue()));

            // TODO: extract this into a separate standalone step
            RepositoryImplementationGenerator repositoryImplementationGenerator =
                    new RepositoryImplementationGenerator(repositoryNativeQueryRegistry);
            TypeSpec typeSpec = repositoryImplementationGenerator.generate(repository);

            JavaFile javaFile = JavaFile.builder(repository.getPackageName(), typeSpec)
                                        .skipJavaLangImports(true)
                                        .indent("\t")
                                        .addStaticImport(UnnamedParameterUnwrapper.class, "unwrapFor")
                                        .build();
            try {
                javaFile.writeTo(filer);
            }
            catch (IOException e) {
                messager.printMessage(Kind.ERROR, e.getMessage(), repository.getNativeElement());
            }
        }

        return true;
    }
}
