package com.oracle.spring.json.duality.model.movie;

import java.util.Objects;
import java.util.Set;

import com.oracle.spring.json.duality.annotation.JsonRelationalDualityView;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "director")
@Getter
@Setter
public class Director {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "director_id")
    private Long directorId;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @JsonRelationalDualityView
    @OneToMany(mappedBy = "director") // Reference related entity's associated field
    private Set<Movie> movies;

    @OneToOne(
            mappedBy = "director", // Reference related entity's associated field
            cascade = CascadeType.ALL, // Cascade persistence to the mapped entity
            orphanRemoval = true // Remove director bio from director if deleted
    )
    // The primary key of the Director entity is used as the foreign key of the DirectorBio entity.
    @PrimaryKeyJoinColumn
    @JsonRelationalDualityView
    private DirectorBio directorBio;

    public void setDirectorBio(DirectorBio directorBio) {
        this.directorBio = directorBio;
        if (directorBio != null) {
            directorBio.setDirector(this);
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Director director)) return false;

        return Objects.equals(getDirectorId(), director.getDirectorId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getDirectorId());
    }
}
