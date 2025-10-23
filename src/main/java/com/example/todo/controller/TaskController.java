package com.example.todo.controller;

import com.example.todo.model.dto.TaskDTO;
import com.example.todo.model.entity.Task;
import com.example.todo.service.TaskService;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/tasks")
@AllArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping("/list")
    public List<TaskDTO> listTasks(Principal principal) {
        List<TaskDTO> taskForUser = taskService.getTasksForUser(principal.getName());
        return taskForUser;
    }

    @PostMapping("/create")
    public ResponseEntity<TaskDTO> create(@RequestBody TaskDTO task, Principal principal) {
        TaskDTO task1 = taskService.create(task, principal.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(task1);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Task task, Principal principal) {
        return taskService.update(id, task, principal.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Principal principal) {
        return taskService.delete(id, principal.getName());
    }
}