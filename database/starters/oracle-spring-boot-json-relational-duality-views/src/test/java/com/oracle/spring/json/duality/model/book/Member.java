// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.json.duality.model.book;

import java.util.List;

import com.oracle.spring.json.duality.annotation.AccessMode;
import com.oracle.spring.json.duality.annotation.JsonRelationalDualityView;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import static com.oracle.spring.json.duality.builder.Annotations._ID_FIELD;

@Entity
@Table(name = "members")
@JsonRelationalDualityView(accessMode = @AccessMode(
        insert = true,
        update = true,
        delete = true
))
@Getter
@Setter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonbProperty(_ID_FIELD)
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "name", nullable = false)
    private String fullName;

    @JsonRelationalDualityView(name = "loans", accessMode = @AccessMode(
            insert = true,
            update = true
    ))
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Loan> loans;
}
