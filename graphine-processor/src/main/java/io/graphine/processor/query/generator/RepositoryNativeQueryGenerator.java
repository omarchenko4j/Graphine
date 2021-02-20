package io.graphine.processor.query.generator;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.RepositoryMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment.MethodType;
import io.graphine.processor.query.generator.specific.*;
import io.graphine.processor.query.model.NativeQuery;
import io.graphine.processor.query.registry.RepositoryNativeQueryRegistry;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryNativeQueryGenerator {
    public RepositoryNativeQueryRegistry generate(RepositoryMetadata repository) {
        EntityMetadata entity = repository.getEntity();

        RepositoryNativeQueryRegistry repositoryNativeQueryRegistry = new RepositoryNativeQueryRegistry(repository);

        Map<MethodType, RepositoryMethodNativeQueryGenerator> nativeQueryGenerators = new EnumMap<>(MethodType.class);

        List<MethodMetadata> methods = repository.getMethods();
        for (MethodMetadata method : methods) {
            QueryableMethodName queryableName = method.getQueryableName();
            QualifierFragment qualifier = queryableName.getQualifier();

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
