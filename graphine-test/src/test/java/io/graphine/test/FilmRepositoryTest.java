package io.graphine.test;

import io.graphine.core.NonUniqueResultException;
import io.graphine.test.model.Film;
import io.graphine.test.repository.FilmRepository;
import io.graphine.test.repository.GraphineFilmRepository;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

import static io.graphine.test.util.DataSourceProvider.DATA_SOURCE;
import static io.graphine.test.util.DataSourceProvider.PROXY_DATA_SOURCE;

/**
 * @author Oleg Marchenko
 */
public class FilmRepositoryTest {

    @BeforeAll
    @SneakyThrows
    public static void createTable() {
        @Cleanup Connection connection = DATA_SOURCE.getConnection();
        @Cleanup Statement statement = connection.createStatement();
        statement.executeQuery("CREATE TABLE public.films(" +
                               "id BIGSERIAL PRIMARY KEY, " +
                               "imdb_id TEXT NOT NULL, " +
                               "title TEXT NOT NULL, " +
                               "year INTEGER NOT NULL, " +
                               "budget BIGINT, " +
                               "gross BIGINT, " +
                               "tagline TEXT, " +
                               "was_released BOOLEAN NOT NULL DEFAULT FALSE)");
    }

    @AfterEach
    @SneakyThrows
    public void cleanTable() {
        @Cleanup Connection connection = DATA_SOURCE.getConnection();
        @Cleanup Statement statement = connection.createStatement();
        statement.executeQuery("TRUNCATE TABLE public.films");
    }

    @AfterAll
    @SneakyThrows
    public static void dropTable() {
        @Cleanup Connection connection = DATA_SOURCE.getConnection();
        @Cleanup Statement statement = connection.createStatement();
        statement.executeQuery("DROP TABLE public.films");
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
    public void testFindByImdbIdMethodReturnNonEmptyOptional() {
        Film film = MarvelFilms.ironMan();
        insertFilm(film);

        Optional<Film> foundFilm = filmRepository.findByImdbId(film.getImdbId());
        Assertions.assertNotNull(foundFilm);
        Assertions.assertTrue(foundFilm.isPresent());
        Assertions.assertEquals(film, foundFilm.get());
    }

    @Test
    public void testFindByImdbIdMethodReturnEmptyOptional() {
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

        insertFilms(Arrays.asList(film1, film2));

        Assertions.assertThrows(NonUniqueResultException.class, () -> filmRepository.findByImdbId("tt0000000"));
    }

    @Test
    public void testFindAllMethodReturnAllResults() {
        Collection<Film> films = Arrays.asList(
                MarvelFilms.ironMan(),
                MarvelFilms.ironMan2(),
                MarvelFilms.ironMan3()
        );
        insertFilms(films);

        Collection<Film> foundFilms = filmRepository.findAll();
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllMethodReturnEmptyResult() {
        Collection<Film> films = filmRepository.findAll();
        Assertions.assertNotNull(films);
        Assertions.assertEquals(0, films.size());
    }

    @Test
    public void testFindAllByYearMethodReturnResults() {
        Collection<Film> films = Arrays.asList(
                MarvelFilms.incredibleHulk(),
                MarvelFilms.ironMan()
        );
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
        Collection<Film> films = Arrays.asList(
                MarvelFilms.incredibleHulk(),
                MarvelFilms.ironMan()
        );
        insertFilms(films);

        Collection<Film> foundFilms = filmRepository.findAllByYearIsNot(2010);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByBudgetBetweenMethodReturnResults() {
        Collection<Film> films = Arrays.asList(
                MarvelFilms.incredibleHulk(),
                MarvelFilms.ironMan()
        );
        insertFilms(films);

        Collection<Film> foundFilms = filmRepository.findAllByBudgetBetween(140_000_000, 150_000_000);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByBudgetNotBetweenMethodReturnResults() {
        Collection<Film> films = Arrays.asList(
                MarvelFilms.incredibleHulk(),
                MarvelFilms.ironMan()
        );
        insertFilms(films);

        Collection<Film> foundFilms = filmRepository.findAllByBudgetNotBetween(200_000_000, 300_000_000);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByBudgetLessThanMethodReturnResults() {
        Collection<Film> films = Arrays.asList(
                MarvelFilms.incredibleHulk(),
                MarvelFilms.ironMan()
        );
        insertFilms(films);

        Collection<Film> foundFilms = filmRepository.findAllByBudgetLessThan(200_000_000);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByBudgetLessThanEqualMethodReturnResults() {
        Collection<Film> films = Arrays.asList(
                MarvelFilms.incredibleHulk(),
                MarvelFilms.ironMan(),
                MarvelFilms.ironMan2()
        );
        insertFilms(films);

        Collection<Film> foundFilms = filmRepository.findAllByBudgetLessThanEqual(200_000_000);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByGrossGreaterThanMethodReturnResults() {
        Collection<Film> films = Arrays.asList(
                MarvelFilms.ironMan2(),
                MarvelFilms.ironMan3()
        );
        insertFilms(films);

        Collection<Film> foundFilms = filmRepository.findAllByGrossGreaterThan(600_000_000);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByGrossGreaterThanEqualMethodReturnResults() {
        Collection<Film> films = Collections.singletonList(MarvelFilms.ironMan3());
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

        List<Film> films = Arrays.asList(film1, film2);
        insertFilms(films);

        List<Film> foundFilms = filmRepository.findAllByTaglineIsEmpty();
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByTaglineIsNotEmptyMethodReturnResults() {
        List<Film> films = Arrays.asList(
                MarvelFilms.ironMan(),
                MarvelFilms.ironMan2()
        );
        insertFilms(films);

        List<Film> foundFilms = filmRepository.findAllByTaglineIsNotEmpty();
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByTitleLikeMethodReturnResults() {
        List<Film> films = Arrays.asList(
                MarvelFilms.ironMan(),
                MarvelFilms.ironMan2(),
                MarvelFilms.ironMan3()
        );
        insertFilms(films);

        List<Film> foundFilms = filmRepository.findAllByTitleLike("Iron Man%");
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByTitleNotLikeMethodReturnResults() {
        List<Film> films = Arrays.asList(
                MarvelFilms.ironMan(),
                MarvelFilms.ironMan2(),
                MarvelFilms.ironMan3()
        );
        insertFilms(films);

        List<Film> foundFilms = filmRepository.findAllByTitleNotLike("Captain America%");
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByTitleStartingWithMethodReturnResults() {
        List<Film> films = Arrays.asList(
                MarvelFilms.ironMan(),
                MarvelFilms.ironMan2(),
                MarvelFilms.ironMan3()
        );
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
        List<Film> films = Arrays.asList(
                MarvelFilms.ironMan(),
                MarvelFilms.ironMan2(),
                MarvelFilms.ironMan3()
        );
        insertFilms(films);

        List<Film> foundFilms = filmRepository.findAllByTitleContaining("Man");
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByTitleNotContainingMethodReturnResults() {
        List<Film> films = Arrays.asList(
                MarvelFilms.ironMan(),
                MarvelFilms.ironMan2(),
                MarvelFilms.ironMan3()
        );
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

        List<Film> films = Arrays.asList(film1, film2);
        insertFilms(films);

        Set<Film> foundFilms = filmRepository.findAllByBudgetIsNull();
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByGrossIsNotNullMethodReturnResults() {
        List<Film> films = Arrays.asList(
                MarvelFilms.ironMan(),
                MarvelFilms.ironMan2(),
                MarvelFilms.ironMan3()
        );
        insertFilms(films);

        Set<Film> foundFilms = filmRepository.findAllByGrossIsNotNull();
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByWasReleasedIsTrueMethodReturnResults() {
        List<Film> films = Arrays.asList(
                MarvelFilms.ironMan(),
                MarvelFilms.ironMan2(),
                MarvelFilms.ironMan3()
        );
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

        List<Film> films = Arrays.asList(film1, film2);
        insertFilms(films);

        Set<Film> foundFilms = filmRepository.findAllByWasReleasedIsFalse();
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByYearInMethodReturnResults() {
        List<Film> films = Arrays.asList(
                MarvelFilms.incredibleHulk(),
                MarvelFilms.ironMan(),
                MarvelFilms.ironMan2(),
                MarvelFilms.ironMan3()
        );
        insertFilms(films);

        Set<Film> foundFilms = filmRepository.findAllByYearIn(Arrays.asList(2008, 2010, 2013));
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByYearNotInMethodReturnResults() {
        List<Film> films = Arrays.asList(
                MarvelFilms.incredibleHulk(),
                MarvelFilms.ironMan(),
                MarvelFilms.ironMan2(),
                MarvelFilms.ironMan3()
        );
        insertFilms(films);

        Set<Film> foundFilms = filmRepository.findAllByYearNotIn(2000, 2001, 2002);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByBudgetGreaterThanEqualAndGrossGreaterThanMethodReturnResults() {
        List<Film> films = Collections.singletonList(MarvelFilms.ironMan3());
        insertFilms(films);

        Set<Film> foundFilms = filmRepository.findAllByBudgetGreaterThanEqualAndGrossGreaterThan(200_000_000,
                                                                                                 1_000_000_000);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByBudgetBetweenOrGrossBetweenMethodReturnResults() {
        List<Film> films = Arrays.asList(
                MarvelFilms.ironMan(),
                MarvelFilms.ironMan2(),
                MarvelFilms.ironMan3()
        );
        insertFilms(films);

        Set<Film> foundFilms = filmRepository.findAllByBudgetBetweenOrGrossBetween(100_000_000, 150_000_000,
                                                                                   600_000_000, 1_500_000_000);
        Assertions.assertNotNull(foundFilms);
        Assertions.assertEquals(films.size(), foundFilms.size());
        Assertions.assertTrue(films.containsAll(foundFilms));
    }

    @Test
    public void testFindAllByYearBetweenOrderByYearMethodReturnResults() {
        List<Film> films = Arrays.asList(
                MarvelFilms.ironMan(),
                MarvelFilms.ironMan2(),
                MarvelFilms.ironMan3()
        );
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
        List<Film> films = Arrays.asList(
                MarvelFilms.ironMan(),
                MarvelFilms.ironMan2(),
                MarvelFilms.ironMan3()
        );
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
        List<Film> films = Arrays.asList(
                MarvelFilms.ironMan(),
                MarvelFilms.ironMan2(),
                MarvelFilms.ironMan3()
        );
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
    public void testCountAllMethodReturnNonZero() {
        List<Film> films = Arrays.asList(
                MarvelFilms.ironMan(),
                MarvelFilms.ironMan2(),
                MarvelFilms.ironMan3()
        );
        insertFilms(films);

        long numberOfFilms = filmRepository.countAll();
        Assertions.assertEquals(films.size(), numberOfFilms);
    }

    @Test
    public void testCountAllByYearMethodReturnNonZero() {
        List<Film> films = Arrays.asList(
                MarvelFilms.incredibleHulk(),
                MarvelFilms.ironMan()
        );
        insertFilms(films);

        int numberOfFilms = filmRepository.countAllByYear(2008);
        Assertions.assertEquals(films.size(), numberOfFilms);
    }

    @Test
    public void testCountAllByYearMethodReturnZero() {
        int numberOfFilms = filmRepository.countAllByYear(2000);
        Assertions.assertEquals(0, numberOfFilms);
    }

    @Test
    public void testSaveMethod() {
        Film film = Film.builder()
                        .imdbId("tt9419884")
                        .title("Doctor Strange in the Multiverse of Madness")
                        .year(2022)
                        .wasReleased(false)
                        .build();
        filmRepository.save(film);

        Assertions.assertNotNull(film.getId());

        Film foundFilm = selectFilmById(film.getId());
        Assertions.assertEquals(film, foundFilm);
    }

    @Test
    public void testSaveAllMethod() {
        Film film1 = Film.builder()
                         .imdbId("tt9419884")
                         .title("Doctor Strange in the Multiverse of Madness")
                         .year(2022)
                         .wasReleased(false)
                         .build();
        Film film2 = Film.builder()
                         .imdbId("tt10648342")
                         .title("Thor: Love and Thunder")
                         .year(2022)
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
    public void testUpdateMethod() {
        Film film = MarvelFilms.ironMan();
        insertFilm(film);

        film.setImdbId("tt0458339");
        film.setTitle("Captain America: The First Avenger");
        film.setYear(2011);
        film.setBudget(140_000_000L);
        film.setGross(370_000_000L);
        film.setTagline("When patriots become heroes");
        filmRepository.update(film);

        Film foundFilm = selectFilmById(film.getId());
        Assertions.assertEquals(film, foundFilm);
    }

    @Test
    public void testUpdateAllMethod() {
        Film film1 = MarvelFilms.ironMan();
        Film film2 = MarvelFilms.ironMan2();
        insertFilms(Arrays.asList(film1, film2));

        film1.setImdbId("tt0458339");
        film1.setTitle("Captain America: The First Avenger");
        film1.setYear(2011);
        film1.setBudget(140_000_000L);
        film1.setGross(370_000_000L);
        film1.setTagline("When patriots become heroes");

        film2.setImdbId("tt1843866");
        film2.setTitle("Captain America: The Winter Soldier");
        film2.setYear(2014);
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
    public void testDeleteAllMethod() {
        List<Film> films = Arrays.asList(
                MarvelFilms.ironMan(),
                MarvelFilms.ironMan2(),
                MarvelFilms.ironMan3()
        );
        insertFilms(films);

        filmRepository.deleteAll(films.toArray(new Film[0]));

        films.forEach(film -> {
            Film foundFilm = selectFilmById(film.getId());
            Assertions.assertNull(foundFilm);
        });
    }

    @Test
    public void testDeleteAllByYearInMethod() {
        List<Film> films = Arrays.asList(
                MarvelFilms.ironMan(),
                MarvelFilms.ironMan2(),
                MarvelFilms.ironMan3()
        );
        insertFilms(films);

        filmRepository.deleteAllByYearIn(new HashSet<>(Arrays.asList(2008, 2010, 2013)));

        films.forEach(film -> {
            Film foundFilm = selectFilmById(film.getId());
            Assertions.assertNull(foundFilm);
        });
    }

    @SneakyThrows
    public static void insertFilm(Film film) {
        @Cleanup Connection connection = DATA_SOURCE.getConnection();
        @Cleanup PreparedStatement statement =
                connection.prepareStatement("INSERT INTO public.films(id, imdb_id, title, year, budget, gross, tagline, was_released) " +
                                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        int index = 1;
        statement.setLong(index++, film.getId());
        statement.setString(index++, film.getImdbId());
        statement.setString(index++, film.getTitle());
        statement.setInt(index++, film.getYear());
        statement.setObject(index++, film.getBudget());
        statement.setObject(index++, film.getGross());
        statement.setString(index++, film.getTagline());
        statement.setBoolean(index++, film.isWasReleased());
        statement.executeUpdate();
    }

    @SneakyThrows
    public static void insertFilms(Collection<Film> films) {
        @Cleanup Connection connection = DATA_SOURCE.getConnection();
        @Cleanup PreparedStatement statement =
                connection.prepareStatement("INSERT INTO public.films(id, imdb_id, title, year, budget, gross, tagline, was_released) " +
                                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        for (Film film : films) {
            int index = 1;
            statement.setLong(index++, film.getId());
            statement.setString(index++, film.getImdbId());
            statement.setString(index++, film.getTitle());
            statement.setInt(index++, film.getYear());
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
                connection.prepareStatement("SELECT id, imdb_id, title, year, budget, gross, tagline, was_released " +
                                            "FROM public.films " +
                                            "WHERE id = ?");
        statement.setLong(1, id);
        @Cleanup ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return Film.builder()
                       .id(resultSet.getLong("id"))
                       .imdbId(resultSet.getString("imdb_id"))
                       .title(resultSet.getString("title"))
                       .year(resultSet.getInt("year"))
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
