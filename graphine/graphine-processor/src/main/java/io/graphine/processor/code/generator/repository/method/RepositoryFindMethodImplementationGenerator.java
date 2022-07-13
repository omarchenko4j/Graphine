package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.graphine.processor.code.renderer.index.NumericParameterIndexProvider;
import io.graphine.processor.code.renderer.index.ParameterIndexProvider;
import io.graphine.processor.code.renderer.mapping.ResultSetMappingRenderer;
import io.graphine.processor.code.renderer.mapping.StatementMappingRenderer;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;
import io.graphine.processor.query.model.NativeQuery;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.*;

import static io.graphine.processor.code.generator.infrastructure.GeneralExceptionGenerator.NonUniqueResultException;
import static io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment.MethodForm.PLURAL;
import static io.graphine.processor.util.StringUtils.uncapitalize;
import static io.graphine.processor.util.VariableNameUniqueizer.uniqueize;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryFindMethodImplementationGenerator extends RepositoryMethodImplementationGenerator {
    private static final String COLLECTION_VARIABLE_NAME = uniqueize("elements");

    public RepositoryFindMethodImplementationGenerator(EntityMetadataRegistry entityMetadataRegistry,
                                                       StatementMappingRenderer statementMappingRenderer,
                                                       ResultSetMappingRenderer resultSetMappingRenderer) {
        super(entityMetadataRegistry, statementMappingRenderer, resultSetMappingRenderer);
    }

    @Override
    protected CodeBlock renderResultSetParameters(MethodMetadata method, NativeQuery query, EntityMetadata entity) {
        CodeBlock.Builder snippetBuilder = CodeBlock.builder();

        ExecutableElement methodElement = method.getNativeElement();
        TypeMirror returnType = methodElement.getReturnType();

        String entityVariableName = uniqueize(uncapitalize(entity.getName()));

        QueryableMethodName queryableName = method.getQueryableName();
        QualifierFragment qualifier = queryableName.getQualifier();
        if (qualifier.getMethodForm() == PLURAL) {
            switch (returnType.getKind()) {
                case ARRAY:
                    snippetBuilder
                            .addStatement("$T $L = new $T<>()",
                                          ParameterizedTypeName.get(ClassName.get(Collection.class),
                                                                    TypeName.get(entity.getNativeType())),
                                          COLLECTION_VARIABLE_NAME,
                                          ArrayList.class);
                    break;
                case DECLARED:
                    DeclaredType declaredType = (DeclaredType) returnType;
                    TypeElement typeElement = (TypeElement) declaredType.asElement();
                    switch (typeElement.getQualifiedName().toString()) {
                        case "java.lang.Iterable":
                        case "java.util.stream.Stream":
                            snippetBuilder
                                    .addStatement("$T $L = new $T<>()",
                                                  ParameterizedTypeName.get(ClassName.get(Collection.class),
                                                                            TypeName.get(entity.getNativeType())),
                                                  COLLECTION_VARIABLE_NAME,
                                                  ArrayList.class);
                            break;
                        case "java.util.Collection":
                        case "java.util.List":
                            snippetBuilder
                                    .addStatement("$T $L = new $T<>()",
                                                  ParameterizedTypeName.get(returnType),
                                                  COLLECTION_VARIABLE_NAME,
                                                  ArrayList.class);
                            break;
                        case "java.util.Set":
                            snippetBuilder
                                    .addStatement("$T $L = new $T<>()",
                                                  ParameterizedTypeName.get(returnType),
                                                  COLLECTION_VARIABLE_NAME,
                                                  HashSet.class);
                            break;
                    }
                    break;
            }

            snippetBuilder
                    .beginControlFlow("while ($L.next())", RESULT_SET_VARIABLE_NAME);
        }
        else {
            snippetBuilder
                    .beginControlFlow("if ($L.next())", RESULT_SET_VARIABLE_NAME);
        }

        ParameterIndexProvider parameterIndexProvider = new NumericParameterIndexProvider();

        snippetBuilder
                .addStatement("$T $L = new $T()",
                              entity.getNativeType(),
                              entityVariableName,
                              entity.getNativeType());
        List<AttributeMetadata> attributes = entity.getAttributes();
        for (AttributeMetadata attribute : attributes) {
            snippetBuilder
                    .add(attributeFromResultSetMappingRenderer.renderAttribute(entityVariableName,
                                                                               attribute,
                                                                               parameterIndexProvider));
        }

        switch (returnType.getKind()) {
            case ARRAY:
                snippetBuilder
                        .addStatement("$L.add($L)",
                                      COLLECTION_VARIABLE_NAME,
                                      entityVariableName);
                break;
            case DECLARED:
                DeclaredType declaredType = (DeclaredType) returnType;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                String elementQualifiedName = typeElement.getQualifiedName().toString();
                switch (elementQualifiedName) {
                    case "java.lang.Iterable":
                    case "java.util.Collection":
                    case "java.util.List":
                    case "java.util.Set":
                    case "java.util.stream.Stream":
                        snippetBuilder
                                .addStatement("$L.add($L)",
                                              COLLECTION_VARIABLE_NAME,
                                              entityVariableName);
                        break;
                    default:
                        if (!qualifier.hasFirstSpecifier()) {
                            snippetBuilder
                                    .beginControlFlow("if ($L.next())", RESULT_SET_VARIABLE_NAME)
                                    .addStatement("throw new $T()", NonUniqueResultException)
                                    .endControlFlow();
                        }

                        if (elementQualifiedName.equals("java.util.Optional")) {
                            snippetBuilder
                                    .addStatement("return $T.of($L)",
                                                  Optional.class,
                                                  entityVariableName);
                        }
                        else {
                            snippetBuilder
                                    .addStatement("return $L",
                                                  entityVariableName);
                        }
                        break;
                }
                break;
        }

        snippetBuilder
                .endControlFlow();

        switch (returnType.getKind()) {
            case ARRAY:
                snippetBuilder
                        .addStatement("return $L.toArray(new $T[0])",
                                      COLLECTION_VARIABLE_NAME,
                                      entity.getNativeType());
                break;
            case DECLARED:
                DeclaredType declaredType = (DeclaredType) returnType;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                switch (typeElement.getQualifiedName().toString()) {
                    case "java.util.Optional":
                        snippetBuilder
                                .addStatement("return $T.empty()",
                                              Optional.class);
                        break;
                    case "java.lang.Iterable":
                    case "java.util.Collection":
                    case "java.util.List":
                    case "java.util.Set":
                        snippetBuilder
                                .addStatement("return $L",
                                              COLLECTION_VARIABLE_NAME);
                        break;
                    case "java.util.stream.Stream":
                        snippetBuilder
                                .addStatement("return $L.stream()",
                                              COLLECTION_VARIABLE_NAME);
                        break;
                    default:
                        snippetBuilder
                                .addStatement("return null");
                        break;
                }
                break;
        }

        return snippetBuilder.build();
    }
}
