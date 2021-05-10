package io.graphine.processor;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import io.graphine.core.util.UnnamedParameterRepeater;
import io.graphine.processor.code.collector.OriginatingElementDependencyCollector;
import io.graphine.processor.code.generator.repository.RepositoryImplementationGenerator;
import io.graphine.processor.metadata.collector.EntityMetadataCollector;
import io.graphine.processor.metadata.collector.RepositoryMetadataCollector;
import io.graphine.processor.metadata.factory.entity.AttributeMetadataFactory;
import io.graphine.processor.metadata.factory.entity.EntityMetadataFactory;
import io.graphine.processor.metadata.factory.repository.MethodMetadataFactory;
import io.graphine.processor.metadata.factory.repository.RepositoryMetadataFactory;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.RepositoryMetadata;
import io.graphine.processor.metadata.parser.RepositoryMethodNameParser;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;
import io.graphine.processor.metadata.registry.RepositoryMetadataRegistry;
import io.graphine.processor.metadata.validator.entity.EntityMetadataValidator;
import io.graphine.processor.metadata.validator.repository.RepositoryMetadataValidator;
import io.graphine.processor.query.generator.RepositoryNativeQueryGenerator;
import io.graphine.processor.query.registry.RepositoryNativeQueryRegistry;
import io.graphine.processor.query.registry.RepositoryNativeQueryRegistryStorage;
import io.graphine.processor.support.EnvironmentContext;
import io.graphine.processor.support.naming.pipeline.ColumnNamingPipeline;
import io.graphine.processor.support.naming.pipeline.TableNamingPipeline;

import javax.annotation.processing.*;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
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
public class GraphineProcessor extends AbstractProcessor {
    private static final boolean ANNOTATIONS_CLAIMED = true;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        EnvironmentContext.init(processingEnv);
    }

    @Override
    public Set<String> getSupportedOptions() {
        return GraphineOptions.names();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver() || annotations.isEmpty()) {
            return ANNOTATIONS_CLAIMED;
        }

        // Step 1. Collecting entity metadata
        EntityMetadataRegistry entityMetadataRegistry = collectEntityMetadata(roundEnv);

        // Step 2. Validating entity metadata
        if (!validateEntityMetadata(entityMetadataRegistry.getEntities())) {
            return ANNOTATIONS_CLAIMED;
        }

        entityMetadataRegistry.getEntities()
                              .forEach(entity -> messager.printMessage(Kind.NOTE, "Found entity: " + entity));

        // Step 3. Collecting repository metadata
        RepositoryMetadataRegistry repositoryMetadataRegistry = collectRepositoryMetadata(roundEnv);

        // Step 4. Validating repository metadata
        if (!validateRepositoryMetadata(repositoryMetadataRegistry.getRepositories(), entityMetadataRegistry)) {
            return ANNOTATIONS_CLAIMED;
        }

        repositoryMetadataRegistry.getRepositories()
                                  .forEach(repository -> messager.printMessage(Kind.NOTE, "Found repository: " + repository));

        // Step 5. Generating repository native queries
        RepositoryNativeQueryRegistryStorage nativeQueryRegistryStorage =
                generateNativeQueries(repositoryMetadataRegistry.getRepositories(), entityMetadataRegistry);

        nativeQueryRegistryStorage.getRegistries()
                                  .stream()
                                  .flatMap(registry -> registry.getQueries().stream())
                                  .forEach(query -> messager.printMessage(Kind.NOTE,
                                                                          "Generated query: " + query.getValue()));

        // Step 6. Generating repository implementations
        generateRepositoryImplementation(nativeQueryRegistryStorage.getRegistries(), entityMetadataRegistry);

        return ANNOTATIONS_CLAIMED;
    }

    private EntityMetadataRegistry collectEntityMetadata(RoundEnvironment roundEnv) {
        ColumnNamingPipeline columnNamingPipeline =
                new ColumnNamingPipeline();
        AttributeMetadataFactory attributeMetadataFactory =
                new AttributeMetadataFactory(columnNamingPipeline);
        TableNamingPipeline tableNamingPipeline =
                new TableNamingPipeline();
        EntityMetadataFactory entityMetadataFactory =
                new EntityMetadataFactory(tableNamingPipeline, attributeMetadataFactory);
        EntityMetadataCollector entityMetadataCollector =
                new EntityMetadataCollector(entityMetadataFactory);
        return entityMetadataCollector.collect(roundEnv);
    }

    private boolean validateEntityMetadata(Collection<EntityMetadata> entities) {
        EntityMetadataValidator entityMetadataValidator = new EntityMetadataValidator();
        return entityMetadataValidator.validate(entities);
    }

    private RepositoryMetadataRegistry collectRepositoryMetadata(RoundEnvironment roundEnv) {
        RepositoryMethodNameParser repositoryMethodNameParser =
                new RepositoryMethodNameParser();
        MethodMetadataFactory methodMetadataFactory =
                new MethodMetadataFactory(repositoryMethodNameParser);
        RepositoryMetadataFactory repositoryMetadataFactory =
                new RepositoryMetadataFactory(methodMetadataFactory);
        RepositoryMetadataCollector repositoryMetadataCollector =
                new RepositoryMetadataCollector(repositoryMetadataFactory);
        return repositoryMetadataCollector.collect(roundEnv);
    }

    private boolean validateRepositoryMetadata(Collection<RepositoryMetadata> repositories,
                                               EntityMetadataRegistry entityMetadataRegistry) {
        RepositoryMetadataValidator repositoryMetadataValidator =
                new RepositoryMetadataValidator(entityMetadataRegistry);
        return repositoryMetadataValidator.validate(repositories);
    }

    private RepositoryNativeQueryRegistryStorage generateNativeQueries(Collection<RepositoryMetadata> repositories,
                                                                       EntityMetadataRegistry entityMetadataRegistry) {
        RepositoryNativeQueryGenerator repositoryNativeQueryGenerator =
                new RepositoryNativeQueryGenerator(entityMetadataRegistry);
        return repositoryNativeQueryGenerator.generate(repositories);
    }

    private void generateRepositoryImplementation(List<RepositoryNativeQueryRegistry> nativeQueryRegistries,
                                                  EntityMetadataRegistry entityMetadataRegistry) {
        OriginatingElementDependencyCollector originatingElementDependencyCollector =
                new OriginatingElementDependencyCollector(entityMetadataRegistry);

        for (RepositoryNativeQueryRegistry nativeQueryRegistry : nativeQueryRegistries) {
            RepositoryMetadata repository = nativeQueryRegistry.getRepository();

            RepositoryImplementationGenerator repositoryImplementationGenerator =
                    new RepositoryImplementationGenerator(originatingElementDependencyCollector, nativeQueryRegistry);
            TypeSpec typeSpec = repositoryImplementationGenerator.generate(repository);

            JavaFile javaFile = JavaFile.builder(repository.getPackageName(), typeSpec)
                                        .skipJavaLangImports(true)
                                        .indent("\t")
                                        // TODO: imported even if not used
                                        .addStaticImport(UnnamedParameterRepeater.class, "repeat", "repeatFor")
                                        .build();
            try {
                javaFile.writeTo(filer);
            }
            catch (IOException e) {
                messager.printMessage(Kind.ERROR, e.getMessage(), repository.getNativeElement());
            }
        }
    }
}
