package service;

import com.example.todo.model.dto.TaskDTO;
import com.example.todo.model.entity.Role;
import com.example.todo.model.entity.Task;
import com.example.todo.model.entity.User;
import com.example.todo.model.mapper.TaskMapper;
import com.example.todo.repository.TaskRepository;
import com.example.todo.service.TaskService;
import com.example.todo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private User user;
    private User admin;
    private TaskDTO taskDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setRoles(Set.of(Role.ROLE_USER));

        admin = new User();
        admin.setId(2L);
        admin.setUsername("adminuser");
        admin.setRoles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN));


        task = new Task();
        task.setId(1L);
        task.setTitle("task title");
        task.setDescription("task description");
        task.setDone(false);
        task.setOwner(user);

        taskDTO = new TaskDTO();
        taskDTO.setTitle("task title");
        taskDTO.setDescription("task description");
        taskDTO.setDone(false);
    }

    // ========== GET TASKS TESTS ==========

    @Test
    void getTasksForUser_WithValidUsername_ShouldReturnTaskList() {
        String username = "username";
        List<Task> tasks = Arrays.asList(task);
        when(taskRepository.findByOwnerUsername(username)).thenReturn(tasks);
        when(taskMapper.taskToTaskDTO(task)).thenReturn(taskDTO);

        List<TaskDTO> result = taskService.getTasksForUser(username);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("task title", result.get(0).getTitle());
        verify(taskRepository, times(1)).findByOwnerUsername(username);
        verify(taskMapper, times(1)).taskToTaskDTO(task);
    }

    @Test
    void getTasksForUser_ShouldReturnTaskDTOList_WhenTasksExist() {
        when(taskRepository.findByOwnerUsername(user.getUsername())).thenReturn(List.of(task));
        when(taskMapper.taskToTaskDTO(task)).thenReturn(taskDTO);

        List<TaskDTO> result = taskService.getTasksForUser(user.getUsername());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(taskDTO, result.get(0));

        verify(taskRepository).findByOwnerUsername(user.getUsername());
        verify(taskMapper).taskToTaskDTO(task);
    }

    // ====== CREATE TESTS ========

    @Test
    void create_WhenAdminCreatesDoneTask_ShouldKeepDoneTrue() {
        String username = "adminuser";
        TaskDTO createDTO = new TaskDTO();
        createDTO.setTitle("New Task");
        createDTO.setDescription("New Description");
        createDTO.setDone(true);

        Task taskEntity = new Task();
        taskEntity.setTitle("New Task");
        taskEntity.setDescription("New Description");
        taskEntity.setDone(false);

        Task savedTask = new Task();
        savedTask.setId(1L);
        savedTask.setTitle("New Task");
        savedTask.setDescription("New Description");
        savedTask.setDone(false);
        savedTask.setOwner(admin);

        TaskDTO savedTaskDTO = new TaskDTO();
        savedTaskDTO.setTitle("New Task");
        savedTaskDTO.setDescription("New Description");
        savedTaskDTO.setDone(false);

        when(userService.findByUsername(username)).thenReturn(Optional.of(admin));
        when(taskMapper.taskDTOToTask(createDTO)).thenReturn(taskEntity);
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
        when(taskMapper.taskToTaskDTO(savedTask)).thenReturn(savedTaskDTO);

        TaskDTO result = taskService.create(createDTO, username);

        assertNotNull(result);
        assertFalse(result.isDone());
        verify(taskRepository, times(1)).save(taskEntity);
        assertFalse(taskEntity.isDone());
    }

    @Test
    void create_WhenUserNotFound_ShouldThrowException() {
        String username = "nonexistent";
        TaskDTO createDTO = new TaskDTO();
        createDTO.setTitle("New Task");
        createDTO.setDescription("New Description");
        createDTO.setDone(false);

        when(userService.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            taskService.create(createDTO, username);
        });

        verify(userService, times(1)).findByUsername(username);
        verify(taskRepository, never()).save(any());
    }


    // ========== update TESTS ==========

    @Test
    void update_ShouldUpdateTask_WhenUserIsOwnerAndNotAdmin() {
        taskDTO.setTitle("Updated Title");
        taskDTO.setDone(false);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userService.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.taskToTaskDTO(task)).thenReturn(taskDTO);

        ResponseEntity<TaskDTO> response = taskService.update(task.getId(), taskDTO, user.getUsername());

        assertNotNull(response);
        assertEquals("Updated Title", task.getTitle());
        assertFalse(task.isDone());
        verify(taskRepository).save(task);
    }

    @Test
    void update_ShouldUpdateTitleAndDescription_WhenUserIsOwner() {
        taskDTO.setTitle("New Title");
        taskDTO.setDescription("New Description");

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userService.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.taskToTaskDTO(task)).thenReturn(taskDTO);

        ResponseEntity<TaskDTO> response = taskService.update(task.getId(), taskDTO, user.getUsername());

        assertEquals("New Title", task.getTitle());
        assertEquals("New Description", task.getDescription());
    }

    @Test
    void update_ShouldAllowAdminToUpdateTitleAndSetDoneTrue() {
        taskDTO.setTitle("Admin Update");
        taskDTO.setDone(true);
        task.setOwner(admin);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userService.findByUsername(admin.getUsername())).thenReturn(Optional.of(admin));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.taskToTaskDTO(task)).thenReturn(taskDTO);

        ResponseEntity<TaskDTO> response = taskService.update(task.getId(), taskDTO, admin.getUsername());

        assertEquals("Admin Update", task.getTitle());
        assertTrue(task.isDone());
        assertNotNull(response);
    }

    // ========== delete TESTS ==========

    @Test
    void delete_WhenUserNotOwner_ShouldReturnForbidden() {
        Long taskId = 1L;
        String differentUsername = "differentuser";
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        ResponseEntity<?> response = taskService.delete(taskId, differentUsername);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, never()).delete(any());
    }

    @Test
    void delete_WhenTaskNotFound_ShouldThrowException() {
        Long taskId = 99L;
        String username = "regularuser";
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            taskService.delete(taskId, username);
        });

        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, never()).delete(any());
    }

    // ========== hasAdminRole TESTS ==========

    @Test
    void hasAdminRole_WhenUserHasAdminRole_ShouldReturnTrue() {
        assertTrue(taskService.hasAdminRole(admin));
    }

    @Test
    void hasAdminRole_WhenUserDoesNotHaveAdminRole_ShouldReturnFalse() {
        assertFalse(taskService.hasAdminRole(user));
    }

    @Test
    void hasAdminRole_WhenUserHasNoRoles_ShouldReturnFalse() {
        User userWithNoRoles = new User();
        userWithNoRoles.setRoles(Collections.emptySet());

        assertFalse(taskService.hasAdminRole(userWithNoRoles));
    }

    @Test
    void hasAdminRole_WhenUserHasMultipleRolesIncludingAdmin_ShouldReturnTrue() {
        User userWithMultipleRoles = new User();
        userWithMultipleRoles.setRoles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN));

        assertTrue(taskService.hasAdminRole(userWithMultipleRoles));
    }
}
