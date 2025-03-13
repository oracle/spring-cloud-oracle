package com.oracle.spring.json.duality.model.movie;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "director_bio")
@Getter
@Setter
public class DirectorBio {

    @Id
    @Column(name = "director_id")
    private Long directorId;

    @OneToOne(fetch = FetchType.LAZY)
    // The primary key will be copied from the director entity
    @MapsId
    @JoinColumn(name = "director_id")
    private Director director;

    @Column(name = "biography", columnDefinition = "CLOB")
    private String biography;

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof DirectorBio directorBio)) return false;
        return Objects.equals(getDirectorId(), directorBio.getDirectorId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getDirectorId());
    }
}
