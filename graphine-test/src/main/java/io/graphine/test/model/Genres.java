package io.graphine.test.model;

import io.graphine.annotation.AttributeMapper;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Oleg Marchenko
 */
@EqualsAndHashCode
@ToString
public class Genres implements Iterable<String> {
    private final List<String> values;

    public Genres(String... values) {
        this.values = Arrays.stream(values)
                            .sorted()
                            .collect(Collectors.toList());
    }

    public Stream<String> stream() {
        return values.stream();
    }

    @Override
    public Iterator<String> iterator() {
        return values.iterator();
    }

    @AttributeMapper(Genres.class)
    public static class Mapper {
        public static Genres getGenres(ResultSet resultSet, int columnIndex) throws SQLException {
            String columnValue = resultSet.getString(columnIndex);
            if (columnValue == null || columnValue.isEmpty()) {
                return null;
            }

            String[] values = columnValue.split(",");
            return new Genres(values);
        }

        public static void setGenres(PreparedStatement statement, int columnIndex, Genres genres) throws SQLException {
            if (genres == null) {
                statement.setNull(columnIndex, Types.VARCHAR);
            }
            else {
                String joinedValues = genres.stream().collect(Collectors.joining(","));
                statement.setString(columnIndex, joinedValues);
            }
        }
    }
}
