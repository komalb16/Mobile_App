package com.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TaskController {

    private final List<Task> tasks = new CopyOnWriteArrayList<>();

    public TaskController() {
        // Seed data
        Task t1 = new Task("Set up the server");   t1.setStatus("done");
        Task t2 = new Task("Build the REST API");  t2.setStatus("done");
        Task t3 = new Task("Design the UI");       t3.setStatus("in-progress");
        Task t4 = new Task("Write unit tests");
        tasks.addAll(List.of(t1, t2, t3, t4));
    }

    @GetMapping("/tasks")
    public List<Task> getAllTasks() {
        return tasks;
    }

    @PostMapping("/tasks")
    public ResponseEntity<Task> createTask(@RequestBody Map<String, String> body) {
        String title = body.get("title");
        if (title == null || title.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Task task = new Task(title.trim());
        tasks.add(task);
        return ResponseEntity.status(201).body(task);
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable int id) {
        boolean removed = tasks.removeIf(t -> t.getId() == id);
        return removed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PatchMapping("/tasks/{id}")
    public ResponseEntity<Task> updateStatus(@PathVariable int id,
                                             @RequestBody Map<String, String> body) {
        return tasks.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .map(t -> { t.setStatus(body.getOrDefault("status", t.getStatus())); return ResponseEntity.ok(t); })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/info")
    public Map<String, String> info() {
        return Map.of(
            "app",     "Demo App",
            "version", "1.0.0",
            "java",    System.getProperty("java.version"),
            "os",      System.getProperty("os.name")
        );
    }
}
