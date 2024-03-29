package io.graphine.processor.metadata.validator.repository.method;

import io.graphine.processor.metadata.model.entity.EmbeddableEntityMetadata;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.EmbeddedAttributeMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.AttributeChain;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.SortingFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.SortingFragment.Sort;
import io.graphine.processor.metadata.model.repository.method.parameter.ParameterMetadata;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static io.graphine.processor.support.EnvironmentContext.logger;
import static io.graphine.processor.support.EnvironmentContext.typeUtils;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryFindMethodMetadataValidator extends RepositoryMethodMetadataValidator {
    public RepositoryFindMethodMetadataValidator(EntityMetadataRegistry entityMetadataRegistry) {
        super(entityMetadataRegistry);
    }

    @Override
    protected boolean validateReturnType(MethodMetadata method, EntityMetadata entity) {
        boolean valid = true;

        TypeMirror entityType = entity.getNativeType();

        ExecutableElement methodElement = method.getNativeElement();
        TypeMirror returnType = methodElement.getReturnType();

        QueryableMethodName queryableName = method.getQueryableName();

        QualifierFragment qualifier = queryableName.getQualifier();
        switch (qualifier.getMethodForm()) {
            case SINGULAR:
                if (returnType.getKind() != TypeKind.DECLARED) {
                    valid = false;
                    logger.error("Method must return the entity class", methodElement);
                }
                else {
                    DeclaredType declaredType = (DeclaredType) returnType;
                    String qualifiedName = ((TypeElement) declaredType.asElement()).getQualifiedName().toString();
                    if (qualifiedName.equals(Optional.class.getName())) {
                        returnType = declaredType.getTypeArguments().get(0);
                    }
                    if (!typeUtils.isSameType(returnType, entityType)) {
                        valid = false;
                        logger.error("Method must return the entity class", methodElement);
                    }
                }
                break;
            case PLURAL:
                switch (returnType.getKind()) {
                    case ARRAY:
                        ArrayType arrayType = (ArrayType) returnType;
                        returnType = arrayType.getComponentType();
                        if (!typeUtils.isSameType(returnType, entityType)) {
                            valid = false;
                            logger.error("Method must return an array or collection of entity classes", methodElement);
                        }
                        break;
                    case DECLARED:
                        DeclaredType declaredType = (DeclaredType) returnType;
                        String qualifiedName = ((TypeElement) declaredType.asElement()).getQualifiedName().toString();
                        if (qualifiedName.equals(Iterable.class.getName()) ||
                            qualifiedName.equals(Collection.class.getName()) ||
                            qualifiedName.equals(List.class.getName()) ||
                            qualifiedName.equals(Set.class.getName()) ||
                            qualifiedName.equals(Stream.class.getName())) {
                            returnType = declaredType.getTypeArguments().get(0);
                            if (!typeUtils.isSameType(returnType, entityType)) {
                                valid = false;
                                logger.error("Method must return an array or collection of entity classes", methodElement);
                            }
                        }
                        else {
                            valid = false;
                            logger.error("Method must return an array or collection of entity classes", methodElement);
                        }
                        break;
                    default:
                        valid = false;
                        logger.error("Method must return an array or collection of entity classes", methodElement);
                        break;
                }
                break;
        }

        return valid;
    }

    @Override
    protected boolean validateSignature(MethodMetadata method, EntityMetadata entity) {
        boolean valid = true;

        QueryableMethodName queryableName = method.getQueryableName();

        QualifierFragment qualifier = queryableName.getQualifier();
        if (qualifier.isSingularForm()) {
            if (qualifier.hasDistinctSpecifier()) {
                valid = false;
                logger.error("Method name must not include 'Distinct' keyword", method.getNativeElement());
            }
        }

        ConditionFragment condition = queryableName.getCondition();
        if (isNull(condition)) {
            if (qualifier.isSingularForm() && !qualifier.hasFirstSpecifier()) {
                valid = false;
                logger.error("Method name must have condition parameters after 'By' keyword", method.getNativeElement());
            }
            else {
                List<ParameterMetadata> methodParameters = method.getParameters();
                if (!methodParameters.isEmpty()) {
                    valid = false;
                    logger.error("Method without condition parameters should not contain method parameters",
                                 method.getNativeElement());
                }
            }
        }
        else {
            if (!validateConditionParameters(method, entity)) {
                valid = false;
            }
        }

        SortingFragment sorting = queryableName.getSorting();
        if (nonNull(sorting)) {
            if (qualifier.isSingularForm() && !qualifier.hasFirstSpecifier()) {
                valid = false;
                logger.error("Method name must not include sorting", method.getNativeElement());
            }
            if (!validateSortingParameters(method, entity)) {
                valid = false;
            }
        }
        else {
            if (qualifier.hasFirstSpecifier()) {
                logger.warn("Use explicit sorting in the method name", method.getNativeElement());
            }
        }

        return valid;
    }

    private boolean validateSortingParameters(MethodMetadata method, EntityMetadata entity) {
        boolean valid = true;

        QueryableMethodName queryableName = method.getQueryableName();
        SortingFragment sorting = queryableName.getSorting();
        List<Sort> sorts = sorting.getSorts();
        for (Sort sort : sorts) {
            AttributeChain attributeChain = sort.getAttributeChain();
            List<String> attributeNames = attributeChain.getAttributeNames();
            // Predicate must have at least one attribute.
            String attributeName = attributeNames.get(0);

            AttributeMetadata attribute = entity.getAttribute(attributeName);
            if (isNull(attribute)) {
                valid = false;
                logger.error("Sorting parameter '" + attributeName + "' not found as entity attribute",
                             method.getNativeElement());
                continue;
            }

            for (int i = 1; i < attributeNames.size(); i++) {
                attributeName = attributeNames.get(i);

                if (attribute instanceof EmbeddedAttributeMetadata) {
                    EmbeddableEntityMetadata embeddableEntity =
                            entityMetadataRegistry.getEmbeddableEntity(attribute.getNativeType().toString());
                    AttributeMetadata innerAttribute = embeddableEntity.getAttribute(attributeName);
                    if (isNull(innerAttribute)) {
                        valid = false;
                        logger.error("Sorting parameter (" + attributeName + ") not found in embeddable entity as attribute",
                                     method.getNativeElement());
                        break;
                    }

                    attribute = innerAttribute;
                }
                else {
                    valid = false;
                    logger.error("Sorting parameter (" + attribute.getName() + ") is not an embeddable entity type",
                                 method.getNativeElement());
                    break;
                }
            }
        }

        return valid;
    }
}
