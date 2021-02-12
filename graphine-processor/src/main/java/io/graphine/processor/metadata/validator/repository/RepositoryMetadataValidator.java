package io.graphine.processor.metadata.validator.repository;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.RepositoryMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.List;

import static io.graphine.processor.support.EnvironmentContext.messager;
import static io.graphine.processor.util.RepositoryAnnotationUtils.getRepositoryAnnotation;
import static io.graphine.processor.util.RepositoryAnnotationUtils.getRepositoryAnnotationValue;
import static java.util.Objects.isNull;
import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.tools.Diagnostic.Kind;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryMetadataValidator {
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
        boolean valid = true;

        TypeElement repositoryElement = repository.getNativeElement();
        if (repositoryElement.getKind() != INTERFACE) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Repository must be an interface", repositoryElement);
        }
        if (!repositoryElement.getModifiers().contains(PUBLIC)) {
            valid = false;
            messager.printMessage(Kind.ERROR, "Repository interface must be public", repositoryElement);
        }

        EntityMetadata entity = repository.getEntity();
        if (isNull(entity)) {
            AnnotationMirror annotation = getRepositoryAnnotation(repositoryElement);
            AnnotationValue annotationValue = getRepositoryAnnotationValue(repositoryElement);
            messager.printMessage(Kind.ERROR, "Repository annotation contains a non-entity class",
                                  repositoryElement, annotation, annotationValue);
            return false;
        }

        List<MethodMetadata> methods = repository.getMethods();
        for (MethodMetadata method : methods) {
            QueryableMethodName queryableName = method.getQueryableName();

            QualifierFragment qualifier = queryableName.getQualifier();
            if (isNull(qualifier)) continue; // Method validation is skipped! Error will be thrown in the parser.

            // TODO: add validation of specific methods
        }
        return valid;
    }
}
