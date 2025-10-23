package controller;

import com.example.todo.controller.TaskController;
import com.example.todo.model.dto.TaskDTO;
import com.example.todo.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @Mock
    private Principal principal;

    @InjectMocks
    private TaskController taskController;

    private TaskDTO taskDTO;
    private String username;

    @BeforeEach
    void setUp() {
        username = "testuser";

        taskDTO = new TaskDTO();
        taskDTO.setTitle("Test Task");
        taskDTO.setDescription("Test Description");
        taskDTO.setDone(false);

        when(principal.getName()).thenReturn(username);
    }

    // ========== listTasks TESTS ==========

    @Test
    void listTasks_WithValidPrincipal_ShouldReturnTaskList() {
        List<TaskDTO> taskList = Arrays.asList(taskDTO);
        when(taskService.getTasksForUser(username)).thenReturn(taskList);

        List<TaskDTO> result = taskController.listTasks(principal);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Task", result.get(0).getTitle());
        verify(taskService, times(1)).getTasksForUser(username);
        verify(principal, times(1)).getName();
    }

    @Test
    void listTasks_WithEmptyTaskList_ShouldReturnEmptyList() {
        List<TaskDTO> emptyList = List.of();
        when(taskService.getTasksForUser(username)).thenReturn(emptyList);

        List<TaskDTO> result = taskController.listTasks(principal);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(taskService, times(1)).getTasksForUser(username);
    }

    // ========== create TESTS ==========

    @Test
    void create_WithValidTask_ShouldReturnCreatedResponse() {
        when(taskService.create(taskDTO, username)).thenReturn(taskDTO);

        ResponseEntity<TaskDTO> response = taskController.create(taskDTO, principal);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Task", response.getBody().getTitle());
        verify(taskService, times(1)).create(taskDTO, username);
        verify(principal, times(1)).getName();
    }

    @Test
    void create_WithNullTask_ShouldPassNullToService() {
        when(taskService.create(null, username)).thenReturn(null);

        ResponseEntity<TaskDTO> response = taskController.create(null, principal);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(taskService, times(1)).create(null, username);
    }

    // ========== update TESTS ==========

    @Test
    void update_WithValidData_ShouldReturnOkResponse() {
        Long taskId = 1L;
        ResponseEntity<TaskDTO> serviceResponse = ResponseEntity.ok(taskDTO);
        when(taskService.update(taskId, taskDTO, username)).thenReturn(serviceResponse);

        ResponseEntity<TaskDTO> response = taskController.update(taskId, taskDTO, principal);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Task", response.getBody().getTitle());
        verify(taskService, times(1)).update(taskId, taskDTO, username);
        verify(principal, times(1)).getName();
    }

    @Test
    void update_WithDifferentTaskIdInPath_ShouldUsePathId() {
        Long pathTaskId = 99L;
        ResponseEntity<TaskDTO> serviceResponse = ResponseEntity.ok(taskDTO);
        when(taskService.update(pathTaskId, taskDTO, username)).thenReturn(serviceResponse);

        ResponseEntity<TaskDTO> response = taskController.update(pathTaskId, taskDTO, principal);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(taskService, times(1)).update(pathTaskId, taskDTO, username);
    }

    @Test
    void update_WithNullTaskBody_ShouldPassNullToService() {
        Long taskId = 1L;
        ResponseEntity<TaskDTO> serviceResponse = ResponseEntity.ok(null);
        when(taskService.update(taskId, null, username)).thenReturn(serviceResponse);

        ResponseEntity<TaskDTO> response = taskController.update(taskId, null, principal);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(taskService, times(1)).update(taskId, null, username);
    }

    // ========== delete TESTS ==========

    @Test
    void delete_WhenServiceReturnsNotFound_ShouldReturnNotFound() {
        Long taskId = 99L;
        when(taskService.delete(taskId, username)).thenReturn(null);

        ResponseEntity<Void> response = taskController.delete(taskId, principal);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(taskService, times(1)).delete(taskId, username); // Called only once due to null check
    }

    // ========== Other TESTS ==========
    @Test
    void create_WithEmptyUsername_ShouldPassEmptyUsernameToService() {
        String emptyUsername = "";
        when(principal.getName()).thenReturn(emptyUsername);
        when(taskService.create(taskDTO, emptyUsername)).thenReturn(taskDTO);

        ResponseEntity<TaskDTO> response = taskController.create(taskDTO, principal);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(taskService, times(1)).create(taskDTO, emptyUsername);
    }


}
