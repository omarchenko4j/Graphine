CREATE SEQUENCE seq_films_id START WITH 1;

CREATE TABLE films (
    id BIGINT NOT NULL DEFAULT nextval('seq_films_id'),
    imdb_id TEXT NOT NULL,
    title TEXT NOT NULL,
    year INTEGER NOT NULL,
    budget BIGINT,
    gross BIGINT,
    tagline TEXT,
    was_released BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_films_id PRIMARY KEY (id),
    CONSTRAINT uk_films_imdb_id UNIQUE (imdb_id)
);
