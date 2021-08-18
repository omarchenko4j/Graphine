package io.graphine.processor.metadata.validator.repository;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.RepositoryMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;
import io.graphine.processor.metadata.validator.repository.method.*;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment.MethodType;
import static io.graphine.processor.support.EnvironmentContext.messager;
import static java.util.Objects.isNull;
import static javax.tools.Diagnostic.Kind;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryMetadataValidator {
    private final EntityMetadataRegistry entityMetadataRegistry;

    public RepositoryMetadataValidator(EntityMetadataRegistry entityMetadataRegistry) {
        this.entityMetadataRegistry = entityMetadataRegistry;
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
        if (!entityMetadataRegistry.exists(entityQualifiedName)) {
            messager.printMessage(Kind.ERROR, "Entity class not found", repository.getNativeElement());
            return false;
        }

        boolean valid = true;

        EntityMetadata entity = entityMetadataRegistry.get(entityQualifiedName);
        if (isNull(entity)) return false; // Abort validation if the entity has no metadata.

        Map<MethodType, RepositoryMethodMetadataValidator> methodValidators = new EnumMap<>(MethodType.class);

        List<MethodMetadata> methods = repository.getMethods();
        for (MethodMetadata method : methods) {
            QueryableMethodName queryableName = method.getQueryableName();

            QualifierFragment qualifier = queryableName.getQualifier();
            if (isNull(qualifier)) continue; // Method validation is skipped! Error will be thrown in the parser.

            RepositoryMethodMetadataValidator repositoryMethodMetadataValidator =
                    methodValidators.computeIfAbsent(qualifier.getMethodType(),
                                                     methodType -> createMethodValidator(methodType, entity));
            if (!repositoryMethodMetadataValidator.validate(method)) {
                valid = false;
            }
        }
        return valid;
    }

    private RepositoryMethodMetadataValidator createMethodValidator(MethodType methodType, EntityMetadata entity) {
        switch (methodType) {
            case FIND:
                return new RepositoryFindMethodMetadataValidator(entity);
            case COUNT:
                return new RepositoryCountMethodMetadataValidator(entity);
            case SAVE:
                return new RepositorySaveMethodMetadataValidator(entity);
            case UPDATE:
                return new RepositoryUpdateMethodMetadataValidator(entity);
            case DELETE:
                return new RepositoryDeleteMethodMetadataValidator(entity);
            default:
                // Unreachable exception for the current method type.
                throw new IllegalArgumentException("Unknown method type");
        }
    }
}
