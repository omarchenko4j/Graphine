package io.graphine.test.model;

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
@Entity
public class Film {
    @Id
    private Long id;
    private String imdbId;
    private String title;
    private int year;
    private Long budget;
    private Long gross;
    private String tagline;
    private boolean wasReleased;
}
