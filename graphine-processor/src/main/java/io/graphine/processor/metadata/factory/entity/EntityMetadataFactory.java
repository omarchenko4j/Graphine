package io.graphine.processor.metadata.factory.entity;

import io.graphine.core.annotation.Entity;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.IdentifierMetadata;
import io.graphine.processor.support.AttributeDetectionStrategy;
import io.graphine.processor.support.naming.pipeline.TableNamingPipeline;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.graphine.processor.GraphineOptions.DEFAULT_SCHEME;
import static io.graphine.processor.util.StringUtils.isEmpty;
import static javax.lang.model.util.ElementFilter.fieldsIn;

/**
 * @author Oleg Marchenko
 */
public final class EntityMetadataFactory {
    private final TableNamingPipeline tableNamingPipeline;
    private final AttributeMetadataFactory attributeMetadataFactory;

    public EntityMetadataFactory(TableNamingPipeline tableNamingPipeline,
                                 AttributeMetadataFactory attributeMetadataFactory) {
        this.tableNamingPipeline = tableNamingPipeline;
        this.attributeMetadataFactory = attributeMetadataFactory;
    }

    public EntityMetadata createEntity(TypeElement entityElement) {
        Entity entity = entityElement.getAnnotation(Entity.class);

        String schema = entity.schema();
        if (isEmpty(schema)) {
            schema = DEFAULT_SCHEME.value();
        }

        String table = entity.table();
        if (isEmpty(table)) {
            String entityName = entityElement.getSimpleName().toString();
            table = tableNamingPipeline.transform(entityName);
        }

        Stream<VariableElement> fields = fieldsIn(entityElement.getEnclosedElements()).stream();

        if (AttributeDetectionStrategy.onlyAnnotatedFields()) {
            fields = fields.filter(AttributeMetadata::isAttribute);
        }

        List<AttributeMetadata> attributes = fields
                .map(attributeMetadataFactory::createAttribute)
                .collect(Collectors.toList());

        IdentifierMetadata identifier = (IdentifierMetadata) attributes
                .stream()
                .filter(attribute -> attribute instanceof IdentifierMetadata)
                .findFirst()
                .orElse(null);

        return new EntityMetadata(entityElement, schema, table, identifier, attributes);
    }
}
