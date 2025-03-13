package com.oracle.spring.json.duality.model.movie;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.oracle.spring.json.duality.annotation.JsonRelationalDualityView;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import static com.oracle.spring.json.duality.builder.Annotations._ID_FIELD;

@Entity
@Table(name = "actor")
@Getter
@Setter
@JsonRelationalDualityView
public class Actor {
    @JsonbProperty(_ID_FIELD)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @ManyToMany(mappedBy = "actors")
    @JsonRelationalDualityView
    private Set<Movie> movies;

    /**
     * Adds an Actor to a movie, maintaining bidirectional integrity.
     * @param movie to add the Actor into.
     */
    public void addMovie(Movie movie) {
        if (movies == null) {
            movies = new HashSet<>();
        }
        movies.add(movie);
        movie.getActors().add(this);
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Actor actor)) return false;

        return Objects.equals(getActorId(), actor.getActorId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getActorId());
    }
}
