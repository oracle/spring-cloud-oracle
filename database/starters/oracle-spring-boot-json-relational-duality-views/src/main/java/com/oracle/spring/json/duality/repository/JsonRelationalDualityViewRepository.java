package com.oracle.spring.json.duality.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.ListCrudRepository;

public interface JsonRelationalDualityViewRepository<T, ID> extends ListCrudRepository<T, ID> {
}
