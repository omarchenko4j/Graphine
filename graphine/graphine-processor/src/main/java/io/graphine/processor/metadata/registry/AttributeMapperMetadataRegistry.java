package io.graphine.processor.metadata.registry;

import io.graphine.processor.metadata.model.entity.attribute.AttributeMapperMetadata;

import java.util.Collection;
import java.util.Map;

import static java.util.Collections.unmodifiableCollection;

/**
 * @author Oleg Marchenko
 */
public final class AttributeMapperMetadataRegistry {
    private final Map<String, AttributeMapperMetadata> attributeMapperRegistry;

    public AttributeMapperMetadataRegistry(Map<String, AttributeMapperMetadata> attributeMapperRegistry) {
        this.attributeMapperRegistry = attributeMapperRegistry;
    }

    public Collection<AttributeMapperMetadata> getAttributeMappers() {
        return unmodifiableCollection(attributeMapperRegistry.values());
    }

    public AttributeMapperMetadata getAttributeMapper(String qualifiedName) {
        return attributeMapperRegistry.get(qualifiedName);
    }

    public boolean containsAttributeMapper(String qualifiedName) {
        return attributeMapperRegistry.containsKey(qualifiedName);
    }
}
