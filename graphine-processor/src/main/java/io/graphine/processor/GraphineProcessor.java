package io.graphine.processor;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import io.graphine.core.util.WildcardParameterRepeater;
import io.graphine.processor.code.collector.OriginatingElementDependencyCollector;
import io.graphine.processor.code.generator.entity.AttributeMappingGenerator;
import io.graphine.processor.code.generator.repository.RepositoryImplementationGenerator;
import io.graphine.processor.code.renderer.mapping.ResultSetMappingRenderer;
import io.graphine.processor.code.renderer.mapping.StatementMappingRenderer;
import io.graphine.processor.metadata.collector.EntityMetadataCollector;
import io.graphine.processor.metadata.collector.RepositoryMetadataCollector;
import io.graphine.processor.metadata.factory.entity.AttributeMetadataFactory;
import io.graphine.processor.metadata.factory.entity.EmbeddableEntityMetadataFactory;
import io.graphine.processor.metadata.factory.entity.EntityMetadataFactory;
import io.graphine.processor.metadata.factory.repository.MethodMetadataFactory;
import io.graphine.processor.metadata.factory.repository.RepositoryMetadataFactory;
import io.graphine.processor.metadata.model.repository.RepositoryMetadata;
import io.graphine.processor.metadata.parser.RepositoryMethodNameParser;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;
import io.graphine.processor.metadata.registry.RepositoryMetadataRegistry;
import io.graphine.processor.metadata.validator.entity.EmbeddableEntityElementValidator;
import io.graphine.processor.metadata.validator.entity.EntityElementValidator;
import io.graphine.processor.metadata.validator.repository.RepositoryElementValidator;
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
        entityMetadataRegistry.getEntities()
                              .forEach(entity -> messager.printMessage(Kind.NOTE, "Found entity: " + entity));

        // Step 2. Collecting repository metadata
        RepositoryMetadataRegistry repositoryMetadataRegistry = collectRepositoryMetadata(roundEnv);

        // Step 3. Validating repository metadata
        if (!validateRepositoryMetadata(repositoryMetadataRegistry.getRepositories(), entityMetadataRegistry)) {
            return ANNOTATIONS_CLAIMED;
        }

        repositoryMetadataRegistry.getRepositories()
                                  .forEach(repository -> messager.printMessage(Kind.NOTE, "Found repository: " + repository));

        // Step 4. Generating repository native queries
        RepositoryNativeQueryRegistryStorage nativeQueryRegistryStorage =
                generateNativeQueries(repositoryMetadataRegistry.getRepositories(), entityMetadataRegistry);

        nativeQueryRegistryStorage.getRegistries()
                                  .stream()
                                  .flatMap(registry -> registry.getQueries().stream())
                                  .forEach(query -> messager.printMessage(Kind.NOTE,
                                                                          "Generated query: " + query.getValue()));

        // Step 5. Generating infrastructure code
        generateInfrastructureCode();

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
        EntityElementValidator entityElementValidator =
                new EntityElementValidator();
        EmbeddableEntityElementValidator embeddableEntityElementValidator =
                new EmbeddableEntityElementValidator();
        EmbeddableEntityMetadataFactory embeddableEntityMetadataFactory =
                new EmbeddableEntityMetadataFactory(attributeMetadataFactory);
        EntityMetadataCollector entityMetadataCollector =
                new EntityMetadataCollector(entityElementValidator, entityMetadataFactory,
                                            embeddableEntityElementValidator, embeddableEntityMetadataFactory);
        return entityMetadataCollector.collect(roundEnv);
    }

    private RepositoryMetadataRegistry collectRepositoryMetadata(RoundEnvironment roundEnv) {
        RepositoryMethodNameParser repositoryMethodNameParser =
                new RepositoryMethodNameParser();
        MethodMetadataFactory methodMetadataFactory =
                new MethodMetadataFactory(repositoryMethodNameParser);
        RepositoryMetadataFactory repositoryMetadataFactory =
                new RepositoryMetadataFactory(methodMetadataFactory);
        RepositoryElementValidator repositoryElementValidator =
                new RepositoryElementValidator();
        RepositoryMetadataCollector repositoryMetadataCollector =
                new RepositoryMetadataCollector(repositoryElementValidator, repositoryMetadataFactory);
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

    private void generateInfrastructureCode() {
        AttributeMappingGenerator attributeMappingGenerator = new AttributeMappingGenerator();
        attributeMappingGenerator.generate();
    }

    private void generateRepositoryImplementation(List<RepositoryNativeQueryRegistry> nativeQueryRegistries,
                                                  EntityMetadataRegistry entityMetadataRegistry) {
        OriginatingElementDependencyCollector originatingElementDependencyCollector =
                new OriginatingElementDependencyCollector(entityMetadataRegistry);
        StatementMappingRenderer statementMappingRenderer = new StatementMappingRenderer();
        ResultSetMappingRenderer resultSetMappingRenderer = new ResultSetMappingRenderer();

        for (RepositoryNativeQueryRegistry nativeQueryRegistry : nativeQueryRegistries) {
            RepositoryMetadata repository = nativeQueryRegistry.getRepository();

            RepositoryImplementationGenerator repositoryImplementationGenerator =
                    new RepositoryImplementationGenerator(originatingElementDependencyCollector,
                                                          nativeQueryRegistry,
                                                          entityMetadataRegistry,
                                                          statementMappingRenderer,
                                                          resultSetMappingRenderer);
            TypeSpec typeSpec = repositoryImplementationGenerator.generate(repository);

            // TODO: move to a separate helper method
            JavaFile javaFile = JavaFile.builder(repository.getPackageName(), typeSpec)
                                        .skipJavaLangImports(true)
                                        .indent("\t")
                                        // TODO: imported even if not used
                                        .addStaticImport(WildcardParameterRepeater.class, "repeat", "repeatFor")
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
