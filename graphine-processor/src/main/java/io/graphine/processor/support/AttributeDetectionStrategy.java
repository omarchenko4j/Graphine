package io.graphine.processor.support;

import static io.graphine.processor.support.SupportedOptions.ATTRIBUTE_DETECTION_STRATEGY;
import static java.util.Objects.isNull;

/**
 * @author Oleg Marchenko
 */
public enum AttributeDetectionStrategy {
    ALL_FIELDS,
    ANNOTATED_FIELDS;

    public static AttributeDetectionStrategy safetyValueOf(String name) {
        try {
            return AttributeDetectionStrategy.valueOf(name);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static boolean onlyAnnotatedFields() {
        AttributeDetectionStrategy attributeDetectionStrategy =
                ATTRIBUTE_DETECTION_STRATEGY.value(AttributeDetectionStrategy::safetyValueOf);
        return isNull(attributeDetectionStrategy) || ANNOTATED_FIELDS.equals(attributeDetectionStrategy);
    }
}
