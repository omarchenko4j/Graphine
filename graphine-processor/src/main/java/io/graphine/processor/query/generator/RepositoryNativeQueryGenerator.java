package io.graphine.processor.query.generator;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.RepositoryMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment.MethodType;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;
import io.graphine.processor.query.generator.specific.*;
import io.graphine.processor.query.model.NativeQuery;
import io.graphine.processor.query.registry.RepositoryNativeQueryRegistry;
import io.graphine.processor.query.registry.RepositoryNativeQueryRegistryStorage;

import java.util.*;

import static java.util.Objects.isNull;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryNativeQueryGenerator {
    private final EntityMetadataRegistry entityMetadataRegistry;

    public RepositoryNativeQueryGenerator(EntityMetadataRegistry entityMetadataRegistry) {
        this.entityMetadataRegistry = entityMetadataRegistry;
    }

    public RepositoryNativeQueryRegistryStorage generate(Collection<RepositoryMetadata> repositories) {
        List<RepositoryNativeQueryRegistry> registries = new ArrayList<>(repositories.size());
        for (RepositoryMetadata repository : repositories) {
            registries.add(generate(repository));
        }
        return new RepositoryNativeQueryRegistryStorage(registries);
    }

    public RepositoryNativeQueryRegistry generate(RepositoryMetadata repository) {
        EntityMetadata entity = entityMetadataRegistry.get(repository.getEntityQualifiedName());

        RepositoryNativeQueryRegistry repositoryNativeQueryRegistry = new RepositoryNativeQueryRegistry(repository);

        Map<MethodType, RepositoryMethodNativeQueryGenerator> nativeQueryGenerators = new EnumMap<>(MethodType.class);

        List<MethodMetadata> methods = repository.getMethods();
        for (MethodMetadata method : methods) {
            QueryableMethodName queryableName = method.getQueryableName();

            QualifierFragment qualifier = queryableName.getQualifier();
            if (isNull(qualifier)) continue; // Native query generation is skipped! Method is invalid.

            NativeQuery query = nativeQueryGenerators
                    .computeIfAbsent(qualifier.getMethodType(),
                                     methodType -> createSpecificNativeQueryGenerator(methodType, entity))
                    .generate(method);
            repositoryNativeQueryRegistry.registerQuery(method, query);
        }
        return repositoryNativeQueryRegistry;
    }

    private RepositoryMethodNativeQueryGenerator createSpecificNativeQueryGenerator(MethodType methodType,
                                                                                    EntityMetadata entity) {
        switch (methodType) {
            case FIND:
                return new RepositoryFindMethodNativeQueryGenerator(entity);
            case COUNT:
                return new RepositoryCountMethodNativeQueryGenerator(entity);
            case SAVE:
                return new RepositorySaveMethodNativeQueryGenerator(entity);
            case UPDATE:
                return new RepositoryUpdateMethodNativeQueryGenerator(entity);
            case DELETE:
                return new RepositoryDeleteMethodNativeQueryGenerator(entity);
            default:
                // Unreachable exception for the current method type.
                throw new IllegalArgumentException("Unknown method type");
        }
    }
}
