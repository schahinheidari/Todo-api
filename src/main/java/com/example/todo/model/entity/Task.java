package com.example.todo.model.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Task {
    @Id
    @GeneratedValue
    private Long id;

    private String title;
    private String description;
    private boolean done;

    @ManyToOne
    private User owner;
}