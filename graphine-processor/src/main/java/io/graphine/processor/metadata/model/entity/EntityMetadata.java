package io.graphine.processor.metadata.model.entity;

import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.IdentifierAttributeMetadata;
import io.graphine.processor.support.element.NativeTypeElement;

import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.graphine.processor.util.StringUtils.getIfNotEmpty;
import static java.util.Collections.unmodifiableCollection;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableList;

/**
 * @author Oleg Marchenko
 */
public class EntityMetadata extends NativeTypeElement {
    private final String schema;
    private final String table;
    private final IdentifierAttributeMetadata identifier;
    private final Map<String, AttributeMetadata> attributes;

    public EntityMetadata(TypeElement element,
                          String schema,
                          String table,
                          IdentifierAttributeMetadata identifier,
                          List<AttributeMetadata> attributes) {
        super(element);
        this.schema = schema;
        this.table = table;
        this.identifier = identifier;
        this.attributes = attributes
                .stream()
                .collect(toMap(AttributeMetadata::getName, identity(), (a1, a2) -> a1, LinkedHashMap::new));
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
        return attributes.get(attributeName);
    }

    public Collection<AttributeMetadata> getAttributes() {
        return unmodifiableCollection(attributes.values());
    }

    public Collection<AttributeMetadata> getAttributes(boolean excludeIdentifier) {
        if (excludeIdentifier) {
            return this.attributes.values()
                                  .stream()
                                  .filter(attribute -> !attribute.getName().equals(identifier.getName()))
                                  .collect(toUnmodifiableList());
        }
        return getAttributes();
    }

    @Override
    public String toString() {
        return qualifiedName + " [" + getIfNotEmpty(schema, () -> schema + '.') + table + ']';
    }
}
