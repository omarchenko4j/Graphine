package io.graphine.processor.metadata.model.entity;

import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.IdentifierAttributeMetadata;
import io.graphine.processor.support.element.NativeTypeElement;

import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.graphine.processor.util.StringUtils.getIfNotEmpty;
import static java.util.Collections.unmodifiableList;

/**
 * @author Oleg Marchenko
 */
public class EntityMetadata extends NativeTypeElement {
    private final String schema;
    private final String table;
    private final IdentifierAttributeMetadata identifier;
    private final List<AttributeMetadata> attributes;
    private final List<AttributeMetadata> unidentifiedAttributes;
    private final Map<String, AttributeMetadata> attributeRegistry;

    public EntityMetadata(TypeElement element,
                          String schema,
                          String table,
                          IdentifierAttributeMetadata identifier,
                          List<AttributeMetadata> attributes) {
        super(element);
        this.schema = schema;
        this.table = table;
        this.identifier = identifier;
        this.attributes = attributes;
        this.unidentifiedAttributes =
                attributes
                        .stream()
                        .filter(attribute -> !(attribute instanceof IdentifierAttributeMetadata))
                        .collect(Collectors.toList());
        this.attributeRegistry =
                attributes
                        .stream()
                        .collect(Collectors.toMap(AttributeMetadata::getName, Function.identity()));
    }

    public String getSchema() {
        return schema;
    }

    public String getTable() {
        return table;
    }

    public IdentifierAttributeMetadata getIdentifier() {
        return identifier;
    }

    public AttributeMetadata getAttribute(String attributeName) {
        return attributeRegistry.get(attributeName);
    }

    public List<AttributeMetadata> getUnidentifiedAttributes() {
        return unmodifiableList(unidentifiedAttributes);
    }

    public List<AttributeMetadata> getAttributes() {
        return unmodifiableList(attributes);
    }

    @Override
    public String toString() {
        return qualifiedName + " [" + getIfNotEmpty(schema, () -> schema + '.') + table + ']';
    }
}
