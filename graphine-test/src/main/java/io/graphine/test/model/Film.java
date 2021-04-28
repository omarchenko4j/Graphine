package io.graphine.test.model;

import io.graphine.core.annotation.Attribute;
import io.graphine.core.annotation.Entity;
import io.graphine.core.annotation.Id;
import lombok.*;

/**
 * @author Oleg Marchenko
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Entity(table = "films")
public class Film {
    @Id
    private Long id;
    @Attribute(column = "imdb_id")
    private String imdbId;
    @Attribute
    private String title;
    @Attribute
    private int year;
    @Attribute
    private Long budget;
    @Attribute
    private Long gross;
    @Attribute
    private String tagline;
    @Attribute(column = "was_released")
    private boolean wasReleased;
}
