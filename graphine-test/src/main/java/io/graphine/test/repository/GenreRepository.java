package io.graphine.test.repository;

import io.graphine.core.annotation.Repository;
import io.graphine.test.model.Genre;

import java.util.Collection;

/**
 * @author Oleg Marchenko
 */
@Repository(Genre.class)
public interface GenreRepository {
    void save(Genre genre);
    void saveAll(Collection<Genre> genres);
}
