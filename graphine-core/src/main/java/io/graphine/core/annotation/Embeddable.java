package io.graphine.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * @author Oleg Marchenko
 */
@Retention(SOURCE)
@Target(TYPE)
public @interface Embeddable {
}
