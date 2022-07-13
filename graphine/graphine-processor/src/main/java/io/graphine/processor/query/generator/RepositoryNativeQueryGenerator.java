package io.graphine.processor.query.generator;

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
    private final Map<MethodType, RepositoryMethodNativeQueryGenerator> nativeQueryGenerators;

    public RepositoryNativeQueryGenerator(EntityMetadataRegistry entityMetadataRegistry) {
        this.nativeQueryGenerators = new EnumMap<>(MethodType.class);
        this.nativeQueryGenerators.put(MethodType.FIND, new RepositoryFindMethodNativeQueryGenerator(entityMetadataRegistry));
        this.nativeQueryGenerators.put(MethodType.COUNT, new RepositoryCountMethodNativeQueryGenerator(entityMetadataRegistry));
        this.nativeQueryGenerators.put(MethodType.SAVE, new RepositorySaveMethodNativeQueryGenerator(entityMetadataRegistry));
        this.nativeQueryGenerators.put(MethodType.UPDATE, new RepositoryUpdateMethodNativeQueryGenerator(entityMetadataRegistry));
        this.nativeQueryGenerators.put(MethodType.DELETE, new RepositoryDeleteMethodNativeQueryGenerator(entityMetadataRegistry));
    }

    public RepositoryNativeQueryRegistryStorage generate(Collection<RepositoryMetadata> repositories) {
        List<RepositoryNativeQueryRegistry> registries = new ArrayList<>(repositories.size());
        for (RepositoryMetadata repository : repositories) {
            registries.add(generate(repository));
        }
        return new RepositoryNativeQueryRegistryStorage(registries);
    }

    public RepositoryNativeQueryRegistry generate(RepositoryMetadata repository) {
        String entityQualifiedName = repository.getEntityQualifiedName();

        RepositoryNativeQueryRegistry repositoryNativeQueryRegistry = new RepositoryNativeQueryRegistry(repository);

        List<MethodMetadata> methods = repository.getMethods();
        for (MethodMetadata method : methods) {
            QueryableMethodName queryableName = method.getQueryableName();

            QualifierFragment qualifier = queryableName.getQualifier();
            if (isNull(qualifier)) continue; // Native query generation is skipped! Method is invalid.

            RepositoryMethodNativeQueryGenerator nativeQueryGenerator = nativeQueryGenerators.get(qualifier.getMethodType());

            NativeQuery query = nativeQueryGenerator.generate(entityQualifiedName, method);
            repositoryNativeQueryRegistry.registerQuery(method, query);
        }
        return repositoryNativeQueryRegistry;
    }
}
