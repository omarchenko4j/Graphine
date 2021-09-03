package io.graphine.processor.metadata.model.repository.method.name.fragment;

import io.graphine.processor.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;

/**
 * @author Oleg Marchenko
 */
public final class AttributeChain {
    private static final String CHAIN_SEPARATOR = "_";

    private final List<String> attributeNames;

    private AttributeChain(List<String> attributeNames) {
        this.attributeNames = attributeNames;
    }

    public List<String> getAttributeNames() {
        return unmodifiableList(attributeNames);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(" -> ");
        attributeNames.forEach(joiner::add);
        return joiner.toString();
    }

    public static AttributeChain parse(String str) {
        List<String> attributeNames =
                Arrays.stream(str.split(CHAIN_SEPARATOR))
                      .map(StringUtils::uncapitalize)
                      .collect(Collectors.toList());
        return new AttributeChain(attributeNames);
    }
}
