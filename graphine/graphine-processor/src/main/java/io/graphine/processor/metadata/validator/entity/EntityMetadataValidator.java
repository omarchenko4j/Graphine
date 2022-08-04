package io.graphine.processor.metadata.validator.entity;

import io.graphine.annotation.Attribute;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMapperMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.registry.AttributeMapperMetadataRegistry;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.VariableElement;
import java.util.Collection;
import java.util.List;

import static io.graphine.processor.support.EnvironmentContext.logger;
import static io.graphine.processor.util.AnnotationUtils.findAnnotation;
import static io.graphine.processor.util.AnnotationUtils.findAnnotationValue;
import static io.graphine.processor.util.StringUtils.isEmpty;
import static java.util.Objects.isNull;

/**
 * @author Oleg Marchenko
 */
public final class EntityMetadataValidator {
    private final AttributeMapperMetadataRegistry attributeMapperMetadataRegistry;

    public EntityMetadataValidator(AttributeMapperMetadataRegistry attributeMapperMetadataRegistry) {
        this.attributeMapperMetadataRegistry = attributeMapperMetadataRegistry;
    }

    public boolean validate(Collection<EntityMetadata> entities) {
        boolean valid = true;
        for (EntityMetadata entity : entities) {
            if (!validate(entity)) {
                valid = false;
            }
        }
        return valid;
    }

    public boolean validate(EntityMetadata entity) {
        List<AttributeMetadata> attributes = entity.getAttributes();
        for (AttributeMetadata attribute : attributes) {
            String attributeMapperName = attribute.getMapper();
            if (isEmpty(attributeMapperName)) {
                continue;
            }

            if (!attributeMapperMetadataRegistry.containsAttributeMapper(attributeMapperName)) {
                VariableElement attributeElement = attribute.getNativeElement();
                AnnotationMirror attributeAnnotation =
                        findAnnotation(attributeElement, Attribute.class.getName()).get();
                AnnotationValue attributeAnnotationValue =
                        findAnnotationValue(attributeAnnotation, "mapper").get();
                logger.error("Class '" + attributeMapperName + "' must be annotated with @AttributeMapper",
                             attributeElement, attributeAnnotation, attributeAnnotationValue);
                return false;
            }

            AttributeMapperMetadata attributeMapper =
                    attributeMapperMetadataRegistry.getAttributeMapper(attributeMapperName);
            if (isNull(attributeMapper)) {
                return false; // Abort validation if the attribute mapper has no metadata.
            }
        }
        return true;
    }
}
