package io.graphine.core;

/**
 * @author Oleg Marchenko
 */
public class GraphineException extends RuntimeException {
    public GraphineException(String message) {
        super(message);
    }

    public GraphineException(Exception e) {
        super(e);
    }
}
