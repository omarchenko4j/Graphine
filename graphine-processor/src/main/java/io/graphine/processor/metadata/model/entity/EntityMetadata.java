package io.graphine.processor.metadata.model.entity;

import io.graphine.processor.metadata.model.NativeElementMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.IdentifierMetadata;

import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.graphine.processor.util.StringUtils.getIfNotEmpty;
import static io.graphine.processor.util.StringUtils.nullToEmpty;
import static java.util.Collections.unmodifiableCollection;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * @author Oleg Marchenko
 */
public class EntityMetadata extends NativeElementMetadata<TypeElement> {
    private final String schema;
    private final String table;
    private final IdentifierMetadata identifier;
    private final Map<String, AttributeMetadata> attributes;

    public EntityMetadata(TypeElement element,
                          String schema,
                          String table,
                          IdentifierMetadata identifier,
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

    public IdentifierMetadata getIdentifier() {
        return identifier;
    }

    public AttributeMetadata getAttribute(String attributeName) {
        return attributes.get(attributeName);
    }

    public Collection<AttributeMetadata> getAttributes() {
        return unmodifiableCollection(attributes.values());
    }

    @Override
    public String toString() {
        return qualifiedName + " [" + getIfNotEmpty(nullToEmpty(schema), () -> schema + '.') + table + ']';
    }
}
