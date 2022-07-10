package io.graphine.test.model;

import io.graphine.annotation.Embeddable;
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
@Embeddable
public class Rating {
    private float value;
    private long count;
}
