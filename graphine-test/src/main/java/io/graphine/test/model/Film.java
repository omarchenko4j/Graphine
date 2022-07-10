package io.graphine.test.model;

import io.graphine.annotation.Attribute;
import io.graphine.annotation.AttributeOverride;
import io.graphine.annotation.Entity;
import io.graphine.annotation.Id;
import lombok.*;

import java.util.UUID;

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
    @Builder.Default
    private UUID id = UUID.randomUUID();
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
