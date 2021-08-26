package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.parameter.index_provider.IncrementalParameterIndexProvider;
import io.graphine.processor.code.renderer.parameter.index_provider.NumericParameterIndexProvider;
import io.graphine.processor.code.renderer.parameter.index_provider.ParameterIndexProvider;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.IdentifierMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.model.repository.method.parameter.ParameterMetadata;
import io.graphine.processor.query.model.NativeQuery;

import java.sql.PreparedStatement;

import static io.graphine.processor.code.renderer.parameter.index_provider.IncrementalParameterIndexProvider.INDEX_VARIABLE_NAME;
import static io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment.MethodForm.PLURAL;
import static io.graphine.processor.util.AccessorUtils.getter;
import static io.graphine.processor.util.VariableNameUniqueizer.uniqueize;
import static java.util.Objects.isNull;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryDeleteMethodImplementationGenerator extends RepositoryMethodImplementationGenerator {
    @Override
    protected CodeBlock renderStatement(MethodMetadata method, NativeQuery query, EntityMetadata entity) {
        return CodeBlock.builder()
                        .beginControlFlow("try ($T $L = $L.prepareStatement($L))",
                                          PreparedStatement.class,
                                          STATEMENT_VARIABLE_NAME,
                                          CONNECTION_VARIABLE_NAME,
                                          QUERY_VARIABLE_NAME)
                        .add(renderStatementParameters(method, query, entity))
                        .addStatement("$L.executeUpdate()", STATEMENT_VARIABLE_NAME)
                        .endControlFlow()
                        .build();
    }

    @Override
    protected CodeBlock renderStatementParameters(MethodMetadata method, NativeQuery query, EntityMetadata entity) {
        CodeBlock.Builder snippetBuilder = CodeBlock.builder();

        QueryableMethodName queryableName = method.getQueryableName();
        ConditionFragment condition = queryableName.getCondition();
        if (isNull(condition)) {
            ParameterMetadata parameter = method.getParameters().get(0);

            ParameterIndexProvider parameterIndexProvider;

            String entityVariableName;

            QualifierFragment qualifier = queryableName.getQualifier();
            if (qualifier.getMethodForm() == PLURAL) {
                parameterIndexProvider = new IncrementalParameterIndexProvider(INDEX_VARIABLE_NAME);

                snippetBuilder.addStatement("int $L = 1", INDEX_VARIABLE_NAME);

                entityVariableName = uniqueize("element");
                snippetBuilder
                        .beginControlFlow("for ($T $L : $L)",
                                          entity.getNativeType(), entityVariableName, parameter.getName());
            }
            else {
                parameterIndexProvider = new NumericParameterIndexProvider();

                entityVariableName = parameter.getName();
            }

            IdentifierMetadata identifier = entity.getIdentifier();
            snippetBuilder.add(
                    preparedStatementMethodMappingRenderer.render(identifier.getNativeType(),
                                                                  parameterIndexProvider.getParameterIndex(),
                                                                  CodeBlock.of("$L.$L()",
                                                                               entityVariableName, getter(identifier.getNativeElement())))
            );

            if (qualifier.getMethodForm() == PLURAL) {
                snippetBuilder.endControlFlow();
            }
        }
        else {
            snippetBuilder.add(repositoryMethodParameterMappingRenderer.render(method));
        }
        return snippetBuilder.build();
    }
}
