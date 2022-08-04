package io.graphine.processor;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import io.graphine.processor.code.collector.OriginatingElementDependencyCollector;
import io.graphine.processor.code.generator.infrastructure.AttributeMappingGenerator;
import io.graphine.processor.code.generator.infrastructure.GeneralExceptionGenerator;
import io.graphine.processor.code.generator.infrastructure.WildcardRepeaterGenerator;
import io.graphine.processor.code.generator.repository.RepositoryImplementationGenerator;
import io.graphine.processor.code.renderer.mapping.ResultSetMappingRenderer;
import io.graphine.processor.code.renderer.mapping.StatementMappingRenderer;
import io.graphine.processor.metadata.collector.AttributeMapperMetadataCollector;
import io.graphine.processor.metadata.collector.EntityMetadataCollector;
import io.graphine.processor.metadata.collector.RepositoryMetadataCollector;
import io.graphine.processor.metadata.factory.entity.AttributeMapperMetadataFactory;
import io.graphine.processor.metadata.factory.entity.AttributeMetadataFactory;
import io.graphine.processor.metadata.factory.entity.EmbeddableEntityMetadataFactory;
import io.graphine.processor.metadata.factory.entity.EntityMetadataFactory;
import io.graphine.processor.metadata.factory.repository.MethodMetadataFactory;
import io.graphine.processor.metadata.factory.repository.RepositoryMetadataFactory;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.RepositoryMetadata;
import io.graphine.processor.metadata.parser.RepositoryMethodNameParser;
import io.graphine.processor.metadata.registry.AttributeMapperMetadataRegistry;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;
import io.graphine.processor.metadata.registry.RepositoryMetadataRegistry;
import io.graphine.processor.metadata.validator.entity.AttributeMapperElementValidator;
import io.graphine.processor.metadata.validator.entity.EmbeddableEntityElementValidator;
import io.graphine.processor.metadata.validator.entity.EntityElementValidator;
import io.graphine.processor.metadata.validator.entity.EntityMetadataValidator;
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
import static io.graphine.processor.support.EnvironmentContext.logger;
import static javax.lang.model.SourceVersion.RELEASE_11;

/**
 * @author Oleg Marchenko
 */
@SupportedAnnotationTypes("io.graphine.annotation.Repository")
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

        // Step 0. Collecting attribute mapper metadata
        AttributeMapperMetadataRegistry attributeMapperMetadataRegistry = collectAttributeMapperMetadata(roundEnv);

        // Step 1. Collecting entity metadata
        EntityMetadataRegistry entityMetadataRegistry = collectEntityMetadata(roundEnv);

        // Step 2. Collecting repository metadata
        RepositoryMetadataRegistry repositoryMetadataRegistry = collectRepositoryMetadata(roundEnv);

        // Step 3. Validating entity and repository metadata
        boolean allEntitiesAreValid =
                validateEntityMetadata(entityMetadataRegistry.getEntities(), attributeMapperMetadataRegistry);
        boolean allRepositoriesAreValid =
                validateRepositoryMetadata(repositoryMetadataRegistry.getRepositories(), entityMetadataRegistry);
        // If at least one entity or repository has validation errors then query and code generation is aborted.
        if (!allEntitiesAreValid || !allRepositoriesAreValid) {
            return ANNOTATIONS_CLAIMED;
        }

        // Step 4. Generating repository native queries
        RepositoryNativeQueryRegistryStorage nativeQueryRegistryStorage =
                generateNativeQueries(repositoryMetadataRegistry.getRepositories(), entityMetadataRegistry);

        // TODO: Hide logging behind flag option (like graphine.debug=true/false)
        attributeMapperMetadataRegistry.getAttributeMappers()
                                       .forEach(attributeMapper -> logger.info("Found attribute mapper: " + attributeMapper));
        entityMetadataRegistry.getEntities()
                              .forEach(entity -> logger.info("Found entity: " + entity));
        repositoryMetadataRegistry.getRepositories()
                                  .forEach(repository -> logger.info("Found repository: " + repository));
        nativeQueryRegistryStorage.getRegistries()
                                  .stream()
                                  .map(RepositoryNativeQueryRegistry::getQueries)
                                  .flatMap(Collection::stream)
                                  .forEach(query -> logger.info("Generated query: " + query.getValue()));

        // Step 5. Generating infrastructure code
        generateInfrastructureCode();

        // Step 6. Generating repository implementations
        generateRepositoryImplementation(nativeQueryRegistryStorage.getRegistries(),
                                         entityMetadataRegistry,
                                         attributeMapperMetadataRegistry);

        return ANNOTATIONS_CLAIMED;
    }

    private AttributeMapperMetadataRegistry collectAttributeMapperMetadata(RoundEnvironment roundEnv) {
        AttributeMapperElementValidator attributeMapperElementValidator =
                new AttributeMapperElementValidator();
        AttributeMapperMetadataFactory attributeMapperMetadataFactory =
                new AttributeMapperMetadataFactory();
        AttributeMapperMetadataCollector attributeMapperMetadataCollector =
                new AttributeMapperMetadataCollector(attributeMapperElementValidator, attributeMapperMetadataFactory);
        return attributeMapperMetadataCollector.collect(roundEnv);
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

    private boolean validateEntityMetadata(Collection<EntityMetadata> entities,
                                           AttributeMapperMetadataRegistry attributeMapperMetadataRegistry) {
        EntityMetadataValidator entityMetadataValidator =
                new EntityMetadataValidator(attributeMapperMetadataRegistry);
        return entityMetadataValidator.validate(entities);
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

        WildcardRepeaterGenerator wildcardRepeaterGenerator = new WildcardRepeaterGenerator();
        wildcardRepeaterGenerator.generate();

        GeneralExceptionGenerator generalExceptionGenerator = new GeneralExceptionGenerator();
        generalExceptionGenerator.generate();
    }

    private void generateRepositoryImplementation(List<RepositoryNativeQueryRegistry> nativeQueryRegistries,
                                                  EntityMetadataRegistry entityMetadataRegistry,
                                                  AttributeMapperMetadataRegistry attributeMapperMetadataRegistry) {
        OriginatingElementDependencyCollector originatingElementDependencyCollector =
                new OriginatingElementDependencyCollector(entityMetadataRegistry, attributeMapperMetadataRegistry);
        StatementMappingRenderer statementMappingRenderer = new StatementMappingRenderer();
        ResultSetMappingRenderer resultSetMappingRenderer = new ResultSetMappingRenderer();

        for (RepositoryNativeQueryRegistry nativeQueryRegistry : nativeQueryRegistries) {
            RepositoryMetadata repository = nativeQueryRegistry.getRepository();

            RepositoryImplementationGenerator repositoryImplementationGenerator =
                    new RepositoryImplementationGenerator(originatingElementDependencyCollector,
                                                          nativeQueryRegistry,
                                                          entityMetadataRegistry,
                                                          attributeMapperMetadataRegistry,
                                                          statementMappingRenderer,
                                                          resultSetMappingRenderer);
            TypeSpec typeSpec = repositoryImplementationGenerator.generate(repository);

            // TODO: move to a separate helper method
            JavaFile javaFile = JavaFile.builder(repository.getPackageName(), typeSpec)
                                        .skipJavaLangImports(true)
                                        .indent("\t")
                                        .build();
            try {
                javaFile.writeTo(filer);
            }
            catch (IOException e) {
                logger.error(e.getMessage(), repository.getNativeElement());
            }
        }
    }
}
