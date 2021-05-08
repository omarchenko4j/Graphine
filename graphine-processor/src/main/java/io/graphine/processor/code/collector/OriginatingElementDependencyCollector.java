package io.graphine.processor.code.collector;

import io.graphine.processor.metadata.model.repository.RepositoryMetadata;

import javax.lang.model.element.Element;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Oleg Marchenko
 */
public final class OriginatingElementDependencyCollector {
    public Collection<Element> collect(RepositoryMetadata repository) {
        return Arrays.asList(repository.getNativeElement(),
                             // Entity is a dependency of the repository implementation.
                             // It positively affects on incremental build!
                             repository.getEntity().getNativeElement());
    }
}
