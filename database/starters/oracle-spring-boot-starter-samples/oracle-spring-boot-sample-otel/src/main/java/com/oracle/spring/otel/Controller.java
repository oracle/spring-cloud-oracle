// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.otel;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class Controller {
    public Controller(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public record CreateIceCreamFlavorRequest(String flavor) {}

    public record IceCreamFlavor(long id, String flavor) {}

    private final JdbcClient jdbcClient;

    @GetMapping("/flavors")
    public List<IceCreamFlavor> getIceCreamFlavors() {
        return jdbcClient.sql("select * from ice_cream_flavors")
                .query(IceCreamFlavor.class)
                .list();
    }

    @PostMapping("/flavors")
    public IceCreamFlavor createIceCreamFlavor(@RequestBody CreateIceCreamFlavorRequest request) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("insert into ice_cream_flavors (flavor) values (?)")
                .param(request.flavor)
                .update(keyHolder, "id");

        return jdbcClient.sql("select * from ice_cream_flavors where id = ?")
                .param(keyHolder.getKey().longValue())
                .query(IceCreamFlavor.class)
                .single();

    }
}
