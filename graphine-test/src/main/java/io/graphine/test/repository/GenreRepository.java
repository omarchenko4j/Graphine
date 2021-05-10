package io.graphine.test.repository;

import io.graphine.core.GraphineRepository;
import io.graphine.core.annotation.Repository;
import io.graphine.test.model.Genre;

import java.util.Collection;

/**
 * @author Oleg Marchenko
 */
@Repository
public interface GenreRepository extends GraphineRepository<Genre> {
    void save(Genre genre);
    void saveAll(Collection<Genre> genres);
}
