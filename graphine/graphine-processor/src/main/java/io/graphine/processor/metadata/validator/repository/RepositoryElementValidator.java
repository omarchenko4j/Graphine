package io.graphine.processor.metadata.validator.repository;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import static io.graphine.processor.metadata.parser.RepositoryMethodNameParser.METHOD_NAME_PATTERN;
import static io.graphine.processor.support.EnvironmentContext.logger;
import static java.util.Objects.nonNull;
import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.lang.model.element.Modifier.DEFAULT;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.util.ElementFilter.methodsIn;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryElementValidator {
    public boolean validate(TypeElement repositoryElement) {
        boolean valid = true;

        if (repositoryElement.getKind() != INTERFACE) {
            valid = false;
            logger.error("Repository must be an interface", repositoryElement);
        }
        if (!repositoryElement.getModifiers().contains(PUBLIC)) {
            valid = false;
            logger.error("Repository interface must be public", repositoryElement);
        }

        List<ExecutableElement> methodElements = methodsIn(repositoryElement.getEnclosedElements());
        for (ExecutableElement methodElement : methodElements) {
            Set<Modifier> methodModifiers = methodElement.getModifiers();
            if (methodModifiers.contains(DEFAULT)) continue;

            String methodName = methodElement.getSimpleName().toString();

            Matcher methodNameMatcher = METHOD_NAME_PATTERN.matcher(methodName);
            if (methodNameMatcher.find()) {
                String conditionFragment = methodNameMatcher.group("condition");
                if (nonNull(conditionFragment) && conditionFragment.isEmpty()) {
                    valid = false;
                    logger.error("Method name contains a condition specifier (By...) but does not specify it",
                                 methodElement);
                }

                String sortingFragment = methodNameMatcher.group("sorting");
                if (nonNull(sortingFragment) && sortingFragment.isEmpty()) {
                    valid = false;
                    logger.error("Method name contains a sorting specifier (OrderBy...) but does not specify it",
                                 methodElement);
                }
            }
            else {
                valid = false;
                logger.error("Method name could not be recognized. " +
                                     "The following prefixes are supported: " +
                                     "find(First|All)(Distinct)(By), countAll(By), save(All), update(All), delete(All)(By)",
                             methodElement);
            }
        }

        return valid;
    }
}
