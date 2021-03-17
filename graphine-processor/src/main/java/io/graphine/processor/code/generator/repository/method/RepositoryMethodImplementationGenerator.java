package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.MethodSpec;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.query.model.NativeQuery;

/**
 * @author Oleg Marchenko
 */
public abstract class RepositoryMethodImplementationGenerator {
    public abstract MethodSpec generate(MethodMetadata method, NativeQuery query);
}
