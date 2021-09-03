package io.graphine.processor.metadata.model.entity;

import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.support.element.NativeTypeElement;

import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableCollection;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * @author Oleg Marchenko
 */
public class EmbeddableEntityMetadata extends NativeTypeElement {
    private final Map<String, AttributeMetadata> attributes;

    public EmbeddableEntityMetadata(TypeElement element,
                                    List<AttributeMetadata> attributes) {
        super(element);
        this.attributes = attributes
                .stream()
                .collect(toMap(AttributeMetadata::getName, identity(), (a1, a2) -> a1, LinkedHashMap::new));
    }

    public AttributeMetadata getAttribute(String attributeName) {
        return attributes.get(attributeName);
    }

    public Collection<AttributeMetadata> getAttributes() {
        return unmodifiableCollection(attributes.values());
    }
}
