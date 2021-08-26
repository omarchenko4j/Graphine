package io.graphine.processor.code.renderer;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.parameter.index_provider.IncrementalParameterIndexProvider;
import io.graphine.processor.code.renderer.parameter.index_provider.NumericParameterIndexProvider;
import io.graphine.processor.code.renderer.parameter.index_provider.ParameterIndexProvider;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment.AndPredicate;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment.OperatorType;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment.OrPredicate;
import io.graphine.processor.metadata.model.repository.method.parameter.ParameterMetadata;

import javax.lang.model.type.TypeMirror;
import java.util.List;

import static io.graphine.processor.code.renderer.parameter.index_provider.IncrementalParameterIndexProvider.INDEX_VARIABLE_NAME;
import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryMethodParameterMappingRenderer {
    private final PreparedStatementMethodMappingRenderer preparedStatementMethodMappingRenderer;

    public RepositoryMethodParameterMappingRenderer(
            PreparedStatementMethodMappingRenderer preparedStatementMethodMappingRenderer) {
        this.preparedStatementMethodMappingRenderer = preparedStatementMethodMappingRenderer;
    }

    public CodeBlock render(MethodMetadata method) {
        List<ParameterMetadata> parameters = method.getParameters();
        if (parameters.isEmpty()) {
            return CodeBlock.builder().build();
        }

        CodeBlock.Builder snippetBuilder = CodeBlock.builder();

        ParameterIndexProvider parameterIndexProvider;
        if (method.getDeferredParameters().isEmpty()) {
            parameterIndexProvider = new NumericParameterIndexProvider();
        }
        else {
            parameterIndexProvider = new IncrementalParameterIndexProvider(INDEX_VARIABLE_NAME);

            snippetBuilder.addStatement("int $L = 1", INDEX_VARIABLE_NAME);
        }

        QueryableMethodName queryableName = method.getQueryableName();
        ConditionFragment condition = queryableName.getCondition();
        if (nonNull(condition)) {
            int parameterIndex = 0;

            List<OrPredicate> orPredicates = condition.getOrPredicates();
            for (OrPredicate orPredicate : orPredicates) {
                List<AndPredicate> andPredicates = orPredicate.getAndPredicates();
                for (AndPredicate andPredicate : andPredicates) {
                    OperatorType operator = andPredicate.getOperator();

                    for (int i = parameterIndex; i < parameterIndex + operator.getParameterCount(); i++) {
                        ParameterMetadata parameter = parameters.get(i);

                        String index = parameterIndexProvider.getParameterIndex();

                        String parameterName = parameter.getName();
                        switch (operator) {
                            case STARTING_WITH:
                                parameterName += " + '%'";
                                break;
                            case ENDING_WITH:
                                parameterName = "'%' + " + parameterName;
                                break;
                            case CONTAINING:
                            case NOT_CONTAINING:
                                parameterName = "'%' + " + parameterName + " + '%'";
                                break;
                        }

                        TypeMirror parameterType = parameter.getNativeType();

                        snippetBuilder.add(preparedStatementMethodMappingRenderer.render(parameterType, index, parameterName));
                    }

                    parameterIndex += operator.getParameterCount();
                }
            }
        }

        return snippetBuilder.build();
    }
}
