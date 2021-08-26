package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.parameter.index_provider.NumericParameterIndexProvider;
import io.graphine.processor.code.renderer.parameter.index_provider.ParameterIndexProvider;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.IdentifierMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.model.repository.method.parameter.ParameterMetadata;
import io.graphine.processor.query.model.NativeQuery;

import javax.lang.model.type.TypeMirror;
import java.util.Collection;

import static io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment.MethodForm.PLURAL;
import static io.graphine.processor.util.AccessorUtils.getter;
import static io.graphine.processor.util.VariableNameUniqueizer.uniqueize;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryUpdateMethodImplementationGenerator extends RepositoryMethodImplementationGenerator {
    @Override
    protected CodeBlock renderQuery(MethodMetadata method, NativeQuery query) {
        return CodeBlock.builder()
                        .addStatement("String $L = $S", QUERY_VARIABLE_NAME, query.getValue())
                        .build();
    }

    @Override
    protected CodeBlock renderStatementParameters(MethodMetadata method, NativeQuery query, EntityMetadata entity) {
        ParameterMetadata parameter = method.getParameters().get(0);

        CodeBlock.Builder snippetBuilder = CodeBlock.builder();

        String entityVariableName;

        QueryableMethodName queryableName = method.getQueryableName();
        QualifierFragment qualifier = queryableName.getQualifier();
        if (qualifier.getMethodForm() == PLURAL) {
            entityVariableName = uniqueize("element");
            snippetBuilder
                    .beginControlFlow("for ($T $L : $L)",
                                      entity.getNativeType(), entityVariableName, parameter.getName());
        }
        else {
            entityVariableName = parameter.getName();
        }

        ParameterIndexProvider parameterIndexProvider = new NumericParameterIndexProvider();

        Collection<AttributeMetadata> attributes = entity.getAttributes(true);
        for (AttributeMetadata attribute : attributes) {
            TypeMirror attributeType = attribute.getNativeType();
            String attributeGetter = getter(attribute.getNativeElement());

            String parameterIndex = parameterIndexProvider.getParameterIndex();

            snippetBuilder.add(
                    preparedStatementMethodMappingRenderer.render(attributeType,
                                                                  parameterIndex,
                                                                  CodeBlock.of("$L.$L()",
                                                                               entityVariableName, attributeGetter))
            );
        }

        IdentifierMetadata identifier = entity.getIdentifier();
        snippetBuilder.add(
                preparedStatementMethodMappingRenderer.render(identifier.getNativeType(),
                                                              parameterIndexProvider.getParameterIndex(),
                                                              CodeBlock.of("$L.$L()",
                                                                           entityVariableName, getter(identifier.getNativeElement())))
        );

        if (qualifier.getMethodForm() == PLURAL) {
            snippetBuilder
                    .addStatement("$L.addBatch()", STATEMENT_VARIABLE_NAME)
                    .endControlFlow()
                    .addStatement("$L.executeBatch()", STATEMENT_VARIABLE_NAME);
        }
        else {
            snippetBuilder
                    .addStatement("$L.executeUpdate()", STATEMENT_VARIABLE_NAME);
        }

        return snippetBuilder.build();
    }

    @Override
    protected CodeBlock renderResultSet(MethodMetadata method, NativeQuery query) {
        return CodeBlock.builder().build();
    }
}
