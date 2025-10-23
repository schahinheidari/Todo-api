package com.example.todo.model.mapper;

import com.example.todo.model.dto.TaskDTO;
import com.example.todo.model.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {
    public Task taskDTOToTask(TaskDTO taskDTO){
        if(taskDTO == null){
            return null;
        }
        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setDone(taskDTO.isDone());
        return task;
    }

    public TaskDTO taskToTaskDTO(Task task){
        if(task == null){
            return null;
        }
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle(task.getTitle());
        taskDTO.setDescription(task.getDescription());
        taskDTO.setDone(task.isDone());
        return taskDTO;
    }
}
