package com.oracle.spring.json.duality.model.movie;

import java.util.Objects;
import java.util.Set;

import com.oracle.spring.json.duality.annotation.JsonRelationalDualityViewEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "movie")
@Getter
@Setter
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_id")
    private Long movieId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "release_year")
    private Integer releaseYear;

    @Column(name = "genre", length = 50)
    private String genre;

    @ManyToOne
    @JoinColumn(name = "director_id")
    @JsonRelationalDualityViewEntity(entity = Director.class)
    private Director director;

    @ManyToMany
    @JsonRelationalDualityViewEntity(
            entity = Actor.class
    )
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
}
