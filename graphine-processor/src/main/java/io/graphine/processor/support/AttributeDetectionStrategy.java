package io.graphine.processor.support;

import io.graphine.processor.util.EnumUtils;

import static io.graphine.processor.GraphineOptions.ATTRIBUTE_DETECTION_STRATEGY;
import static java.util.Objects.isNull;

/**
 * @author Oleg Marchenko
 */
public enum AttributeDetectionStrategy {
    ALL_FIELDS,
    ANNOTATED_FIELDS;

    public static final String DEFAULT_ATTRIBUTE_DETECTION_STRATEGY = ALL_FIELDS.name();

    public static boolean onlyAnnotatedFields() {
        AttributeDetectionStrategy attributeDetectionStrategy =
                ATTRIBUTE_DETECTION_STRATEGY.value(value -> EnumUtils.valueOf(AttributeDetectionStrategy.class, value));
        return isNull(attributeDetectionStrategy) || ANNOTATED_FIELDS.equals(attributeDetectionStrategy);
    }
}
