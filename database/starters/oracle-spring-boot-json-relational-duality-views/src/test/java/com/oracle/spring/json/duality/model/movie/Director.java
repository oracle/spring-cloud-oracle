// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.json.duality.model.movie;

import java.util.Objects;
import java.util.Set;

import com.oracle.spring.json.duality.annotation.JsonRelationalDualityView;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;
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

import static com.oracle.spring.json.duality.builder.Annotations._ID_FIELD;

@Entity
@Table(name = "director")
@Getter
@Setter
public class Director {
    @JsonbProperty(_ID_FIELD)
    @Id
    @Column(name = "director_id")
    private String directorId;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @JsonbTransient
    @OneToMany(mappedBy = "director") // Reference related entity's associated field
    private Set<Movie> movies;

    @OneToOne(
            mappedBy = "director", // Reference related entity's associated field
            cascade = CascadeType.ALL, // Cascade persistence to the mapped entity
            orphanRemoval = true // Remove director bio from director if deleted
    )
    // The primary key of the Director entity is used as the foreign key of the DirectorBio entity.
    @PrimaryKeyJoinColumn
    @JsonbTransient
    //@JsonRelationalDualityView(name = "directorBio", accessMode = @AccessMode(insert = true))
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
