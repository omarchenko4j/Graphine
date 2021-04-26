package io.graphine.core;

/**
 * @author Oleg Marchenko
 */
public class GraphineException extends RuntimeException {
    public GraphineException(Exception e) {
        super(e);
    }
}
