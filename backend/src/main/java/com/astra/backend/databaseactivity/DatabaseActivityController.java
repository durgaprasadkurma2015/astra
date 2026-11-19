package com.astra.backend.databaseactivity;


import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/database-activity")
public class DatabaseActivityController {

private final DatabaseActivityService service;

public DatabaseActivityController(DatabaseActivityService service) {
this.service = service;
}

@GetMapping("/stats")
public Map<String, Object> getStats() {
return service.getStats();
}

@GetMapping("/logs")
public Map<String, Object> getLogs(
@RequestParam(defaultValue = "0") int page,
@RequestParam(defaultValue = "20") int size,
@RequestParam(required = false) String table,
@RequestParam(required = false) String operation,
@RequestParam(defaultValue = "24H") String range,
@RequestParam(required = false) String search) {

return service.getLogs(
page,
size,
table,
operation,
range,
search
);
}

@GetMapping("/tables")
public List<String> getTables() {
return service.getTables();
}
}