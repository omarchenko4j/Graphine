package io.graphine.processor.code.generator.repository;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.RepositoryMetadata;

import javax.annotation.processing.Generated;
import javax.sql.DataSource;

import static javax.lang.model.element.Modifier.*;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryImplementationGenerator {
    public static final String CLASS_NAME_PREFIX = "Graphine";

    public TypeSpec generate(RepositoryMetadata repository) {
        EntityMetadata entity = repository.getEntity();

        return TypeSpec
                .classBuilder(ClassName.get(repository.getPackageName(), CLASS_NAME_PREFIX + repository.getName()))
                .addOriginatingElement(repository.getNativeElement())
                // Entity is a dependency of the repository implementation.
                // It positively affects on incremental build!
                .addOriginatingElement(entity.getNativeElement())
                .addAnnotation(AnnotationSpec.builder(Generated.class)
                                             .addMember("value", "$S", "io.graphine.processor.GraphineRepositoryProcessor")
                                             .build())
                .addModifiers(PUBLIC, ABSTRACT) // TODO: Remove abstract modifier after adding method generators.
                .addSuperinterface(repository.getNativeType())
                .addField(DataSource.class, "dataSource", PRIVATE, FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                                     .addModifiers(PUBLIC)
                                     .addParameter(DataSource.class, "dataSource")
                                     .addStatement("this.dataSource = dataSource")
                                     .build())
                .build();
    }
}
