package com.deploy.devops.tasks;

import com.deploy.devops.tasks.data.TaskRepository;
import com.deploy.devops.tasks.data.interfaces.ITaskDataSource;
import com.deploy.devops.tasks.delivery.*;
import com.deploy.devops.tasks.domain.Task;
import com.deploy.devops.tasks.usecase.*;
import com.deploy.devops.users.domain.User;
import com.deploy.devops.users.usecase.interfaces.IUserRepository;
import com.deploy.devops.users.delivery.UserExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaskCrudTests {
    private ITaskDataSource dataSource;
    private IUserRepository userRepository;
    private MockMvc mvc;
    private Task task;

    @BeforeEach
    void setUp() {
        dataSource = mock(ITaskDataSource.class);
        userRepository = mock(IUserRepository.class);
        TaskRepository repository = new TaskRepository(dataSource);
        TaskService service = new TaskService(new CreateTask(repository), new UpdateTask(repository),
                new DeleteTask(repository), new GetTask(repository), new GetAllTasks(repository),
                new AssignTask(repository, userRepository));
        mvc = MockMvcBuilders.standaloneSetup(new TaskController(service))
                .setControllerAdvice(new TaskExceptionHandler(), new UserExceptionHandler()).build();
        task = new Task();
        task.setId("507f1f77bcf86cd799439011");
        task.setName("Configurar CI");
        task.setCreated(new Date(1000));
        task.setUpdated(new Date(1000));
    }

    @Test
    void createGeneratesMetadataAndReturnsLocation() throws Exception {
        when(dataSource.create(any())).thenAnswer(invocation -> {
            Task created = invocation.getArgument(0);
            assertNull(created.getId());
            assertNotNull(created.getCreated());
            assertEquals(created.getCreated(), created.getUpdated());
            created.setId(task.getId());
            return created;
        });
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Configurar CI\",\"status\":\"pending\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/tasks/" + task.getId()))
                .andExpect(jsonPath("$.name").value("Configurar CI"))
                .andExpect(jsonPath("$.status").value("pending"))
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    void getReturnsTask() throws Exception {
        when(dataSource.get(task.getId())).thenReturn(Optional.of(task));
        mvc.perform(get("/api/tasks/{id}", task.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()));
    }

    @Test
    void listReturnsTasks() throws Exception {
        when(dataSource.getAll()).thenReturn(List.of(task));
        mvc.perform(get("/api/tasks")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(task.getId()));
    }

    @Test
    void listReturnsEmptyArray() throws Exception {
        when(dataSource.getAll()).thenReturn(List.of());
        mvc.perform(get("/api/tasks")).andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void updateSetsTimestampAndReturnsUpdatedTask() throws Exception {
        when(dataSource.update(eq(task.getId()), any())).thenAnswer(invocation -> {
            Task updated = invocation.getArgument(1);
            assertNotNull(updated.getUpdated());
            assertTrue(updated.getUpdated().after(task.getUpdated()));
            updated.setId(task.getId());
            updated.setCreated(task.getCreated());
            return Optional.of(updated);
        });
        mvc.perform(put("/api/tasks/{id}", task.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Configurar Docker\",\"status\":\"done\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Configurar Docker"))
                .andExpect(jsonPath("$.status").value("Test para validar pipeline (rompemo' todo)"));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        when(dataSource.delete(task.getId())).thenReturn(true);
        mvc.perform(delete("/api/tasks/{id}", task.getId()))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
    }

    @Test
    void missingTaskReturns404ForGetUpdateAndDelete() throws Exception {
        when(dataSource.get("missing")).thenReturn(Optional.empty());
        when(dataSource.update(eq("missing"), any())).thenReturn(Optional.empty());
        mvc.perform(get("/api/tasks/missing")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Task not found: missing"));
        mvc.perform(put("/api/tasks/missing").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Task\"}")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/tasks/missing")).andExpect(status().isNotFound());
    }

    @Test
    void blankAndMissingNamesAreRejectedBeforePersistence() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"  \"}")).andExpect(status().isBadRequest());
        mvc.perform(put("/api/tasks/id").contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isBadRequest());
        verifyNoInteractions(dataSource);
    }

    @Test
    void malformedJsonIsRejected() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                .content("{invalid")).andExpect(status().isBadRequest());
        verifyNoInteractions(dataSource);
    }

    private void prepareAssignment() {
        when(dataSource.get(task.getId())).thenReturn(Optional.of(task));
        User user = new User();
        user.setId("user-id");
        user.setUsername("username-not-id");
        when(userRepository.get("user-id")).thenReturn(Optional.of(user));
    }

    @Test
    void assignmentStoresRegisteredUserIdAndReturnsTask() throws Exception {
        prepareAssignment();
        when(dataSource.assignIfUnassigned(eq(task.getId()), eq("user-id"), any(Date.class)))
                .thenAnswer(invocation -> {
                    assertNull(task.getAssignee());
                    task.setAssignee(invocation.getArgument(1));
                    task.setUpdated(invocation.getArgument(2));
                    return Optional.of(task);
                });
        mvc.perform(patch("/api/tasks/{id}/assignee", task.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"user-id\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignee").value("user-id"))
                .andExpect(jsonPath("$.name").value("Configurar CI"));
        assertTrue(task.getUpdated().after(task.getCreated()));
        verify(dataSource, never()).update(anyString(), any());
    }

    @Test
    void assignmentToUnregisteredUserReturns404WithoutWriting() throws Exception {
        when(dataSource.get(task.getId())).thenReturn(Optional.of(task));
        when(userRepository.get("missing")).thenReturn(Optional.empty());
        mvc.perform(patch("/api/tasks/{id}/assignee", task.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"missing\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User not found: missing"));
        verify(dataSource, never()).assignIfUnassigned(anyString(), anyString(), any());
        assertNull(task.getAssignee());
    }

    @Test
    void assignmentToMissingTaskReturns404() throws Exception {
        when(dataSource.get("missing")).thenReturn(Optional.empty());
        mvc.perform(patch("/api/tasks/missing/assignee").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"user-id\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Task not found: missing"));
        verifyNoInteractions(userRepository);
        verify(dataSource, never()).assignIfUnassigned(anyString(), anyString(), any());
    }

    @Test
    void assignmentToAlreadyAssignedTaskReturns409() throws Exception {
        task.setAssignee("another-user-id");
        when(dataSource.get(task.getId())).thenReturn(Optional.of(task));
        mvc.perform(patch("/api/tasks/{id}/assignee", task.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"user-id\"}"))
                .andExpect(status().isConflict());
        verify(dataSource, never()).assignIfUnassigned(anyString(), anyString(), any());
        assertEquals("another-user-id", task.getAssignee());
    }

    @Test
    void losingConcurrentAssignmentReturns409() throws Exception {
        prepareAssignment();
        Task assigned = new Task();
        assigned.setName(task.getName());
        assigned.setAssignee("winner-id");
        when(dataSource.get(task.getId())).thenReturn(Optional.of(task)).thenReturn(Optional.of(assigned));
        when(dataSource.assignIfUnassigned(eq(task.getId()), eq("user-id"), any()))
                .thenReturn(Optional.empty());
        mvc.perform(patch("/api/tasks/{id}/assignee", task.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"user-id\"}"))
                .andExpect(status().isConflict());
        verify(dataSource, times(2)).get(task.getId());
    }

    @Test
    void deletionDuringAssignmentReturns404() throws Exception {
        prepareAssignment();
        when(dataSource.get(task.getId())).thenReturn(Optional.of(task)).thenReturn(Optional.empty());
        when(dataSource.assignIfUnassigned(eq(task.getId()), eq("user-id"), any()))
                .thenReturn(Optional.empty());
        mvc.perform(patch("/api/tasks/{id}/assignee", task.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"user-id\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingOrBlankAssignmentUserIdIsRejected() throws Exception {
        for (String body : List.of("{}", "{\"userId\":\"  \"}")) {
            mvc.perform(patch("/api/tasks/{id}/assignee", task.getId()).contentType(MediaType.APPLICATION_JSON)
                    .content(body)).andExpect(status().isBadRequest());
        }
        verifyNoInteractions(dataSource, userRepository);
    }

    @Test
    void creationClearsAnyCallerSuppliedAssignee() {
        task.setAssignee("unregistered-user");
        when(dataSource.create(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Task created = new CreateTask(new TaskRepository(dataSource)).execute(task);
        assertNull(created.getAssignee());
    }
}
