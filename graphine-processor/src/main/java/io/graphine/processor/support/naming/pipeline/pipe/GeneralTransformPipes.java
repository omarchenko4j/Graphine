package io.graphine.processor.support.naming.pipeline.pipe;

import static io.graphine.processor.util.StringUtils.join;

/**
 * @author Oleg Marchenko
 */
public enum GeneralTransformPipes implements TransformPipe {
    SNAKE_CASE {
        @Override
        public String transform(String value) {
            return join(value.split("(?=\\p{Lu})"), "_");
        }
    },
    LOWER_CASE {
        @Override
        public String transform(String value) {
            return value.toLowerCase();
        }
    },
    UPPER_CASE {
        @Override
        public String transform(String value) {
            return value.toUpperCase();
        }
    }
}
