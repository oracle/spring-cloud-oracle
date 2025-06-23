// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.json.duality.model.movie;

import java.util.Objects;
import java.util.Set;

import com.oracle.spring.json.duality.annotation.AccessMode;
import com.oracle.spring.json.duality.annotation.JsonRelationalDualityView;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "movie")
@JsonRelationalDualityView(name = "movie_dv", accessMode = @AccessMode(
        insert = true
))
public class Movie {
    @Id
    @Column(name = "movie_id")
    @JsonbProperty("_id")
    private String movieId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "release_year")
    private Integer releaseYear;

    @Column(name = "genre", length = 50)
    private String genre;

    @ManyToOne
    @JoinColumn(name = "director_id")
    //@JsonRelationalDualityView(accessMode = @AccessMode(insert = true))
    @JsonbTransient
    private Director director;

    @ManyToMany
    @JsonbTransient
    @JoinTable(
            name = "movie_actor",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "actor_id")
    )
    private Set<Actor> actors;

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Movie movie)) return false;

        return Objects.equals(movieId, movie.movieId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(movieId);
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Director getDirector() {
        return director;
    }

    public void setDirector(Director director) {
        this.director = director;
    }

    public Set<Actor> getActors() {
        return actors;
    }

    public void setActors(Set<Actor> actors) {
        this.actors = actors;
    }
}
