package io.graphine.processor.metadata.validator.repository;

import io.graphine.core.GraphineRepository;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

import static io.graphine.processor.support.EnvironmentContext.messager;
import static java.util.Objects.isNull;
import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.tools.Diagnostic.Kind;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryElementValidator {
    public boolean validate(TypeElement repositoryElement) {
        boolean valid = true;

        if (repositoryElement.getKind() != INTERFACE) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Repository must be an interface", repositoryElement);
        }

        if (!repositoryElement.getModifiers().contains(PUBLIC)) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Repository interface must be public", repositoryElement);
        }

        DeclaredType graphineRepositoryInterface =
                repositoryElement.getInterfaces()
                                 .stream()
                                 .map(interfaceType -> (DeclaredType) interfaceType)
                                 .filter(interfaceType -> {
                                     TypeElement interfaceElement = (TypeElement) interfaceType.asElement();
                                     return interfaceElement.getQualifiedName()
                                                            .contentEquals(GraphineRepository.class.getName());
                                 })
                                 .findFirst()
                                 .orElse(null);
        if (isNull(graphineRepositoryInterface)) {
            messager.printMessage(Kind.ERROR, "Repository interface should extend GraphineRepository interface", repositoryElement);
            return false;
        }
        if (graphineRepositoryInterface.getTypeArguments().isEmpty()) {
            messager.printMessage(Kind.ERROR, "GraphineRepository interface must be parameterized by the entity class", repositoryElement);
            return false;
        }

        return valid;
    }
}
