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
            Pattern.compile("^(?<qualifier>(find|count|save|update|delete)(All)?(Distinct)?)(By(?<condition>.*?))?(OrderBy(?<sorting>.*?))?$");

    public QueryableMethodName parse(ExecutableElement methodElement) {
        String methodName = methodElement.getSimpleName().toString();

        QualifierFragment qualifier = null;
        ConditionFragment condition = null;
        SortingFragment sorting = null;

        Matcher methodNameMatcher = METHOD_NAME_PATTERN.matcher(methodName);
        if (methodNameMatcher.find()) {
            String qualifierFragment = methodNameMatcher.group("qualifier");
            qualifier = new QualifierFragment(qualifierFragment);

            String conditionFragment = methodNameMatcher.group("condition");
            if (isNotEmpty(conditionFragment)) {
                condition = new ConditionFragment(conditionFragment);
            }

            String sortingFragment = methodNameMatcher.group("sorting");
            if (isNotEmpty(sortingFragment)) {
                sorting = new SortingFragment(sortingFragment);
            }
        }
        else {
            messager.printMessage(Kind.ERROR,
                                  "Method name could not be recognized. " +
                                          "The following prefixes are supported: " +
                                          "find(All)By, countAll(By), save(All), update(All), delete(All)(By)",
                                  methodElement);
        }
        return new QueryableMethodName(qualifier, condition, sorting);
    }
}
