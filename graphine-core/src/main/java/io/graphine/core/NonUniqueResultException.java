package io.graphine.core;

/**
 * @author Oleg Marchenko
 */
public final class NonUniqueResultException extends GraphineException {
    public NonUniqueResultException() {
        super("Query returned a non-unique result");
    }
}
