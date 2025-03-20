// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.cloud.spring.sample.nosql.springcloudnosqlsample;

import com.oracle.nosql.spring.data.repository.NosqlRepository;

public interface BookRepository extends NosqlRepository<Book, Long> {
    Iterable<Book> findByAuthor(String author);
    Iterable<Book> findByTitle(String title);
}