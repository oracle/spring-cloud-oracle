// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.spatial;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LandmarkController {
    private final LandmarkService landmarkService;

    public LandmarkController(LandmarkService landmarkService) {
        this.landmarkService = landmarkService;
    }

    @PostMapping("/landmarks")
    public Landmark create(@RequestBody Landmark landmark) {
        return landmarkService.create(landmark);
    }

    @GetMapping("/landmarks/{id}")
    public Landmark getById(@PathVariable Long id) {
        return landmarkService.getById(id);
    }

    @GetMapping("/landmarks/near")
    public List<Landmark> near(@RequestParam String geometry,
                               @RequestParam(required = false) Integer distance,
                               @RequestParam(required = false) Integer limit) {
        return landmarkService.findNear(geometry, distance, limit);
    }

    @PostMapping("/landmarks/within")
    public List<Landmark> within(@RequestBody WithinLandmarkRequest request) {
        return landmarkService.findWithin(request.geometry(), request.mask());
    }
}
