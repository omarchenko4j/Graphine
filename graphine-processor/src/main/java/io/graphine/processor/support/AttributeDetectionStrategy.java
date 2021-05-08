package io.graphine.processor.support;

import io.graphine.processor.util.EnumUtils;

import static io.graphine.processor.support.GraphineOptions.ATTRIBUTE_DETECTION_STRATEGY;
import static java.util.Objects.isNull;

/**
 * @author Oleg Marchenko
 */
public enum AttributeDetectionStrategy {
    ALL_FIELDS,
    ANNOTATED_FIELDS;

    public static boolean onlyAnnotatedFields() {
        AttributeDetectionStrategy attributeDetectionStrategy =
                ATTRIBUTE_DETECTION_STRATEGY.value(value -> EnumUtils.valueOf(value, ALL_FIELDS));
        return isNull(attributeDetectionStrategy) || ANNOTATED_FIELDS.equals(attributeDetectionStrategy);
    }
}
