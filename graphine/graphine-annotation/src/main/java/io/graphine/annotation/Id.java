package io.graphine.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * @author Oleg Marchenko
 */
@Retention(SOURCE)
@Target(FIELD)
public @interface Id {
}
