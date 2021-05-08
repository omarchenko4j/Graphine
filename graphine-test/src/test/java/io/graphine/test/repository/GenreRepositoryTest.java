package io.graphine.test.repository;

import io.graphine.test.model.Genre;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.UUID;

import static io.graphine.test.util.DataSourceProvider.DATA_SOURCE;
import static io.graphine.test.util.DataSourceProvider.PROXY_DATA_SOURCE;

/**
 * @author Oleg Marchenko
 */
public class GenreRepositoryTest {

    @BeforeAll
    @SneakyThrows
    public static void createTable() {
        @Cleanup Connection connection = DATA_SOURCE.getConnection();
        @Cleanup Statement statement = connection.createStatement();
        statement.executeQuery("CREATE TABLE public.genre(id TEXT PRIMARY KEY, name TEXT NOT NULL)");
    }

    @AfterEach
    @SneakyThrows
    public void cleanTable() {
        @Cleanup Connection connection = DATA_SOURCE.getConnection();
        @Cleanup Statement statement = connection.createStatement();
        statement.executeQuery("TRUNCATE TABLE public.genre");
    }

    @AfterAll
    @SneakyThrows
    public static void dropTable() {
        @Cleanup Connection connection = DATA_SOURCE.getConnection();
        @Cleanup Statement statement = connection.createStatement();
        statement.executeQuery("DROP TABLE public.genre");
    }

    private final GenreRepository genreRepository = new GraphineGenreRepository(PROXY_DATA_SOURCE);

    @Test
    public void testSaveMethod() {
        Genre genre = Genre.builder()
                           .id(UUID.randomUUID())
                           .name("Comedy")
                           .build();
        genreRepository.save(genre);

        Genre savedGenre = selectGenreById(genre.getId());
        Assertions.assertEquals(savedGenre, genre);
    }

    @Test
    public void testSaveAllMethod() {
        Genre genre1 = Genre.builder()
                            .id(UUID.randomUUID())
                            .name("Comedy")
                            .build();
        Genre genre2 = Genre.builder()
                            .id(UUID.randomUUID())
                            .name("Horror")
                            .build();
        Genre genre3 = Genre.builder()
                            .id(UUID.randomUUID())
                            .name("Thriller")
                            .build();
        genreRepository.saveAll(Arrays.asList(genre1, genre2, genre3));

        Genre savedGenre1 = selectGenreById(genre1.getId());
        Assertions.assertEquals(savedGenre1, genre1);

        Genre savedGenre2 = selectGenreById(genre2.getId());
        Assertions.assertEquals(savedGenre2, genre2);

        Genre savedGenre3 = selectGenreById(genre3.getId());
        Assertions.assertEquals(savedGenre3, genre3);
    }

    @SneakyThrows
    public static Genre selectGenreById(UUID id) {
        @Cleanup Connection connection = DATA_SOURCE.getConnection();
        @Cleanup PreparedStatement statement =
                connection.prepareStatement("SELECT id, name FROM public.genre WHERE id = ?");
        statement.setString(1, id.toString());
        @Cleanup ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return Genre.builder()
                        .id(UUID.fromString(resultSet.getString("id")))
                        .name(resultSet.getString("name"))
                        .build();
        }
        return null;
    }
}
