package io.graphine.processor.metadata;

import javax.lang.model.element.TypeElement;
import java.util.List;

import static io.graphine.processor.util.StringUtils.getIfNotEmpty;
import static io.graphine.processor.util.StringUtils.nullToEmpty;
import static java.util.Collections.unmodifiableList;

/**
 * @author Oleg Marchenko
 */
public class EntityMetadata extends NativeElementMetadata<TypeElement> {
    private final String schema;
    private final String table;
    private final IdentifierMetadata identifier;
    private final List<AttributeMetadata> attributes;

    public EntityMetadata(TypeElement element,
                          String schema,
                          String table,
                          IdentifierMetadata identifier,
                          List<AttributeMetadata> attributes) {
        super(element);
        this.schema = schema;
        this.table = table;
        this.identifier = identifier;
        this.attributes = attributes;
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

    public List<AttributeMetadata> getAttributes() {
        return unmodifiableList(attributes);
    }

    @Override
    public String toString() {
        return qualifiedName + " [" + getIfNotEmpty(nullToEmpty(schema), () -> schema + '.') + table + ']';
    }
}
