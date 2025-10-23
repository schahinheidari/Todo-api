package com.example.todo.service;

import com.example.todo.exception.ForbiddenException;
import com.example.todo.exception.NotFoundException;
import com.example.todo.model.dto.TaskDTO;
import com.example.todo.model.entity.Role;
import com.example.todo.model.entity.Task;
import com.example.todo.model.entity.User;
import com.example.todo.model.mapper.TaskMapper;
import com.example.todo.repository.TaskRepository;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
@Transactional
public class TaskService {
    private final TaskRepository taskRepo;
    private final UserService userService;
    private final TaskMapper taskMapper;

    public List<TaskDTO> getTasksForUser(String username) {
        List<Task> taskList = taskRepo.findByOwnerUsername(username);
        if (taskList == null || taskList.isEmpty()) {
            throw new NotFoundException("Can't find any tasks for the given username" + username);
        }
        return taskList.stream().map(taskMapper::taskToTaskDTO).collect(Collectors.toList());
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

    public ResponseEntity<TaskDTO> update(Long id, TaskDTO taskDTO, String username) {
        Task existing = taskRepo.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("Can't find the task with id " + id)
                );

        User currentUser = userService.findByUsername(username)
                .orElseThrow(
                        () -> new NotFoundException("Can't find the user with username " + username)
                );
        if (!existing.getOwner().getUsername().equals(username)) {
            throw new ForbiddenException("You do not have permission to update this task, only the owner can update this task");
        }
        existing.setTitle(taskDTO.getTitle());
        existing.setDescription(taskDTO.getDescription());

        // done = true Admin
        if (taskDTO.isDone() && !hasAdminRole(currentUser)) {
            throw new ForbiddenException("You don't have permission to update this task, only the owner can  can set done=true");
        } else {
        existing.setDone(taskDTO.isDone());
        }

        Task savedTask = taskRepo.save(existing);
        return ResponseEntity.ok(taskMapper.taskToTaskDTO(savedTask));
    }

    public ResponseEntity<?> delete(Long id, String username) {
        Task existing = taskRepo.findById(id).orElseThrow();
        if (!existing.getOwner().getUsername().equals(username))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        taskRepo.delete(existing);
        return ResponseEntity.noContent().build();
    }

    public boolean hasAdminRole(User user){
        return user.getRoles().contains(Role.ROLE_ADMIN);
    }

}