package com.example.todo.repository;

import com.example.todo.model.dto.TaskDTO;
import com.example.todo.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<TaskDTO> findByOwnerUsername(String username);
}