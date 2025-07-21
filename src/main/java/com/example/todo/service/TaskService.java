package com.example.todo.service;

import com.example.todo.entity.Task;
import com.example.todo.entity.User;
import com.example.todo.repository.TaskRepository;
import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;

import javax.transaction.Transactional;
import java.util.List;

@AllArgsConstructor
public class TaskService {
    private final TaskRepository taskRepo;
    private final UserService userService;

    public List<Task> getTasksForUser(String username) {
        return taskRepo.findByOwnerUsername(username);
    }

    @Transactional
    public Task create(Task task, String username) {
        User owner = userService.findByUsername(username).orElseThrow();
        task.setOwner(owner);
        return taskRepo.save(task);
    }

    @Transactional
    public ResponseEntity<?> update(Long id, Task task, String username) {
        Task existing = taskRepo.findById(id).orElseThrow();
        if (!existing.getOwner().getUsername().equals(username)) return ResponseEntity.status(403).build();
        existing.setTitle(task.getTitle());
        existing.setDescription(task.getDescription());
        existing.setDone(task.isDone());
        return ResponseEntity.ok(taskRepo.save(existing));
    }

    @Transactional
    public ResponseEntity<?> delete(Long id, String username) {
        Task existing = taskRepo.findById(id).orElseThrow();
        if (!existing.getOwner().getUsername().equals(username)) return ResponseEntity.status(403).build();
        taskRepo.delete(existing);
        return ResponseEntity.noContent().build();
    }
}