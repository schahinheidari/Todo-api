package com.example.todo.service;

import com.example.todo.model.dto.TaskDTO;
import com.example.todo.model.entity.Role;
import com.example.todo.model.entity.Task;
import com.example.todo.model.entity.User;
import com.example.todo.model.mapper.TaskMapper;
import com.example.todo.repository.TaskRepository;
import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@AllArgsConstructor
@Service
@Transactional
public class TaskService {
    private final TaskRepository taskRepo;
    private final UserService userService;
    private final TaskMapper taskMapper;

    public List<TaskDTO> getTasksForUser(String username) {
        return taskRepo.findByOwnerUsername(username);
    }

    public TaskDTO create(TaskDTO taskDTO, String username) {
        User owner = userService.findByUsername(username).orElseThrow();
        Task task = taskMapper.taskDTOToTask(taskDTO);

        task.setOwner(owner);

        if (taskDTO.isDone() && !hasAdminRole(owner)){
            task.setDone(false);
        }
        Task savedTask = taskRepo.save(task);
        return taskMapper.taskToTaskDTO(savedTask);
    }

    public ResponseEntity<?> update(Long id, Task task, String username) {
        Task existing = taskRepo.findById(id).orElseThrow();
        if (!existing.getOwner().getUsername().equals(username)) return ResponseEntity.status(403).build();
        existing.setTitle(task.getTitle());
        existing.setDescription(task.getDescription());
        existing.setDone(task.isDone());
        return ResponseEntity.ok(taskRepo.save(existing));
    }

    public ResponseEntity<?> delete(Long id, String username) {
        Task existing = taskRepo.findById(id).orElseThrow();
        if (!existing.getOwner().getUsername().equals(username)) return ResponseEntity.status(403).build();
        taskRepo.delete(existing);
        return ResponseEntity.noContent().build();
    }

    public boolean hasAdminRole(User user){
        return user.getRoles().contains(Role.ROLE_ADMIN);
    }

}