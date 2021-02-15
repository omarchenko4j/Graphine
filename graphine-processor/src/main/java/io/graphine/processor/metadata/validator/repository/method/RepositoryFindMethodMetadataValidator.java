package io.graphine.processor.metadata.validator.repository.method;

import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.SortingFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.SortingFragment.Sort;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment.MethodForm;
import static io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment.MethodForm.SINGULAR;
import static io.graphine.processor.support.EnvironmentContext.messager;
import static io.graphine.processor.support.EnvironmentContext.typeUtils;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static javax.tools.Diagnostic.Kind;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryFindMethodMetadataValidator extends MethodMetadataValidator {
    public RepositoryFindMethodMetadataValidator(EntityMetadata entity) {
        super(entity);
    }

    @Override
    protected boolean validateReturnType(ExecutableElement methodElement, MethodForm methodForm) {
        boolean valid = true;

        TypeMirror entityType = entityElement.asType();
        TypeMirror returnType = methodElement.getReturnType();

        switch (methodForm) {
            case SINGULAR:
                if (returnType.getKind() != TypeKind.DECLARED) {
                    valid = false;
                    messager.printMessage(Kind.ERROR, "Method must return the entity class", methodElement);
                }
                else {
                    DeclaredType declaredType = (DeclaredType) returnType;
                    String qualifiedName = ((TypeElement) declaredType.asElement()).getQualifiedName().toString();
                    if (qualifiedName.equals(Optional.class.getName())) {
                        returnType = declaredType.getTypeArguments().get(0);
                    }
                    if (!typeUtils.isSameType(returnType, entityType)) {
                        valid = false;
                        messager.printMessage(Kind.ERROR, "Method must return the entity class", methodElement);
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
                            messager.printMessage(Kind.ERROR,
                                                  "Method must return an array or collection of entity classes",
                                                  methodElement);
                        }
                        break;
                    case DECLARED:
                        DeclaredType declaredType = (DeclaredType) returnType;
                        String qualifiedName = ((TypeElement) declaredType.asElement()).getQualifiedName().toString();
                        if (qualifiedName.equals(Iterable.class.getName()) ||
                                qualifiedName.equals(Collection.class.getName()) ||
                                qualifiedName.equals(List.class.getName()) ||
                                qualifiedName.equals(Set.class.getName())) {
                            returnType = declaredType.getTypeArguments().get(0);
                            if (!typeUtils.isSameType(returnType, entityType)) {
                                valid = false;
                                messager.printMessage(Kind.ERROR,
                                                      "Method must return an array or collection of entity classes",
                                                      methodElement);
                            }
                        }
                        else {
                            valid = false;
                            messager.printMessage(Kind.ERROR,
                                                  "Method must return an array or collection of entity classes",
                                                  methodElement);
                        }
                        break;
                    default:
                        valid = false;
                        messager.printMessage(Kind.ERROR,
                                              "Method must return an array or collection of entity classes",
                                              methodElement);
                        break;
                }
                break;
        }

        return valid;
    }

    @Override
    protected boolean validateSignature(ExecutableElement methodElement, QueryableMethodName queryableName) {
        boolean valid = true;

        ConditionFragment condition = queryableName.getCondition();
        if (!validateConditionParameters(methodElement, condition)) {
            valid = false;
        }

        if (isNull(condition)) {
            QualifierFragment qualifier = queryableName.getQualifier();
            if (qualifier.getMethodForm() == SINGULAR) {
                valid = false;
                messager.printMessage(Kind.ERROR, "Method must have condition parameters after 'By' keyword", methodElement);
            }
            else {
                List<? extends VariableElement> parameters = methodElement.getParameters();
                if (!parameters.isEmpty()) {
                    valid = false;
                    messager.printMessage(Kind.ERROR,
                                          "Method without condition parameters should not contain method parameters",
                                          methodElement);
                }
            }
        }

        SortingFragment sorting = queryableName.getSorting();
        if (!validateSortingParameters(methodElement, sorting)) {
            valid = false;
        }

        return valid;
    }

    private boolean validateSortingParameters(ExecutableElement methodElement, SortingFragment sorting) {
        boolean valid = true;

        if (nonNull(sorting)) {
            List<Sort> sorts = sorting.getSorts();
            for (Sort sort : sorts) {
                String attributeName = sort.getAttributeName();

                AttributeMetadata attribute = entity.getAttribute(attributeName);
                if (isNull(attribute)) {
                    valid = false;
                    messager.printMessage(Kind.ERROR,
                                          "Sorting parameter '" + attributeName + "' not found as entity attribute",
                                          methodElement);
                }
            }
        }

        return valid;
    }
}
