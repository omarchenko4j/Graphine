package io.graphine.processor.code.generator.repository;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.graphine.processor.code.collector.OriginatingElementDependencyCollector;
import io.graphine.processor.code.generator.repository.method.*;
import io.graphine.processor.code.renderer.mapping.ResultSetMappingRenderer;
import io.graphine.processor.code.renderer.mapping.StatementMappingRenderer;
import io.graphine.processor.metadata.model.repository.RepositoryMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.registry.AttributeMapperMetadataRegistry;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;
import io.graphine.processor.query.model.NativeQuery;
import io.graphine.processor.query.registry.RepositoryNativeQueryRegistry;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Element;
import javax.sql.DataSource;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment.MethodType;
import static java.util.Objects.isNull;
import static javax.lang.model.element.Modifier.*;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryImplementationGenerator {
    public static final String CLASS_NAME_PREFIX = "Graphine";

    private final OriginatingElementDependencyCollector originatingElementDependencyCollector;
    private final RepositoryNativeQueryRegistry repositoryNativeQueryRegistry;
    private final Map<MethodType, RepositoryMethodImplementationGenerator> methodGenerators;

    public RepositoryImplementationGenerator(
            OriginatingElementDependencyCollector originatingElementDependencyCollector,
            RepositoryNativeQueryRegistry repositoryNativeQueryRegistry,
            EntityMetadataRegistry entityMetadataRegistry,
            AttributeMapperMetadataRegistry attributeMapperMetadataRegistry,
            StatementMappingRenderer statementMappingRenderer,
            ResultSetMappingRenderer resultSetMappingRenderer) {
        this.originatingElementDependencyCollector = originatingElementDependencyCollector;
        this.repositoryNativeQueryRegistry = repositoryNativeQueryRegistry;

        this.methodGenerators = new EnumMap<>(MethodType.class);
        this.methodGenerators.put(MethodType.FIND,
                                  new RepositoryFindMethodImplementationGenerator(entityMetadataRegistry,
                                                                                  attributeMapperMetadataRegistry,
                                                                                  statementMappingRenderer,
                                                                                  resultSetMappingRenderer));
        this.methodGenerators.put(MethodType.COUNT,
                                  new RepositoryCountMethodImplementationGenerator(entityMetadataRegistry,
                                                                                   attributeMapperMetadataRegistry,
                                                                                   statementMappingRenderer,
                                                                                   resultSetMappingRenderer));
        this.methodGenerators.put(MethodType.SAVE,
                                  new RepositorySaveMethodImplementationGenerator(entityMetadataRegistry,
                                                                                  attributeMapperMetadataRegistry,
                                                                                  statementMappingRenderer,
                                                                                  resultSetMappingRenderer));
        this.methodGenerators.put(MethodType.UPDATE,
                                  new RepositoryUpdateMethodImplementationGenerator(entityMetadataRegistry,
                                                                                    attributeMapperMetadataRegistry,
                                                                                    statementMappingRenderer,
                                                                                    resultSetMappingRenderer));
        this.methodGenerators.put(MethodType.DELETE,
                                  new RepositoryDeleteMethodImplementationGenerator(entityMetadataRegistry,
                                                                                    attributeMapperMetadataRegistry,
                                                                                    statementMappingRenderer,
                                                                                    resultSetMappingRenderer));
    }

    public TypeSpec generate(RepositoryMetadata repository) {
        TypeSpec.Builder classBuilder = TypeSpec
                .classBuilder(ClassName.get(repository.getPackageName(), CLASS_NAME_PREFIX + repository.getName()))
                .addAnnotation(AnnotationSpec.builder(Generated.class)
                                             .addMember("value", "$S", "io.graphine.processor.GraphineProcessor")
                                             .build())
                .addModifiers(PUBLIC)
                .addSuperinterface(repository.getNativeType())
                .addField(DataSource.class, "dataSource", PRIVATE, FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                                     .addModifiers(PUBLIC)
                                     .addParameter(DataSource.class, "dataSource")
                                     .addStatement("this.dataSource = dataSource")
                                     .build());

        Collection<Element> originatingElements = originatingElementDependencyCollector.collect(repository);
        originatingElements.forEach(classBuilder::addOriginatingElement);

        String entityQualifiedName = repository.getEntityQualifiedName();

        List<MethodMetadata> methods = repository.getMethods();
        for (MethodMetadata method : methods) {
            QueryableMethodName queryableName = method.getQueryableName();
            QualifierFragment qualifier = queryableName.getQualifier();
            if (isNull(qualifier)) continue; // Method implementation is skipped because it is invalid!

            NativeQuery query = repositoryNativeQueryRegistry.getQuery(method);

            RepositoryMethodImplementationGenerator methodGenerator = methodGenerators.get(qualifier.getMethodType());
            classBuilder.addMethod(methodGenerator.generate(method, query, entityQualifiedName));
        }

        return classBuilder.build();
    }
}
