package io.graphine.test.model;

import io.graphine.core.annotation.Attribute;
import io.graphine.core.annotation.Entity;
import io.graphine.core.annotation.Id;

import java.util.Objects;

/**
 * @author Oleg Marchenko
 */
@Entity(table = "films")
public class Film {
    @Id
    private Long id;
    @Attribute(column = "imdb_id")
    private String imdbId;
    @Attribute
    private String title;
    @Attribute
    private int year;
    @Attribute
    private Long budget;
    @Attribute
    private Long gross;
    @Attribute
    private String tagline;
    @Attribute(column = "was_released")
    private boolean wasReleased;

    public Film() {
    }

    public Film(String imdbId, String title, Integer year, boolean wasReleased) {
        this(null, imdbId, title, year, wasReleased);
    }

    public Film(Long id, String imdbId, String title, Integer year) {
        this(id, imdbId, title, year, false);
    }

    public Film(Long id, String imdbId, String title, int year, boolean wasReleased) {
        this.id = id;
        this.imdbId = imdbId;
        this.title = title;
        this.year = year;
        this.wasReleased = wasReleased;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImdbId() {
        return imdbId;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Long getBudget() {
        return budget;
    }

    public void setBudget(Long budget) {
        this.budget = budget;
    }

    public Long getGross() {
        return gross;
    }

    public void setGross(Long gross) {
        this.gross = gross;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public boolean isWasReleased() {
        return wasReleased;
    }

    public void setWasReleased(boolean wasReleased) {
        this.wasReleased = wasReleased;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Film film = (Film) o;
        return year == film.year &&
                wasReleased == film.wasReleased &&
                imdbId.equals(film.imdbId) &&
                title.equals(film.title) &&
                Objects.equals(budget, film.budget) &&
                Objects.equals(gross, film.gross) &&
                Objects.equals(tagline, film.tagline);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imdbId, title, year, budget, gross, tagline, wasReleased);
    }

    @Override
    public String toString() {
        return "Film{" +
                "id=" + id +
                ", imdbId='" + imdbId + '\'' +
                ", title='" + title + '\'' +
                ", year=" + year +
                ", budget=" + budget +
                ", gross=" + gross +
                ", tagline='" + tagline + '\'' +
                ", wasReleased=" + wasReleased +
                '}';
    }
}
