package com.demo;

import java.time.LocalDateTime;

public class Task {
    private static int counter = 1;

    private int id;
    private String title;
    private String status; // todo | in-progress | done
    private LocalDateTime created;

    public Task() {}

    public Task(String title) {
        this.id      = counter++;
        this.title   = title;
        this.status  = "todo";
        this.created = LocalDateTime.now();
    }

    // Getters & Setters
    public int getId()                   { return id; }
    public void setId(int id)            { this.id = id; }
    public String getTitle()             { return title; }
    public void setTitle(String title)   { this.title = title; }
    public String getStatus()            { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreated()    { return created; }
    public void setCreated(LocalDateTime created) { this.created = created; }
}
