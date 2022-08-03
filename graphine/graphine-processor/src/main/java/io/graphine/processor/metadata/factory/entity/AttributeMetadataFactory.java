package io.graphine.processor.metadata.factory.entity;

import io.graphine.annotation.Attribute;
import io.graphine.annotation.AttributeOverride;
import io.graphine.annotation.AttributeOverrides;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.EmbeddedAttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.EmbeddedIdentifierAttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.IdentifierAttributeMetadata;
import io.graphine.processor.support.naming.pipeline.ColumnNamingPipeline;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static io.graphine.processor.metadata.model.entity.attribute.EmbeddedAttributeMetadata.isEmbedded;
import static io.graphine.processor.metadata.model.entity.attribute.EmbeddedIdentifierAttributeMetadata.isEmbeddedIdentifier;
import static io.graphine.processor.metadata.model.entity.attribute.IdentifierAttributeMetadata.isIdentifier;
import static io.graphine.processor.util.GraphineAnnotationUtils.getAttributeAnnotationMapperAttributeValue;
import static io.graphine.processor.util.StringUtils.isEmpty;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
public final class AttributeMetadataFactory {
    private final ColumnNamingPipeline columnNamingPipeline;

    public AttributeMetadataFactory(ColumnNamingPipeline columnNamingPipeline) {
        this.columnNamingPipeline = columnNamingPipeline;
    }

    public AttributeMetadata createAttribute(VariableElement fieldElement) {
        // Order of declaration is important!
        if (isEmbeddedIdentifier(fieldElement)) {
            return createEmbeddedIdentifierAttribute(fieldElement);
        }
        if (isIdentifier(fieldElement)) {
            return createIdentifierAttribute(fieldElement);
        }
        if (isEmbedded(fieldElement)) {
            return createEmbeddedAttribute(fieldElement);
        }
        return createSimpleAttribute(fieldElement);
    }

    private EmbeddedIdentifierAttributeMetadata createEmbeddedIdentifierAttribute(VariableElement fieldElement) {
        EmbeddedAttributeMetadata embeddedAttribute = createEmbeddedAttribute(fieldElement);
        return new EmbeddedIdentifierAttributeMetadata(fieldElement,
                                                       embeddedAttribute,
                                                       getMapper(fieldElement));
    }

    private IdentifierAttributeMetadata createIdentifierAttribute(VariableElement fieldElement) {
        return new IdentifierAttributeMetadata(fieldElement,
                                               getColumn(fieldElement),
                                               getMapper(fieldElement));
    }

    private EmbeddedAttributeMetadata createEmbeddedAttribute(VariableElement fieldElement) {
        Map<String, String> attributeNameToColumnNameMap = emptyMap();

        AttributeOverrides attributeOverrides =
                fieldElement.getAnnotation(AttributeOverrides.class);
        if (nonNull(attributeOverrides)) {
            attributeNameToColumnNameMap =
                    Arrays.stream(attributeOverrides.value())
                          .collect(Collectors.toMap(AttributeOverride::name,
                                                    attributeOverride -> attributeOverride.attribute().column()));
        }
        else {
            AttributeOverride attributeOverride =
                    fieldElement.getAnnotation(AttributeOverride.class);
            if (nonNull(attributeOverride)) {
                attributeNameToColumnNameMap = singletonMap(attributeOverride.name(),
                                                            attributeOverride.attribute().column());
            }
        }
        return new EmbeddedAttributeMetadata(fieldElement,
                                             attributeNameToColumnNameMap,
                                             getMapper(fieldElement));
    }

    private AttributeMetadata createSimpleAttribute(VariableElement fieldElement) {
        return new AttributeMetadata(fieldElement,
                                     getColumn(fieldElement),
                                     getMapper(fieldElement));
    }

    private String getColumn(VariableElement fieldElement) {
        String column = null;

        Attribute attribute = fieldElement.getAnnotation(Attribute.class);
        if (nonNull(attribute)) {
            column = attribute.column();
        }
        if (isEmpty(column)) {
            String attributeName = fieldElement.getSimpleName().toString();
            column = columnNamingPipeline.transform(attributeName);
        }
        return column;
    }

    private String getMapper(VariableElement fieldElement) {
        TypeElement attributeMapperElement = getAttributeAnnotationMapperAttributeValue(fieldElement);
        if (isNull(attributeMapperElement)) {
            return null;
        }
        return attributeMapperElement.getQualifiedName().toString();
    }
}
