package io.graphine.test.model;

import io.graphine.core.annotation.Attribute;
import io.graphine.core.annotation.AttributeOverride;
import io.graphine.core.annotation.Entity;
import io.graphine.core.annotation.Id;
import lombok.*;

/**
 * @author Oleg Marchenko
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
@Entity
public class Film {
    @Id
    private Long id;
    private String imdbId;
    private String title;
    private int year;
    @AttributeOverride(name = "value", attribute = @Attribute(column = "rating_value"))
    @AttributeOverride(name = "count", attribute = @Attribute(column = "rating_count"))
    private Rating rating;
    private Long budget;
    private Long gross;
    private String tagline;
    private boolean wasReleased;
}
