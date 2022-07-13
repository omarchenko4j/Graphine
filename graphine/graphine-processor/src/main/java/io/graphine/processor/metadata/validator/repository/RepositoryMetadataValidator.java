package io.graphine.processor.metadata.validator.repository;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.RepositoryMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;
import io.graphine.processor.metadata.validator.repository.method.*;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment.MethodType;
import static io.graphine.processor.support.EnvironmentContext.messager;
import static io.graphine.processor.util.RepositoryAnnotationUtils.getRepositoryAnnotation;
import static io.graphine.processor.util.RepositoryAnnotationUtils.getRepositoryAnnotationValue;
import static java.util.Objects.isNull;
import static javax.tools.Diagnostic.Kind;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryMetadataValidator {
    private final EntityMetadataRegistry entityMetadataRegistry;
    private final Map<MethodType, RepositoryMethodMetadataValidator> methodValidators;

    public RepositoryMetadataValidator(EntityMetadataRegistry entityMetadataRegistry) {
        this.entityMetadataRegistry = entityMetadataRegistry;

        this.methodValidators = new EnumMap<>(MethodType.class);
        this.methodValidators.put(MethodType.FIND, new RepositoryFindMethodMetadataValidator(entityMetadataRegistry));
        this.methodValidators.put(MethodType.COUNT, new RepositoryCountMethodMetadataValidator(entityMetadataRegistry));
        this.methodValidators.put(MethodType.SAVE, new RepositorySaveMethodMetadataValidator(entityMetadataRegistry));
        this.methodValidators.put(MethodType.UPDATE, new RepositoryUpdateMethodMetadataValidator(entityMetadataRegistry));
        this.methodValidators.put(MethodType.DELETE, new RepositoryDeleteMethodMetadataValidator(entityMetadataRegistry));
    }

    public boolean validate(Collection<RepositoryMetadata> repositories) {
        boolean valid = true;
        for (RepositoryMetadata repository : repositories) {
            if (!validate(repository)) {
                valid = false;
            }
        }
        return valid;
    }

    public boolean validate(RepositoryMetadata repository) {
        String entityQualifiedName = repository.getEntityQualifiedName();
        if (!entityMetadataRegistry.containsEntity(entityQualifiedName)) {
            TypeElement repositoryElement = repository.getNativeElement();
            AnnotationMirror repositoryAnnotation = getRepositoryAnnotation(repositoryElement);
            AnnotationValue repositoryAnnotationValue = getRepositoryAnnotationValue(repositoryElement);
            messager.printMessage(Kind.ERROR,
                                  "Class '" + repositoryAnnotationValue.getValue().toString() +
                                  "' must be annotated with @Entity",
                                  repositoryElement, repositoryAnnotation, repositoryAnnotationValue);
            return false;
        }

        EntityMetadata entity = entityMetadataRegistry.getEntity(entityQualifiedName);
        if (isNull(entity)) return false; // Abort validation if the entity has no metadata.

        boolean valid = true;

        List<MethodMetadata> methods = repository.getMethods();
        for (MethodMetadata method : methods) {
            QueryableMethodName queryableName = method.getQueryableName();
            QualifierFragment qualifier = queryableName.getQualifier();

            RepositoryMethodMetadataValidator methodValidator = methodValidators.get(qualifier.getMethodType());
            if (!methodValidator.validate(method, entity)) {
                valid = false;
            }
        }

        return valid;
    }
}
