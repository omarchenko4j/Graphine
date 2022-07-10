package io.graphine.test.repository;

import io.graphine.annotation.Repository;
import io.graphine.test.model.Film;
import io.graphine.test.model.Rating;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author Oleg Marchenko
 */
@Repository(Film.class)
public interface FilmRepository {
    Film findById(UUID id);
    Optional<Film> findByImdbId(String imdbId);
    Film findFirstByBudgetGreaterThanEqualOrderByBudgetAsc(long budget);
    Optional<Film> findFirstOrderByYearDesc();
    Iterable<Film> findAll();
    Iterable<Film> findAllByIdIn(UUID... ids);
    Collection<Film> findAllByYear(int year);
    Collection<Film> findAllByYearIsNot(int year);
    Collection<Film> findAllByBudgetBetween(long budget1, long budget2);
    Collection<Film> findAllByBudgetNotBetween(long budget1, long budget2);
    Collection<Film> findAllByBudgetLessThan(long budget);
    Collection<Film> findAllByBudgetLessThanEqual(long budget);
    Collection<Film> findAllByGrossGreaterThan(long gross);
    Collection<Film> findAllByGrossGreaterThanEqual(long gross);
    List<Film> findAllByTaglineIsEmpty();
    List<Film> findAllByTaglineIsNotEmpty();
    List<Film> findAllByTitleLike(String title);
    List<Film> findAllByTitleNotLike(String title);
    List<Film> findAllByTitleStartingWith(String title);
    List<Film> findAllByTitleEndingWith(String title);
    List<Film> findAllByTitleContaining(String title);
    List<Film> findAllByTitleNotContaining(String title);
    Set<Film> findAllByBudgetIsNull();
    Set<Film> findAllByGrossIsNotNull();
    Set<Film> findAllByWasReleasedIsTrue();
    Set<Film> findAllByWasReleasedIsFalse();
    Set<Film> findAllByYearIn(Collection<Integer> years);
    Set<Film> findAllByYearNotIn(int... years);
    Set<Film> findAllByBudgetGreaterThanEqualAndGrossGreaterThan(long budget, long gross);
    Set<Film> findAllByBudgetBetweenOrGrossBetween(long budget1, long budget2, long gross1, long gross2);
    Stream<Film> findAllByWasReleasedIsTrueAndGrossGreaterThanEqual(long gross);
    List<Film> findAllByYearBetweenOrderByYear(int year1, int year2);
    List<Film> findAllByYearLessThanEqualOrderByYearAsc(int year);
    List<Film> findAllByYearGreaterThanAndTaglineIsNotEmptyOrderByYearDesc(int year);
    Film findFirstByRating(Rating rating);
    List<Film> findAllByRatingIn(Collection<Rating> ratings);
    List<Film> findAllByRating_valueGreaterThanEqualAndRating_countGreaterThanEqual(float ratingValue,
                                                                                    long ratingCount);
    List<Film> findAllByRating_valueLessThanEqual(float ratingValue);
    List<Film> findAllByRating_valueGreaterThanEqualOrderByRating_count(float ratingValue);
    int countAll();
    long countAllByYear(int year);
    long countAllByRatingIn(Rating... ratings);
    Long countAllByGrossGreaterThanEqual(long gross);
    void save(Film film);
    void saveAll(Film... films);
    void saveAll(Iterable<Film> films);
    void saveAll(Collection<Film> films);
    void saveAll(List<Film> films);
    void saveAll(Set<Film> films);
    void update(Film film);
    void updateAll(Film... films);
    void updateAll(Iterable<Film> films);
    void updateAll(Collection<Film> films);
    void updateAll(List<Film> films);
    void updateAll(Set<Film> films);
    void delete(Film film);
    void deleteById(UUID id);
    void deleteAll(Film... films);
    void deleteAll(Iterable<Film> films);
    void deleteAll(Collection<Film> films);
    void deleteAll(List<Film> films);
    void deleteAll(Set<Film> films);
    void deleteAllByYearIn(Set<Integer> years);
    void deleteAllByRatingIn(Rating[] ratings);
}
