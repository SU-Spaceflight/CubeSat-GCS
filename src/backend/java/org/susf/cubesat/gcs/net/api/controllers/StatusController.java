package org.susf.cubesat.gcs.net.api.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class StatusController {

    @GetMapping("/api/status")
    String status() { return "ok"; }
}