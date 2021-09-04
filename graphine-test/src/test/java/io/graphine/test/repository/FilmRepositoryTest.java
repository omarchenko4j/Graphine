package io.graphine.test.repository;

import io.graphine.core.NonUniqueResultException;
import io.graphine.test.model.Film;
import io.graphine.test.model.Rating;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static io.graphine.test.util.DataSourceProvider.DATA_SOURCE;
import static io.graphine.test.util.DataSourceProvider.PROXY_DATA_SOURCE;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/**
 * @author Oleg Marchenko
 */
public class FilmRepositoryTest {

    @BeforeAll
    @SneakyThrows
    public static void createTable() {
        @Cleanup Connection connection = DATA_SOURCE.getConnection();
        @Cleanup Statement statement = connection.createStatement();
        statement.executeQuery("CREATE TABLE public.film(" +
                               "id BIGSERIAL NOT NULL PRIMARY KEY, " +
                               "imdb_id TEXT NOT NULL, " +
                               "title TEXT NOT NULL, " +
                               "year INTEGER NOT NULL, " +
                               "rating_value FLOAT NOT NULL, " +
                               "rating_count BIGINT NOT NULL, " +
                               "budget BIGINT, " +
                               "gross BIGINT, " +
                               "tagline TEXT, " +
                               "was_released BOOLEAN NOT NULL)");
    }

    @AfterEach
    @SneakyThrows
    public void cleanTable() {
        @Cleanup Connection connection = DATA_SOURCE.getConnection();
        @Cleanup Statement statement = connection.createStatement();
        statement.executeQuery("TRUNCATE TABLE public.film");
    }

    @AfterAll
    @SneakyThrows
    public static void dropTable() {
        @Cleanup Connection connection = DATA_SOURCE.getConnection();
        @Cleanup Statement statement = connection.createStatement();
        statement.executeQuery("DROP TABLE public.film");
    }

    private final FilmRepository filmRepository = new GraphineFilmRepository(PROXY_DATA_SOURCE);

    @Test
    public void testFindByIdMethodReturnResult() {
        Film film = MarvelFilms.ironMan();
        insertFilm(film);

        Film foundFilm = filmRepository.findById(film.getId());
        Assertions.assertNotNull(foundFilm);
        Assertions.assertEquals(film, foundFilm);
    }

    @Test
    public void testFindByIdMethodReturnNull() {
        Film film = filmRepository.findById(999);
        Assertions.assertNull(film);
    }

    @Test
    public void testFindByImdbIdMethodReturnNonEmptyResult() {
        Film film = MarvelFilms.ironMan();
        insertFilm(film);

        Optional<Film> foundFilm = filmRepository.findByImdbId(film.getImdbId());
        Assertions.assertNotNull(foundFilm);
        Assertions.assertTrue(foundFilm.isPresent());
        Assertions.assertEquals(film, foundFilm.get());
    }

    @Test
    public void testFindByImdbIdMethodReturnEmptyResult() {
        Optional<Film> film = filmRepository.findByImdbId("tt9999999");
        Assertions.assertNotNull(film);
        Assertions.assertFalse(film.isPresent());
    }

    @Test
    public void testFindByImdbIdMethodThrownNonUniqueResultException() {
        Film film1 = MarvelFilms.ironMan();
        film1.setImdbId("tt0000000");

        Film film2 = MarvelFilms.ironMan2();
        film2.setImdbId("tt0000000");

        insertFilms(List.of(film1, film2));

        Assertions.assertThrows(NonUniqueResultException.class,
                                () -> filmRepository.findByImdbId("tt0000000"));
    }

    @Test
    public void testFindFirstByBudgetGreaterThanEqualOrderByBudgetAscMethodReturnFirstResult() {
        Film film = MarvelFilms.ironMan();
        Collection<Film> films = List.of(MarvelFilms.incredibleHulk(), film, MarvelFilms.ironMan2());
        insertFilms(films);

        Film foundFilm = filmRepository.findFirstByBudgetGreaterThanEqualOrderByBudgetAsc(film.getBudget());
        Assertions.assertNotNull(foundFilm);
        Assertions.assertEquals(film, foundFilm);
    }

    @Test
    public void testFindFirstByBudgetGreaterThanEqualOrderByBudgetAscMethodReturnNull() {
        Film film = filmRepository.findFirstByBudgetGreaterThanEqualOrderByBudgetAsc(1_000_000_000);
        Assertions.assertNull(film);
    }

    @Test
    public void testFindFirstOrderByYearDescMethodReturnFirstResult() {
        Film film = MarvelFilms.ironMan2();
        Collection<Film> films = List.of(MarvelFilms.ironMan(), film);
        insertFilms(films);

        Optional<Film> foundFilm = filmRepository.findFirstOrderByYearDesc();
        Assertions.assertNotNull(foundFilm);
        Assertions.assertTrue(foundFilm.isPresent());
        Assertions.assertEquals(film, foundFilm.get());
    }

    @Test
    public void testFindFirstOrderByYearDescMethodReturnEmptyResult() {
        Optional<Film> film = filmRepository.findFirstOrderByYearDesc();
        Assertions.assertNotNull(film);
        Assertions.assertFalse(film.isPresent());
    }

    @Test
    public void testFindAllMethodReturnAllResults() {
        Collection<Film> films = List.of(MarvelFilms.ironMan(), MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        Iterable<Film> foundFilms = filmRepository.findAll();
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), stream(foundFilms.spliterator(), false).count());
        Assertions.assertTrue(films.containsAll(stream(foundFilms.spliterator(), false).collect(toList())));
    }

    @Test
    public void testFindAllMethodReturnEmptyResult() {
        Iterable<Film> films = filmRepository.findAll();
        Assertions.assertNotNull(films);
        Assertions.assertEquals(0, stream(films.spliterator(), false).count());
    }

    @Test
    public void testFindAllByYearMethodReturnResults() {
        Collection<Film> films = List.of(MarvelFilms.incredibleHulk(), MarvelFilms.ironMan());
        insertFilms(films);

        Collection<Film> foundFilms = filmRepository.findAllByYear(2008);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByYearMethodNotReturnResults() {
        Collection<Film> films = filmRepository.findAllByYear(2000);
        Assertions.assertNotNull(films);
        Assertions.assertEquals(0, films.size());
    }

    @Test
    public void testFindAllByYearIsNotMethodReturnResults() {
        Collection<Film> films = List.of(MarvelFilms.incredibleHulk(), MarvelFilms.ironMan());
        insertFilms(films);

        Collection<Film> foundFilms = filmRepository.findAllByYearIsNot(2010);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByBudgetBetweenMethodReturnResults() {
        Collection<Film> films = List.of(MarvelFilms.incredibleHulk(), MarvelFilms.ironMan());
        insertFilms(films);

        Collection<Film> foundFilms = filmRepository.findAllByBudgetBetween(140_000_000, 150_000_000);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByBudgetNotBetweenMethodReturnResults() {
        Collection<Film> films = List.of(MarvelFilms.incredibleHulk(), MarvelFilms.ironMan());
        insertFilms(films);

        Collection<Film> foundFilms = filmRepository.findAllByBudgetNotBetween(200_000_000, 300_000_000);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByBudgetLessThanMethodReturnResults() {
        Collection<Film> films = List.of(MarvelFilms.incredibleHulk(), MarvelFilms.ironMan());
        insertFilms(films);

        Collection<Film> foundFilms = filmRepository.findAllByBudgetLessThan(200_000_000);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByBudgetLessThanEqualMethodReturnResults() {
        Collection<Film> films = List.of(MarvelFilms.incredibleHulk(), MarvelFilms.ironMan(), MarvelFilms.ironMan2());
        insertFilms(films);

        Collection<Film> foundFilms = filmRepository.findAllByBudgetLessThanEqual(200_000_000);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByGrossGreaterThanMethodReturnResults() {
        Collection<Film> films = List.of(MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        Collection<Film> foundFilms = filmRepository.findAllByGrossGreaterThan(600_000_000);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByGrossGreaterThanEqualMethodReturnResults() {
        Collection<Film> films = List.of(MarvelFilms.ironMan3());
        insertFilms(films);

        Collection<Film> foundFilms = filmRepository.findAllByGrossGreaterThanEqual(1_000_000_000);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByTaglineIsEmptyMethodReturnResults() {
        Film film1 = MarvelFilms.ironMan();
        film1.setTagline("");

        Film film2 = MarvelFilms.ironMan2();
        film2.setTagline("");

        List<Film> films = List.of(film1, film2);
        insertFilms(films);

        List<Film> foundFilms = filmRepository.findAllByTaglineIsEmpty();
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByTaglineIsNotEmptyMethodReturnResults() {
        List<Film> films = List.of(MarvelFilms.ironMan(), MarvelFilms.ironMan2());
        insertFilms(films);

        List<Film> foundFilms = filmRepository.findAllByTaglineIsNotEmpty();
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByTitleLikeMethodReturnResults() {
        List<Film> films = List.of(MarvelFilms.ironMan(), MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        List<Film> foundFilms = filmRepository.findAllByTitleLike("Iron Man%");
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByTitleNotLikeMethodReturnResults() {
        List<Film> films = List.of(MarvelFilms.ironMan(), MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        List<Film> foundFilms = filmRepository.findAllByTitleNotLike("Captain America%");
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByTitleStartingWithMethodReturnResults() {
        List<Film> films = List.of(MarvelFilms.ironMan(), MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        List<Film> foundFilms = filmRepository.findAllByTitleStartingWith("Iron Man");
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByTitleEndingWithMethodReturnResults() {
        Film film = MarvelFilms.ironMan();
        insertFilm(film);

        List<Film> foundFilms = filmRepository.findAllByTitleEndingWith("Man");
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(1, foundFilms.size());
        Assertions.assertEquals(film, foundFilms.get(0));
    }

    @Test
    public void testFindAllByTitleContainingMethodReturnResults() {
        List<Film> films = List.of(MarvelFilms.ironMan(), MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        List<Film> foundFilms = filmRepository.findAllByTitleContaining("Man");
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByTitleNotContainingMethodReturnResults() {
        List<Film> films = List.of(MarvelFilms.ironMan(), MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        List<Film> foundFilms = filmRepository.findAllByTitleNotContaining("Avenger");
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByBudgetIsNullMethodReturnResults() {
        Film film1 = MarvelFilms.ironMan();
        film1.setBudget(null);

        Film film2 = MarvelFilms.ironMan2();
        film2.setBudget(null);

        List<Film> films = List.of(film1, film2);
        insertFilms(films);

        Set<Film> foundFilms = filmRepository.findAllByBudgetIsNull();
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByGrossIsNotNullMethodReturnResults() {
        List<Film> films = List.of(MarvelFilms.ironMan(), MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        Set<Film> foundFilms = filmRepository.findAllByGrossIsNotNull();
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByWasReleasedIsTrueMethodReturnResults() {
        List<Film> films = List.of(MarvelFilms.ironMan(), MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        Set<Film> foundFilms = filmRepository.findAllByWasReleasedIsTrue();
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByWasReleasedIsFalseMethodReturnResults() {
        Film film1 = MarvelFilms.ironMan();
        film1.setWasReleased(false);

        Film film2 = MarvelFilms.ironMan2();
        film2.setWasReleased(false);

        List<Film> films = List.of(film1, film2);
        insertFilms(films);

        Set<Film> foundFilms = filmRepository.findAllByWasReleasedIsFalse();
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByYearInMethodReturnResults() {
        List<Film> films = List.of(MarvelFilms.incredibleHulk(),
                                   MarvelFilms.ironMan(),
                                   MarvelFilms.ironMan2(),
                                   MarvelFilms.ironMan3());
        insertFilms(films);

        Set<Film> foundFilms = filmRepository.findAllByYearIn(List.of(2008, 2010, 2013));
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByYearNotInMethodReturnResults() {
        List<Film> films = List.of(MarvelFilms.incredibleHulk(),
                                   MarvelFilms.ironMan(),
                                   MarvelFilms.ironMan2(),
                                   MarvelFilms.ironMan3());
        insertFilms(films);

        Set<Film> foundFilms = filmRepository.findAllByYearNotIn(2000, 2001, 2002);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByBudgetGreaterThanEqualAndGrossGreaterThanMethodReturnResults() {
        List<Film> films = List.of(MarvelFilms.ironMan3());
        insertFilms(films);

        Set<Film> foundFilms = filmRepository.findAllByBudgetGreaterThanEqualAndGrossGreaterThan(200_000_000,
                                                                                                 1_000_000_000);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByBudgetBetweenOrGrossBetweenMethodReturnResults() {
        List<Film> films = List.of(MarvelFilms.ironMan(), MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        Set<Film> foundFilms = filmRepository.findAllByBudgetBetweenOrGrossBetween(100_000_000, 150_000_000,
                                                                                   600_000_000, 1_500_000_000);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByWasReleasedIsTrueAndGrossGreaterThanEqualMethodReturnResults() {
        List<Film> films = List.of(MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        Stream<Film> foundFilms = filmRepository.findAllByWasReleasedIsTrueAndGrossGreaterThanEqual(200_000_000);
        Assertions.assertNotNull(foundFilms);

        List<Film> foundFilmList = foundFilms.collect(toList());
        Assertions.assertEquals(films.size(), foundFilmList.size());
        Assertions.assertTrue(films.containsAll(foundFilmList));
    }

    @Test
    public void testFindAllByWasReleasedIsTrueAndGrossGreaterThanEqualMethodEmptyResult() {
        Stream<Film> films = filmRepository.findAllByWasReleasedIsTrueAndGrossGreaterThanEqual(1_000_000_000);
        Assertions.assertNotNull(films);
        Assertions.assertEquals(0, films.count());
    }

    @Test
    public void testFindAllByYearBetweenOrderByYearMethodReturnResults() {
        List<Film> films = List.of(MarvelFilms.ironMan(), MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        List<Film> foundFilms = filmRepository.findAllByYearBetweenOrderByYear(2008, 2013);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
        Assertions.assertEquals(foundFilms.get(0).getYear(), 2008);
        Assertions.assertEquals(foundFilms.get(1).getYear(), 2010);
        Assertions.assertEquals(foundFilms.get(2).getYear(), 2013);
    }

    @Test
    public void testFindAllByYearLessThanEqualOrderByYearAscMethodReturnResults() {
        List<Film> films = List.of(MarvelFilms.ironMan(), MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        List<Film> foundFilms = filmRepository.findAllByYearLessThanEqualOrderByYearAsc(2013);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
        Assertions.assertEquals(foundFilms.get(0).getYear(), 2008);
        Assertions.assertEquals(foundFilms.get(1).getYear(), 2010);
        Assertions.assertEquals(foundFilms.get(2).getYear(), 2013);
    }

    @Test
    public void testFindAllByYearGreaterThanAndTaglineIsNotEmptyOrderByYearDescMethodReturnResults() {
        List<Film> films = List.of(MarvelFilms.ironMan(), MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        List<Film> foundFilms = filmRepository.findAllByYearGreaterThanAndTaglineIsNotEmptyOrderByYearDesc(2005);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
        Assertions.assertEquals(foundFilms.get(0).getYear(), 2013);
        Assertions.assertEquals(foundFilms.get(1).getYear(), 2010);
        Assertions.assertEquals(foundFilms.get(2).getYear(), 2008);
    }

    @Test
    public void testFindAllByRatingMethodReturnResults() {
        Film film = MarvelFilms.ironMan();
        List<Film> films = List.of(film, MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        Film foundFilm = filmRepository.findFirstByRating(film.getRating());
        Assertions.assertNotNull(foundFilm);
        Assertions.assertEquals(film, foundFilm);
    }

    @Test
    public void testFindAllByRating_valueGreaterThanEqualAndRating_countGreaterThanEqualMethodReturnResults() {
        List<Film> films = List.of(MarvelFilms.ironMan(), MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        List<Film> foundFilms =
                filmRepository.findAllByRating_valueGreaterThanEqualAndRating_countGreaterThanEqual(7.5f, 900_000);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(1, foundFilms.size());
        Assertions.assertTrue(films.contains(foundFilms.get(0)));
    }

    @Test
    public void testFindAllByRating_valueLessThanEqualMethodReturnResults() {
        List<Film> films = List.of(MarvelFilms.ironMan(), MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        List<Film> foundFilms = filmRepository.findAllByRating_valueLessThanEqual(7.5f);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(2, foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByRating_valueGreaterThanEqualOrderByRating_countMethodReturnResults() {
        List<Film> films = List.of(MarvelFilms.ironMan(), MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        List<Film> foundFilms = filmRepository.findAllByRating_valueGreaterThanEqualOrderByRating_count(7.1f);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(2, foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
        Assertions.assertEquals(foundFilms.get(0).getYear(), 2013);
        Assertions.assertEquals(foundFilms.get(1).getYear(), 2008);
    }

    @Test
    public void testCountAllMethodReturnNonZero() {
        List<Film> films = List.of(MarvelFilms.ironMan(), MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        long numberOfFilms = filmRepository.countAll();
        Assertions.assertEquals(films.size(), numberOfFilms);
    }

    @Test
    public void testCountAllByYearMethodReturnNonZero() {
        List<Film> films = List.of(MarvelFilms.incredibleHulk(), MarvelFilms.ironMan());
        insertFilms(films);

        long numberOfFilms = filmRepository.countAllByYear(2008);
        Assertions.assertEquals(films.size(), numberOfFilms);
    }

    @Test
    public void testCountAllByYearMethodReturnZero() {
        long numberOfFilms = filmRepository.countAllByYear(2000);
        Assertions.assertEquals(0, numberOfFilms);
    }

    @Test
    public void testCountAllByGrossGreaterThanEqualMethodReturnNonZero() {
        List<Film> films = List.of(MarvelFilms.ironMan(), MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        Long numberOfFilms = filmRepository.countAllByGrossGreaterThanEqual(500_000_000);
        Assertions.assertNotNull(numberOfFilms);
        Assertions.assertEquals(films.size(), numberOfFilms);
    }

    @Test
    public void testCountAllByGrossGreaterThanEqualMethodReturnZero() {
        Long numberOfFilms = filmRepository.countAllByGrossGreaterThanEqual(1_000_000_000);
        Assertions.assertNotNull(numberOfFilms);
        Assertions.assertEquals(0, numberOfFilms);
    }

    @Test
    public void testSaveMethod() {
        Film film = Film.builder()
                        .imdbId("tt9419884")
                        .title("Doctor Strange in the Multiverse of Madness")
                        .year(2022)
                        .rating(Rating.builder().build())
                        .wasReleased(false)
                        .build();
        filmRepository.save(film);

        Assertions.assertNotNull(film.getId());

        Film foundFilm = selectFilmById(film.getId());
        Assertions.assertEquals(film, foundFilm);
    }

    @Test
    public void testSaveAllMethodConsumesVarArgsParameter() {
        Film film1 = Film.builder()
                         .imdbId("tt9419884")
                         .title("Doctor Strange in the Multiverse of Madness")
                         .year(2022)
                         .rating(Rating.builder().build())
                         .wasReleased(false)
                         .build();
        Film film2 = Film.builder()
                         .imdbId("tt10648342")
                         .title("Thor: Love and Thunder")
                         .year(2022)
                         .rating(Rating.builder().build())
                         .wasReleased(false)
                         .build();
        filmRepository.saveAll(film1, film2);

        Assertions.assertNotNull(film1.getId());
        Assertions.assertNotNull(film2.getId());

        Film foundFilm1 = selectFilmById(film1.getId());
        Film foundFilm2 = selectFilmById(film2.getId());
        Assertions.assertEquals(film1, foundFilm1);
        Assertions.assertEquals(film2, foundFilm2);
    }

    @Test
    public void testSaveAllMethodConsumesIterableParameter() {
        Film film1 = Film.builder()
                         .imdbId("tt9419884")
                         .title("Doctor Strange in the Multiverse of Madness")
                         .year(2022)
                         .rating(Rating.builder().build())
                         .wasReleased(false)
                         .build();
        Film film2 = Film.builder()
                         .imdbId("tt10648342")
                         .title("Thor: Love and Thunder")
                         .year(2022)
                         .rating(Rating.builder().build())
                         .wasReleased(false)
                         .build();
        filmRepository.saveAll((Iterable<Film>) List.of(film1, film2));

        Assertions.assertNotNull(film1.getId());
        Assertions.assertNotNull(film2.getId());

        Film foundFilm1 = selectFilmById(film1.getId());
        Film foundFilm2 = selectFilmById(film2.getId());
        Assertions.assertEquals(film1, foundFilm1);
        Assertions.assertEquals(film2, foundFilm2);
    }

    @Test
    public void testSaveAllMethodConsumesCollectionParameter() {
        Film film1 = Film.builder()
                         .imdbId("tt9419884")
                         .title("Doctor Strange in the Multiverse of Madness")
                         .year(2022)
                         .rating(Rating.builder().build())
                         .wasReleased(false)
                         .build();
        Film film2 = Film.builder()
                         .imdbId("tt10648342")
                         .title("Thor: Love and Thunder")
                         .year(2022)
                         .rating(Rating.builder().build())
                         .wasReleased(false)
                         .build();
        filmRepository.saveAll((Collection<Film>) List.of(film1, film2));

        Assertions.assertNotNull(film1.getId());
        Assertions.assertNotNull(film2.getId());

        Film foundFilm1 = selectFilmById(film1.getId());
        Film foundFilm2 = selectFilmById(film2.getId());
        Assertions.assertEquals(film1, foundFilm1);
        Assertions.assertEquals(film2, foundFilm2);
    }

    @Test
    public void testSaveAllMethodConsumesListParameter() {
        Film film1 = Film.builder()
                         .imdbId("tt9419884")
                         .title("Doctor Strange in the Multiverse of Madness")
                         .year(2022)
                         .rating(Rating.builder().build())
                         .wasReleased(false)
                         .build();
        Film film2 = Film.builder()
                         .imdbId("tt10648342")
                         .title("Thor: Love and Thunder")
                         .year(2022)
                         .rating(Rating.builder().build())
                         .wasReleased(false)
                         .build();
        filmRepository.saveAll(List.of(film1, film2));

        Assertions.assertNotNull(film1.getId());
        Assertions.assertNotNull(film2.getId());

        Film foundFilm1 = selectFilmById(film1.getId());
        Film foundFilm2 = selectFilmById(film2.getId());
        Assertions.assertEquals(film1, foundFilm1);
        Assertions.assertEquals(film2, foundFilm2);
    }

    @Test
    public void testSaveAllMethodConsumesSetParameter() {
        Film film1 = Film.builder()
                         .imdbId("tt9419884")
                         .title("Doctor Strange in the Multiverse of Madness")
                         .year(2022)
                         .rating(Rating.builder().build())
                         .wasReleased(false)
                         .build();
        Film film2 = Film.builder()
                         .imdbId("tt10648342")
                         .title("Thor: Love and Thunder")
                         .year(2022)
                         .rating(Rating.builder().build())
                         .wasReleased(false)
                         .build();
        filmRepository.saveAll(Set.of(film1, film2));

        Assertions.assertNotNull(film1.getId());
        Assertions.assertNotNull(film2.getId());

        Film foundFilm1 = selectFilmById(film1.getId());
        Film foundFilm2 = selectFilmById(film2.getId());
        Assertions.assertEquals(film1, foundFilm1);
        Assertions.assertEquals(film2, foundFilm2);
    }

    @Test
    public void testUpdateMethod() {
        Film film = MarvelFilms.ironMan();
        insertFilm(film);

        film.setImdbId("tt0458339");
        film.setTitle("Captain America: The First Avenger");
        film.setYear(2011);
        film.setRating(Rating.builder()
                             .value(6.9f)
                             .count(761_701)
                             .build());
        film.setBudget(140_000_000L);
        film.setGross(370_000_000L);
        film.setTagline("When patriots become heroes");
        filmRepository.update(film);

        Film foundFilm = selectFilmById(film.getId());
        Assertions.assertEquals(film, foundFilm);
    }

    @Test
    public void testUpdateAllMethodConsumesVarArgsParameter() {
        Film film1 = MarvelFilms.ironMan();
        Film film2 = MarvelFilms.ironMan2();
        insertFilms(List.of(film1, film2));

        film1.setImdbId("tt0458339");
        film1.setTitle("Captain America: The First Avenger");
        film1.setYear(2011);
        film1.setRating(Rating.builder()
                              .value(6.9f)
                              .count(761_701)
                              .build());
        film1.setBudget(140_000_000L);
        film1.setGross(370_000_000L);
        film1.setTagline("When patriots become heroes");

        film2.setImdbId("tt1843866");
        film2.setTitle("Captain America: The Winter Soldier");
        film2.setYear(2014);
        film2.setRating(Rating.builder()
                              .value(7.7f)
                              .count(756_645)
                              .build());
        film2.setBudget(170_000_000L);
        film2.setGross(714_000_000L);
        film2.setTagline("In heroes we trust.");

        filmRepository.updateAll(film1, film2);

        Film foundFilm1 = selectFilmById(film1.getId());
        Film foundFilm2 = selectFilmById(film2.getId());
        Assertions.assertEquals(film1, foundFilm1);
        Assertions.assertEquals(film2, foundFilm2);
    }

    @Test
    public void testUpdateAllMethodConsumesIterableParameter() {
        Film film1 = MarvelFilms.ironMan();
        Film film2 = MarvelFilms.ironMan2();
        insertFilms(List.of(film1, film2));

        film1.setImdbId("tt0458339");
        film1.setTitle("Captain America: The First Avenger");
        film1.setYear(2011);
        film1.setRating(Rating.builder()
                              .value(6.9f)
                              .count(761_701)
                              .build());
        film1.setBudget(140_000_000L);
        film1.setGross(370_000_000L);
        film1.setTagline("When patriots become heroes");

        film2.setImdbId("tt1843866");
        film2.setTitle("Captain America: The Winter Soldier");
        film2.setYear(2014);
        film2.setRating(Rating.builder()
                              .value(7.7f)
                              .count(756_645)
                              .build());
        film2.setBudget(170_000_000L);
        film2.setGross(714_000_000L);
        film2.setTagline("In heroes we trust.");

        filmRepository.updateAll((Iterable<Film>) List.of(film1, film2));

        Film foundFilm1 = selectFilmById(film1.getId());
        Film foundFilm2 = selectFilmById(film2.getId());
        Assertions.assertEquals(film1, foundFilm1);
        Assertions.assertEquals(film2, foundFilm2);
    }

    @Test
    public void testUpdateAllMethodConsumesCollectionParameter() {
        Film film1 = MarvelFilms.ironMan();
        Film film2 = MarvelFilms.ironMan2();
        insertFilms(List.of(film1, film2));

        film1.setImdbId("tt0458339");
        film1.setTitle("Captain America: The First Avenger");
        film1.setYear(2011);
        film1.setRating(Rating.builder()
                              .value(6.9f)
                              .count(761_701)
                              .build());
        film1.setBudget(140_000_000L);
        film1.setGross(370_000_000L);
        film1.setTagline("When patriots become heroes");

        film2.setImdbId("tt1843866");
        film2.setTitle("Captain America: The Winter Soldier");
        film2.setYear(2014);
        film2.setRating(Rating.builder()
                              .value(7.7f)
                              .count(756_645)
                              .build());
        film2.setBudget(170_000_000L);
        film2.setGross(714_000_000L);
        film2.setTagline("In heroes we trust.");

        filmRepository.updateAll((Collection<Film>) List.of(film1, film2));

        Film foundFilm1 = selectFilmById(film1.getId());
        Film foundFilm2 = selectFilmById(film2.getId());
        Assertions.assertEquals(film1, foundFilm1);
        Assertions.assertEquals(film2, foundFilm2);
    }

    @Test
    public void testUpdateAllMethodConsumesListParameter() {
        Film film1 = MarvelFilms.ironMan();
        Film film2 = MarvelFilms.ironMan2();
        insertFilms(List.of(film1, film2));

        film1.setImdbId("tt0458339");
        film1.setTitle("Captain America: The First Avenger");
        film1.setYear(2011);
        film1.setRating(Rating.builder()
                              .value(6.9f)
                              .count(761_701)
                              .build());
        film1.setBudget(140_000_000L);
        film1.setGross(370_000_000L);
        film1.setTagline("When patriots become heroes");

        film2.setImdbId("tt1843866");
        film2.setTitle("Captain America: The Winter Soldier");
        film2.setYear(2014);
        film2.setRating(Rating.builder()
                              .value(7.7f)
                              .count(756_645)
                              .build());
        film2.setBudget(170_000_000L);
        film2.setGross(714_000_000L);
        film2.setTagline("In heroes we trust.");

        filmRepository.updateAll(List.of(film1, film2));

        Film foundFilm1 = selectFilmById(film1.getId());
        Film foundFilm2 = selectFilmById(film2.getId());
        Assertions.assertEquals(film1, foundFilm1);
        Assertions.assertEquals(film2, foundFilm2);
    }

    @Test
    public void testUpdateAllMethodConsumesSetParameter() {
        Film film1 = MarvelFilms.ironMan();
        Film film2 = MarvelFilms.ironMan2();
        insertFilms(List.of(film1, film2));

        film1.setImdbId("tt0458339");
        film1.setTitle("Captain America: The First Avenger");
        film1.setYear(2011);
        film1.setRating(Rating.builder()
                              .value(6.9f)
                              .count(761_701)
                              .build());
        film1.setBudget(140_000_000L);
        film1.setGross(370_000_000L);
        film1.setTagline("When patriots become heroes");

        film2.setImdbId("tt1843866");
        film2.setTitle("Captain America: The Winter Soldier");
        film2.setYear(2014);
        film2.setRating(Rating.builder()
                              .value(7.7f)
                              .count(756_645)
                              .build());
        film2.setBudget(170_000_000L);
        film2.setGross(714_000_000L);
        film2.setTagline("In heroes we trust.");

        filmRepository.updateAll(Set.of(film1, film2));

        Film foundFilm1 = selectFilmById(film1.getId());
        Film foundFilm2 = selectFilmById(film2.getId());
        Assertions.assertEquals(film1, foundFilm1);
        Assertions.assertEquals(film2, foundFilm2);
    }

    @Test
    public void testDeleteMethod() {
        Film film = MarvelFilms.incredibleHulk();
        insertFilm(film);

        filmRepository.delete(film);

        Film foundFilm = selectFilmById(film.getId());
        Assertions.assertNull(foundFilm);
    }

    @Test
    public void testDeleteByIdMethod() {
        Film film = MarvelFilms.incredibleHulk();
        insertFilm(film);

        filmRepository.deleteById(film.getId());

        Film foundFilm = selectFilmById(film.getId());
        Assertions.assertNull(foundFilm);
    }

    @Test
    public void testDeleteAllMethodConsumesVarArgsParameter() {
        Film film1 = MarvelFilms.ironMan();
        Film film2 = MarvelFilms.ironMan2();
        Film film3 = MarvelFilms.ironMan3();
        insertFilms(List.of(film1, film2, film3));

        filmRepository.deleteAll(film1, film2, film3);

        Film foundFilm1 = selectFilmById(film1.getId());
        Assertions.assertNull(foundFilm1);

        Film foundFilm2 = selectFilmById(film2.getId());
        Assertions.assertNull(foundFilm2);

        Film foundFilm3 = selectFilmById(film3.getId());
        Assertions.assertNull(foundFilm3);
    }

    @Test
    public void testDeleteAllMethodConsumesIterableParameter() {
        Iterable<Film> films = List.of(MarvelFilms.ironMan(), MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        filmRepository.deleteAll(films);

        films.forEach(film -> {
            Film foundFilm = selectFilmById(film.getId());
            Assertions.assertNull(foundFilm);
        });
    }

    @Test
    public void testDeleteAllMethodConsumesCollectionParameter() {
        Collection<Film> films = List.of(MarvelFilms.ironMan(), MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        filmRepository.deleteAll(films);

        films.forEach(film -> {
            Film foundFilm = selectFilmById(film.getId());
            Assertions.assertNull(foundFilm);
        });
    }

    @Test
    public void testDeleteAllMethodConsumesListParameter() {
        List<Film> films = List.of(MarvelFilms.ironMan(), MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        filmRepository.deleteAll(films);

        films.forEach(film -> {
            Film foundFilm = selectFilmById(film.getId());
            Assertions.assertNull(foundFilm);
        });
    }

    @Test
    public void testDeleteAllMethodConsumesSetParameter() {
        Set<Film> films = Set.of(MarvelFilms.ironMan(), MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        filmRepository.deleteAll(films);

        films.forEach(film -> {
            Film foundFilm = selectFilmById(film.getId());
            Assertions.assertNull(foundFilm);
        });
    }

    @Test
    public void testDeleteAllByYearInMethod() {
        List<Film> films = List.of(MarvelFilms.ironMan(), MarvelFilms.ironMan2(), MarvelFilms.ironMan3());
        insertFilms(films);

        filmRepository.deleteAllByYearIn(Set.of(2008, 2010, 2013));

        films.forEach(film -> {
            Film foundFilm = selectFilmById(film.getId());
            Assertions.assertNull(foundFilm);
        });
    }

    @SneakyThrows
    public static void insertFilm(Film film) {
        @Cleanup Connection connection = DATA_SOURCE.getConnection();
        @Cleanup PreparedStatement statement =
                connection.prepareStatement("INSERT INTO public.film(id, imdb_id, title, year, rating_value, rating_count, budget, gross, tagline, was_released) " +
                                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        int index = 1;
        statement.setLong(index++, film.getId());
        statement.setString(index++, film.getImdbId());
        statement.setString(index++, film.getTitle());
        statement.setInt(index++, film.getYear());
        statement.setFloat(index++, film.getRating().getValue());
        statement.setLong(index++, film.getRating().getCount());
        statement.setObject(index++, film.getBudget());
        statement.setObject(index++, film.getGross());
        statement.setString(index++, film.getTagline());
        statement.setBoolean(index++, film.isWasReleased());
        statement.executeUpdate();
    }

    @SneakyThrows
    public static void insertFilms(Iterable<Film> films) {
        @Cleanup Connection connection = DATA_SOURCE.getConnection();
        @Cleanup PreparedStatement statement =
                connection.prepareStatement("INSERT INTO public.film(id, imdb_id, title, year, rating_value, rating_count, budget, gross, tagline, was_released) " +
                                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        for (Film film : films) {
            int index = 1;
            statement.setLong(index++, film.getId());
            statement.setString(index++, film.getImdbId());
            statement.setString(index++, film.getTitle());
            statement.setInt(index++, film.getYear());
            statement.setFloat(index++, film.getRating().getValue());
            statement.setLong(index++, film.getRating().getCount());
            statement.setObject(index++, film.getBudget());
            statement.setObject(index++, film.getGross());
            statement.setString(index++, film.getTagline());
            statement.setBoolean(index++, film.isWasReleased());
            statement.addBatch();
        }
        statement.executeBatch();
    }

    @SneakyThrows
    public static Film selectFilmById(long id) {
        @Cleanup Connection connection = DATA_SOURCE.getConnection();
        @Cleanup PreparedStatement statement =
                connection.prepareStatement("SELECT id, imdb_id, title, YEAR, rating_value, rating_count, budget, gross, tagline, was_released " +
                                            "FROM PUBLIC.film " +
                                            "WHERE id = ?");
        statement.setLong(1, id);
        @Cleanup ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return Film.builder()
                       .id(resultSet.getLong("id"))
                       .imdbId(resultSet.getString("imdb_id"))
                       .title(resultSet.getString("title"))
                       .year(resultSet.getInt("year"))
                       .rating(Rating.builder()
                                     .value(resultSet.getFloat("rating_value"))
                                     .count(resultSet.getLong("rating_count"))
                                     .build())
                       .budget((Long) resultSet.getObject("budget"))
                       .gross((Long) resultSet.getObject("gross"))
                       .tagline(resultSet.getString("tagline"))
                       .wasReleased(resultSet.getBoolean("was_released"))
                       .build();
        }
        return null;
    }

    public static final class MarvelFilms {
        public static Film incredibleHulk() {
            return Film.builder()
                       .id(1L)
                       .imdbId("tt0800080")
                       .title("The Incredible Hulk")
                       .year(2008)
                       .rating(Rating.builder()
                                     .value(6.7f)
                                     .count(444_129)
                                     .build())
                       .budget(150_000_000L)
                       .gross(264_000_000L)
                       .tagline("This June, a hero shows his true colors")
                       .wasReleased(true)
                       .build();
        }

        public static Film ironMan() {
            return Film.builder()
                       .id(2L)
                       .imdbId("tt0371746")
                       .title("Iron Man")
                       .year(2008)
                       .rating(Rating.builder()
                                     .value(7.9f)
                                     .count(960_545)
                                     .build())
                       .budget(140_000_000L)
                       .gross(585_000_000L)
                       .tagline("Get ready for a different breed of heavy metal hero.")
                       .wasReleased(true)
                       .build();
        }

        public static Film ironMan2() {
            return Film.builder()
                       .id(3L)
                       .imdbId("tt1228705")
                       .title("Iron Man 2")
                       .year(2010)
                       .rating(Rating.builder()
                                     .value(7)
                                     .count(739_724)
                                     .build())
                       .budget(200_000_000L)
                       .gross(623_000_000L)
                       .tagline("It's not the armor that makes the hero, but the man inside.")
                       .wasReleased(true)
                       .build();
        }

        public static Film ironMan3() {
            return Film.builder()
                       .id(4L)
                       .imdbId("tt1300854")
                       .title("Iron Man Three")
                       .year(2013)
                       .rating(Rating.builder()
                                     .value(7.1f)
                                     .count(772_782)
                                     .build())
                       .budget(200_000_000L)
                       .gross(1_214_000_000L)
                       .tagline("Unleash the power behind the armor.")
                       .wasReleased(true)
                       .build();
        }

        private MarvelFilms() {
        }
    }
}
