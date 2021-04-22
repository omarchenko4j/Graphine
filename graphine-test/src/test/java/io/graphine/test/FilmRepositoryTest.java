package io.graphine.test;

import io.graphine.test.model.Film;
import io.graphine.test.repository.FilmRepository;
import io.graphine.test.repository.GraphineFilmRepository;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.Configuration;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.*;
import java.util.*;

import static io.graphine.test.util.DataSourceProvider.DATA_SOURCE;
import static io.graphine.test.util.DataSourceProvider.PROXY_DATA_SOURCE;

/**
 * @author Oleg Marchenko
 */
public class FilmRepositoryTest {

    @BeforeClass
    public static void init() {
        Configuration config =
                Flyway.configure()
                      .dataSource(DATA_SOURCE);
        Flyway flyway = new Flyway(config);
        flyway.migrate();
    }

    private final FilmRepository filmRepository = new GraphineFilmRepository(PROXY_DATA_SOURCE);

    @Test
    public void testFindById() {
        Film film = filmRepository.findById(1);
        Assert.assertNotNull(film);
        Assert.assertEquals(film.getId(), Long.valueOf(1));
        Assert.assertEquals(film.getImdbId(), "tt0371746");
        Assert.assertEquals(film.getTitle(), "Iron Man");
        Assert.assertEquals(film.getYear(), 2008);
        Assert.assertEquals(film.getBudget(), Long.valueOf(140_000_000));
        Assert.assertEquals(film.getGross(), Long.valueOf(585_000_000));
        Assert.assertEquals(film.getTagline(), "Get ready for a different breed of heavy metal hero.");
        Assert.assertTrue(film.isWasReleased());
    }

    @Test
    public void testFindByImdbId() {
        Optional<Film> filmOptional = filmRepository.findByImdbId("tt1228705");
        Assert.assertNotNull(filmOptional);
        Assert.assertTrue(filmOptional.isPresent());

        Film film = filmOptional.get();
        Assert.assertEquals(film.getId(), Long.valueOf(3));
        Assert.assertEquals(film.getImdbId(), "tt1228705");
        Assert.assertEquals(film.getTitle(), "Iron Man 2");
        Assert.assertEquals(film.getYear(), 2010);
        Assert.assertEquals(film.getBudget(), Long.valueOf(200_000_000));
        Assert.assertEquals(film.getGross(), Long.valueOf(623_000_000));
        Assert.assertEquals(film.getTagline(), "It's not the armor that makes the hero, but the man inside.");
        Assert.assertTrue(film.isWasReleased());
    }

    @Test
    public void testFindAll() {
        Collection<Film> films = filmRepository.findAll();
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 27);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByYear() {
        Collection<Film> films = filmRepository.findAllByYear(2018);
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 3);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByYearIsNot() {
        Collection<Film> films = filmRepository.findAllByYearIsNot(2021);
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 23);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByYearBetween() {
        Collection<Film> films = filmRepository.findAllByBudgetBetween(150_000_000, 200_000_000);
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 15);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByYearNotBetween() {
        Collection<Film> films = filmRepository.findAllByBudgetNotBetween(200_000_000, 300_000_000);
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 16);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByBudgetLessThan() {
        Collection<Film> films = filmRepository.findAllByBudgetLessThan(200_000_000);
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 14);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByBudgetLessThanEqual() {
        Collection<Film> films = filmRepository.findAllByBudgetLessThanEqual(150_000_000);
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 5);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByGrossGreaterThan() {
        Collection<Film> films = filmRepository.findAllByGrossGreaterThan(750_000_000);
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 13);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByGrossGreaterThanEqual() {
        Collection<Film> films = filmRepository.findAllByGrossGreaterThanEqual(1_000_000_000);
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 9);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByTaglineIsEmpty() {
        List<Film> films = filmRepository.findAllByTaglineIsEmpty();
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 5);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByTaglineIsNotEmpty() {
        List<Film> films = filmRepository.findAllByTaglineIsNotEmpty();
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 22);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByTitleLike() {
        List<Film> films = filmRepository.findAllByTitleLike("Captain America%");
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 3);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByTitleNotLike() {
        List<Film> films = filmRepository.findAllByTitleNotLike("Captain America%");
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 24);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByTitleStartingWith() {
        List<Film> films = filmRepository.findAllByTitleStartingWith("Iron Man");
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 3);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByTitleEndingWith() {
        List<Film> films = filmRepository.findAllByTitleEndingWith("War");
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 2);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByTitleContaining() {
        List<Film> films = filmRepository.findAllByTitleContaining("Man");
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 8);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByTitleNotContaining() {
        List<Film> films = filmRepository.findAllByTitleNotContaining("Avenger");
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 22);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByBudgetIsNull() {
        Set<Film> films = filmRepository.findAllByBudgetIsNull();
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 4);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByGrossIsNotNull() {
        Set<Film> films = filmRepository.findAllByGrossIsNotNull();
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 23);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByWasReleasedIsTrue() {
        Set<Film> films = filmRepository.findAllByWasReleasedIsTrue();
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 23);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByWasReleasedIsFalse() {
        Set<Film> films = filmRepository.findAllByWasReleasedIsFalse();
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 4);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByYearIn() {
        Set<Film> films = filmRepository.findAllByYearIn(Arrays.asList(2017, 2019));
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 6);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByYearNotIn() {
        Set<Film> films = filmRepository.findAllByYearNotIn(2008, 2010, 2013, 2015, 2019);
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 17);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByBudgetGreaterThanEqualAndGrossGreaterThan() {
        Set<Film> films = filmRepository.findAllByBudgetGreaterThanEqualAndGrossGreaterThan(200_000_000, 1_000_000_000);
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 7);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByBudgetBetweenOrGrossBetween() {
        Set<Film> films = filmRepository.findAllByBudgetBetweenOrGrossBetween(200_000_000, 250_000_000,
                                                                              1_500_000_000, 2_500_000_000L);
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 8);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testFindAllByYearBetweenOrderByYear() {
        List<Film> films = filmRepository.findAllByYearBetweenOrderByYear(2010, 2012);
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 4);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
        Assert.assertEquals(films.get(0).getYear(), 2010);
        Assert.assertEquals(films.get(1).getYear(), 2011);
        Assert.assertEquals(films.get(2).getYear(), 2011);
        Assert.assertEquals(films.get(3).getYear(), 2012);
    }

    @Test
    public void testFindAllByYearLessThanEqualOrderByYearAsc() {
        List<Film> films = filmRepository.findAllByYearLessThanEqualOrderByYearAsc(2012);
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 6);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
        Assert.assertEquals(films.get(0).getYear(), 2008);
        Assert.assertEquals(films.get(1).getYear(), 2008);
        Assert.assertEquals(films.get(2).getYear(), 2010);
        Assert.assertEquals(films.get(3).getYear(), 2011);
        Assert.assertEquals(films.get(4).getYear(), 2011);
        Assert.assertEquals(films.get(5).getYear(), 2012);
    }

    @Test
    public void testFindAllByYearGreaterThanAndTaglineIsNotEmptyOrderByYearDesc() {
        List<Film> films = filmRepository.findAllByYearGreaterThanAndTaglineIsNotEmptyOrderByYearDesc(2016);
        Assert.assertNotNull(films);
        Assert.assertEquals(films.size(), 8);
        Assert.assertTrue(films.stream().allMatch(Objects::nonNull));
        Assert.assertEquals(films.get(0).getYear(), 2019);
        Assert.assertEquals(films.get(1).getYear(), 2019);
        Assert.assertEquals(films.get(2).getYear(), 2018);
        Assert.assertEquals(films.get(3).getYear(), 2018);
        Assert.assertEquals(films.get(4).getYear(), 2018);
        Assert.assertEquals(films.get(5).getYear(), 2017);
        Assert.assertEquals(films.get(6).getYear(), 2017);
        Assert.assertEquals(films.get(7).getYear(), 2017);
    }

    @Test
    public void testCountAll() {
        long numberOfFilms = filmRepository.countAll();
        Assert.assertEquals(numberOfFilms, 27);
    }

    @Test
    public void testCountAllByYear() {
        int numberOfFilms = filmRepository.countAllByYear(2017);
        Assert.assertEquals(numberOfFilms, 3);
    }

    @Test
    public void testSave() {
        Film film = Film.builder()
                .imdbId("tt9419884")
                .title("Doctor Strange in the Multiverse of Madness")
                .year(2022)
                .wasReleased(false)
                .build();
        filmRepository.save(film);

        Assert.assertNotNull(film.getId());

        try (Connection connection = DATA_SOURCE.getConnection()) {
            try (PreparedStatement statement =
                         connection.prepareStatement("SELECT id, imdb_id, title, year, was_released FROM films WHERE id = ?")) {
                statement.setLong(1, film.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    Assert.assertTrue(resultSet.next());
                    Assert.assertEquals(resultSet.getString("imdb_id"), "tt9419884");
                    Assert.assertEquals(resultSet.getString("title"), "Doctor Strange in the Multiverse of Madness");
                    Assert.assertEquals(resultSet.getInt("year"), 2022);
                    Assert.assertFalse(resultSet.getBoolean("was_released"));
                }
            }

            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM films WHERE id = ?")) {
                statement.setLong(1, film.getId());
                statement.executeUpdate();
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSaveAll() {
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

        Assert.assertNotNull(film1.getId());
        Assert.assertNotNull(film2.getId());

        try (Connection connection = DATA_SOURCE.getConnection()) {
            try (PreparedStatement statement =
                         connection.prepareStatement("SELECT id, imdb_id, title, year, was_released FROM films WHERE id IN (?, ?)")) {
                statement.setLong(1, film1.getId());
                statement.setLong(2, film2.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    int count = 0;
                    while (resultSet.next()) {
                        long id = resultSet.getLong("id");
                        if (id == film1.getId()) {
                            Assert.assertEquals(resultSet.getString("imdb_id"), "tt9419884");
                            Assert.assertEquals(resultSet.getString("title"), "Doctor Strange in the Multiverse of Madness");
                            Assert.assertEquals(resultSet.getInt("year"), 2022);
                            Assert.assertFalse(resultSet.getBoolean("was_released"));
                        }
                        else if (id == film2.getId()) {
                            Assert.assertEquals(resultSet.getString("imdb_id"), "tt10648342");
                            Assert.assertEquals(resultSet.getString("title"), "Thor: Love and Thunder");
                            Assert.assertEquals(resultSet.getInt("year"), 2022);
                            Assert.assertFalse(resultSet.getBoolean("was_released"));
                        }
                        count++;
                    }
                    Assert.assertEquals(count, 2);
                }
            }

            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM films WHERE id IN (?, ?)")) {
                statement.setLong(1, film1.getId());
                statement.setLong(2, film2.getId());
                statement.executeUpdate();
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testUpdate() {
        Film film = Film.builder()
                .imdbId("tt9419884")
                .title("Doctor Strange in the Multiverse of Madness")
                .year(2022)
                .wasReleased(false)
                .build();

        try (Connection connection = DATA_SOURCE.getConnection()) {
            try (PreparedStatement statement =
                         connection.prepareStatement("INSERT INTO films(imdb_id, title, year, was_released) VALUES (?, ?, ?, ?)",
                                                     Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, film.getImdbId());
                statement.setString(2, film.getTitle());
                statement.setLong(3, film.getYear());
                statement.setBoolean(4, film.isWasReleased());
                statement.executeUpdate();
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        film.setId(generatedKeys.getLong(1));
                    }
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }

        film.setImdbId("tt10648342");
        film.setTitle("Thor: Love and Thunder");
        film.setWasReleased(true);
        filmRepository.update(film);

        try (Connection connection = DATA_SOURCE.getConnection()) {
            try (PreparedStatement statement =
                         connection.prepareStatement("SELECT id, imdb_id, title, year, was_released FROM films WHERE id = ?")) {
                statement.setLong(1, film.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    Assert.assertTrue(resultSet.next());
                    Assert.assertEquals(resultSet.getString("imdb_id"), "tt10648342");
                    Assert.assertEquals(resultSet.getString("title"), "Thor: Love and Thunder");
                    Assert.assertEquals(resultSet.getInt("year"), 2022);
                    Assert.assertTrue(resultSet.getBoolean("was_released"));
                }
            }

            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM films WHERE id = ?")) {
                statement.setLong(1, film.getId());
                statement.executeUpdate();
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testUpdateAll() {
        Film film1 = Film.builder()
                .id(100L)
                .imdbId("tt9419884")
                .title("Doctor Strange in the Multiverse of Madness")
                .year(2022)
                .build();
        Film film2 = Film.builder()
                .id(101L)
                .imdbId("tt10648342")
                .title("Thor: Love and Thunder")
                .year(2022)
                .build();
        Film film3 = Film.builder()
                .id(102L)
                .imdbId("tt9114286")
                .title("Black Panther II")
                .year(2022)
                .build();

        try (Connection connection = DATA_SOURCE.getConnection()) {
            try (PreparedStatement statement =
                         connection.prepareStatement("INSERT INTO films(id, imdb_id, title, year) VALUES (?, ?, ?, ?)")) {
                statement.setLong(1, film1.getId());
                statement.setString(2, film1.getImdbId());
                statement.setString(3, film1.getTitle());
                statement.setLong(4, film1.getYear());
                statement.executeUpdate();

                statement.setLong(1, film2.getId());
                statement.setString(2, film2.getImdbId());
                statement.setString(3, film2.getTitle());
                statement.setLong(4, film2.getYear());
                statement.executeUpdate();

                statement.setLong(1, film3.getId());
                statement.setString(2, film3.getImdbId());
                statement.setString(3, film3.getTitle());
                statement.setLong(4, film3.getYear());
                statement.executeUpdate();
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }

        film1.setImdbId("tt10676048");
        film1.setTitle("Captain Marvel 2");

        film2.setImdbId("tt11213558");
        film2.setTitle("Ant-Man and the Wasp: Quantumania");

        film3.setImdbId("tt6791350");
        film3.setTitle("Guardians of the Galaxy Vol. 3");
        film3.setYear(2023);

        filmRepository.updateAll(film1, film2, film3);

        try (Connection connection = DATA_SOURCE.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet =
                             statement.executeQuery("SELECT id, imdb_id, title, year FROM films WHERE id IN (1, 2, 3)")) {
                    while (resultSet.next()) {
                        long id = resultSet.getLong("id");
                        if (id == film1.getId()) {
                            Assert.assertEquals(resultSet.getString("imdb_id"), "tt10676048");
                            Assert.assertEquals(resultSet.getString("title"), "Captain Marvel 2");
                            Assert.assertEquals(resultSet.getInt("year"), 2022);
                        }
                        else if (id == film2.getId()) {
                            Assert.assertEquals(resultSet.getString("imdb_id"), "tt11213558");
                            Assert.assertEquals(resultSet.getString("title"), "Ant-Man and the Wasp: Quantumania");
                            Assert.assertEquals(resultSet.getInt("year"), 2022);
                        }
                        else if (id == film3.getId()) {
                            Assert.assertEquals(resultSet.getString("imdb_id"), "tt6791350");
                            Assert.assertEquals(resultSet.getString("title"), "Guardians of the Galaxy Vol. 3");
                            Assert.assertEquals(resultSet.getInt("year"), 2023);
                        }
                    }
                }
            }

            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM films WHERE id IN (?, ?, ?)")) {
                statement.setLong(1, film1.getId());
                statement.setLong(2, film2.getId());
                statement.setLong(3, film3.getId());
                statement.executeUpdate();
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testDelete() {
        Film film = Film.builder()
                .id(100L)
                .imdbId("tt6791350")
                .title("Guardians of the Galaxy Vol. 3")
                .year(2023)
                .build();

        try (Connection connection = DATA_SOURCE.getConnection()) {
            try (PreparedStatement statement =
                         connection.prepareStatement("INSERT INTO films(id, imdb_id, title, year) VALUES (?, ?, ?, ?)")) {
                statement.setLong(1, film.getId());
                statement.setString(2, film.getImdbId());
                statement.setString(3, film.getTitle());
                statement.setLong(4, film.getYear());
                statement.executeUpdate();
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }

        filmRepository.delete(film);

        try (Connection connection = DATA_SOURCE.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM films WHERE id = ?")) {
                statement.setLong(1, 100L);
                try (ResultSet resultSet = statement.executeQuery()) {
                    Assert.assertFalse(resultSet.next());
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testDeleteById() {
        Film film = Film.builder()
                .id(100L)
                .imdbId("tt9419884")
                .title("Doctor Strange in the Multiverse of Madness")
                .year(2022)
                .build();

        try (Connection connection = DATA_SOURCE.getConnection()) {
            try (PreparedStatement statement =
                         connection.prepareStatement("INSERT INTO films(id, imdb_id, title, year) VALUES (?, ?, ?, ?)")) {
                statement.setLong(1, film.getId());
                statement.setString(2, film.getImdbId());
                statement.setString(3, film.getTitle());
                statement.setLong(4, film.getYear());
                statement.executeUpdate();
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }

        filmRepository.deleteById(film.getId());

        try (Connection connection = DATA_SOURCE.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM films WHERE id = ?")) {
                statement.setLong(1, film.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    Assert.assertFalse(resultSet.next());
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testDeleteAll() {
        Film film1 = Film.builder()
                .id(100L)
                .imdbId("tt9419884")
                .title("Doctor Strange in the Multiverse of Madness")
                .year(2022)
                .build();
        Film film2 = Film.builder()
                .id(101L)
                .imdbId("tt10648342")
                .title("Thor: Love and Thunder")
                .year(2022)
                .build();
        Film film3 = Film.builder()
                .id(102L)
                .imdbId("tt9114286")
                .title("Black Panther II")
                .year(2022)
                .build();

        try (Connection connection = DATA_SOURCE.getConnection()) {
            try (PreparedStatement statement =
                         connection.prepareStatement("INSERT INTO films(id, imdb_id, title, year) VALUES (?, ?, ?, ?)")) {
                statement.setLong(1, film1.getId());
                statement.setString(2, film1.getImdbId());
                statement.setString(3, film1.getTitle());
                statement.setLong(4, film1.getYear());
                statement.executeUpdate();

                statement.setLong(1, film2.getId());
                statement.setString(2, film2.getImdbId());
                statement.setString(3, film2.getTitle());
                statement.setLong(4, film2.getYear());
                statement.executeUpdate();

                statement.setLong(1, film3.getId());
                statement.setString(2, film3.getImdbId());
                statement.setString(3, film3.getTitle());
                statement.setLong(4, film3.getYear());
                statement.executeUpdate();
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }

        filmRepository.deleteAll(film1, film2, film3);

        try (Connection connection = DATA_SOURCE.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM films WHERE id IN (?, ?, ?)")) {
                statement.setLong(1, film1.getId());
                statement.setLong(2, film2.getId());
                statement.setLong(3, film3.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    Assert.assertFalse(resultSet.next());
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testDeleteAllByYearIn() {
        Film film1 = Film.builder()
                .id(100L)
                .imdbId("tt9419884")
                .title("Doctor Strange in the Multiverse of Madness")
                .year(2022)
                .build();
        Film film2 = Film.builder()
                .id(101L)
                .imdbId("tt10648342")
                .title("Thor: Love and Thunder")
                .year(2022)
                .build();
        Film film3 = Film.builder()
                .id(102L)
                .imdbId("tt9114286")
                .title("Black Panther II")
                .year(2022)
                .build();
        Film film4 = Film.builder()
                .id(103L)
                .imdbId("tt6791350")
                .title("Guardians of the Galaxy Vol. 3")
                .year(2023)
                .build();

        try (Connection connection = DATA_SOURCE.getConnection()) {
            try (PreparedStatement statement =
                         connection.prepareStatement("INSERT INTO films(id, imdb_id, title, year) VALUES (?, ?, ?, ?)")) {
                statement.setLong(1, film1.getId());
                statement.setString(2, film1.getImdbId());
                statement.setString(3, film1.getTitle());
                statement.setLong(4, film1.getYear());
                statement.executeUpdate();

                statement.setLong(1, film2.getId());
                statement.setString(2, film2.getImdbId());
                statement.setString(3, film2.getTitle());
                statement.setLong(4, film2.getYear());
                statement.executeUpdate();

                statement.setLong(1, film3.getId());
                statement.setString(2, film3.getImdbId());
                statement.setString(3, film3.getTitle());
                statement.setLong(4, film3.getYear());
                statement.executeUpdate();

                statement.setLong(1, film4.getId());
                statement.setString(2, film4.getImdbId());
                statement.setString(3, film4.getTitle());
                statement.setLong(4, film4.getYear());
                statement.executeUpdate();
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }

        filmRepository.deleteAllByYearIn(new HashSet<>(Arrays.asList(2022, 2023)));

        try (Connection connection = DATA_SOURCE.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery("SELECT id, title, year FROM films WHERE year IN (2022, 2023)")) {
                    Assert.assertFalse(resultSet.next());
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
