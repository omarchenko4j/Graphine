package io.graphine.processor.metadata.parser;

import io.graphine.processor.metadata.model.repository.method.name.QueryableMethodName;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.QualifierFragment;
import io.graphine.processor.metadata.model.repository.method.name.fragment.SortingFragment;

import javax.lang.model.element.ExecutableElement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.graphine.processor.support.EnvironmentContext.messager;
import static io.graphine.processor.util.StringUtils.isNotEmpty;
import static javax.tools.Diagnostic.Kind;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryMethodNameParser {
    public static final Pattern METHOD_NAME_PATTERN =
            Pattern.compile("^((find|count|delete)(All)?(By)?|(save|update)(All)?)(.*)$");

    public QueryableMethodName parse(ExecutableElement methodElement) {
        QualifierFragment qualifier = null;
        ConditionFragment condition = null;
        SortingFragment sorting = null;

        String methodName = methodElement.getSimpleName().toString();
        Matcher methodNameMatcher = METHOD_NAME_PATTERN.matcher(methodName);
        if (methodNameMatcher.find()) {
            String qualifierFragment = methodNameMatcher.group(1);
            qualifier = new QualifierFragment(qualifierFragment);

            String bodyFragment = methodNameMatcher.group(7);
            if (isNotEmpty(bodyFragment)) {
                String[] fragments = bodyFragment.split(SortingFragment.KEYWORD);
                if (fragments.length > 0) {
                    String conditionFragment = fragments[0];
                    if (isNotEmpty(conditionFragment)) {
                        condition = new ConditionFragment(conditionFragment);
                    }
                }
                if (fragments.length > 1) {
                    String sortingFragment = fragments[1];
                    sorting = new SortingFragment(sortingFragment);
                }
                if (fragments.length > 2) {
                    messager.printMessage(Kind.ERROR, "Multiple 'OrderBy' qualifiers are not allowed", methodElement);
                }
            }
        }
        else {
            messager.printMessage(Kind.ERROR, "Method name prefix could not be recognized. " +
                                              "Use the following options: " +
                                              "find(All)By, countAll(By), save(All), update(All), delete(All)(By)", methodElement);
        }
        return new QueryableMethodName(qualifier, condition, sorting);
    }
}
