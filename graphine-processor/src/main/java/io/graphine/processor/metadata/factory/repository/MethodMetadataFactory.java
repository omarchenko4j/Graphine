package io.graphine.processor.metadata.factory.repository;

import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.parameter.ParameterMetadata;
import io.graphine.processor.metadata.parser.RepositoryMethodNameParser;

import javax.lang.model.element.ExecutableElement;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Oleg Marchenko
 */
public final class MethodMetadataFactory {
    private final RepositoryMethodNameParser repositoryMethodNameParser;

    public MethodMetadataFactory(RepositoryMethodNameParser repositoryMethodNameParser) {
        this.repositoryMethodNameParser = repositoryMethodNameParser;
    }

    public MethodMetadata createMethod(ExecutableElement methodElement) {
        QueryableMethodName queryableName = repositoryMethodNameParser.parse(methodElement);

        List<ParameterMetadata> parameters =
                methodElement.getParameters()
                             .stream()
                             .map(ParameterMetadata::new)
                             .collect(Collectors.toList());

        return new MethodMetadata(methodElement, queryableName, parameters);
    }
}
