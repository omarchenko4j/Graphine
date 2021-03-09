package io.graphine.processor.code.generator.repository;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.graphine.processor.code.generator.repository.method.*;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.RepositoryMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.query.model.NativeQuery;
import io.graphine.processor.query.registry.RepositoryNativeQueryRegistry;

import javax.annotation.processing.Generated;
import javax.sql.DataSource;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment.MethodType;
import static javax.lang.model.element.Modifier.*;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryImplementationGenerator {
    public static final String CLASS_NAME_PREFIX = "Graphine";

    private final RepositoryNativeQueryRegistry repositoryNativeQueryRegistry;

    public RepositoryImplementationGenerator(RepositoryNativeQueryRegistry repositoryNativeQueryRegistry) {
        this.repositoryNativeQueryRegistry = repositoryNativeQueryRegistry;
    }

    public TypeSpec generate(RepositoryMetadata repository) {
        EntityMetadata entity = repository.getEntity();

        TypeSpec.Builder classBuilder = TypeSpec
                .classBuilder(ClassName.get(repository.getPackageName(), CLASS_NAME_PREFIX + repository.getName()))
                .addOriginatingElement(repository.getNativeElement())
                // Entity is a dependency of the repository implementation.
                // It positively affects on incremental build!
                .addOriginatingElement(entity.getNativeElement())
                .addAnnotation(AnnotationSpec.builder(Generated.class)
                                             .addMember("value", "$S", "io.graphine.processor.GraphineRepositoryProcessor")
                                             .build())
                .addModifiers(PUBLIC)
                .addSuperinterface(repository.getNativeType())
                .addField(DataSource.class, "dataSource", PRIVATE, FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                                     .addModifiers(PUBLIC)
                                     .addParameter(DataSource.class, "dataSource")
                                     .addStatement("this.dataSource = dataSource")
                                     .build());

        Map<MethodType, RepositoryMethodImplementationGenerator> methodImplementationGenerators = new EnumMap<>(MethodType.class);
        methodImplementationGenerators.put(MethodType.FIND, new RepositoryFindMethodImplementationGenerator(entity));
        methodImplementationGenerators.put(MethodType.COUNT, new RepositoryCountMethodImplementationGenerator(entity));
        methodImplementationGenerators.put(MethodType.SAVE, new RepositorySaveMethodImplementationGenerator(entity));
        methodImplementationGenerators.put(MethodType.UPDATE, new RepositoryUpdateMethodImplementationGenerator(entity));
        methodImplementationGenerators.put(MethodType.DELETE, new RepositoryDeleteMethodImplementationGenerator(entity));

        List<MethodMetadata> methods = repository.getMethods();
        for (MethodMetadata method : methods) {
            QueryableMethodName queryableName = method.getQueryableName();
            QualifierFragment qualifier = queryableName.getQualifier();

            RepositoryMethodImplementationGenerator methodImplementationGenerator =
                    methodImplementationGenerators.get(qualifier.getMethodType());

            NativeQuery query = repositoryNativeQueryRegistry.getQuery(method);

            classBuilder.addMethod(methodImplementationGenerator.generate(method, query));
        }

        return classBuilder.build();
    }
}
