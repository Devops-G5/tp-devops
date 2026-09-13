package com.deploy.devops.users;

import com.deploy.devops.users.delivery.dto.UserRequest;

import com.deploy.devops.users.data.UserRepository;
import com.deploy.devops.users.data.interfaces.IUserDataSource;
import com.deploy.devops.users.delivery.*;
import com.deploy.devops.users.domain.User;
import com.deploy.devops.users.usecase.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserCrudTests {
    private static final String BODY = """
            {"name":"Lucas","lastname":"Bonaffini","username":"lucas",
             "email":"lucas@example.com","rol":"developer"}
            """;
    private IUserDataSource dataSource;
    private MockMvc mvc;
    private User user;

    @BeforeEach
    void setUp() {
        dataSource = mock(IUserDataSource.class);
        UserRepository repository = new UserRepository(dataSource);
        UserService service = new UserService(new CreateUser(repository), new UpdateUser(repository),
                new DeleteUser(repository), new GetUser(repository), new GetAllUsers(repository));
        mvc = MockMvcBuilders.standaloneSetup(new UserController(service))
                .setControllerAdvice(new UserExceptionHandler()).build();
        user = new UserRequest("Lucas", "Bonaffini", "lucas", "lucas@example.com", "developer").toDomain();
        user.setId("507f1f77bcf86cd799439011");
    }

    @Test
    void createReturnsGeneratedIdAndLocation() throws Exception {
        when(dataSource.create(any())).thenAnswer(invocation -> {
            User created = invocation.getArgument(0);
            assertNull(created.getId());
            created.setId(user.getId());
            return created;
        });
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/users/" + user.getId()))
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.name").value("Lucas"))
                .andExpect(jsonPath("$.lastname").value("Bonaffini"))
                .andExpect(jsonPath("$.username").value("lucas"))
                .andExpect(jsonPath("$.email").value("lucas@example.com"))
                .andExpect(jsonPath("$.rol").value("developer"));
    }

    @Test
    void getReturnsUser() throws Exception {
        when(dataSource.get(user.getId())).thenReturn(Optional.of(user));
        mvc.perform(get("/api/users/{id}", user.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("lucas"));
    }

    @Test
    void listReturnsUsersAndEmptyArray() throws Exception {
        when(dataSource.getAll()).thenReturn(List.of(user), List.of());
        mvc.perform(get("/api/users")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(user.getId()));
        mvc.perform(get("/api/users")).andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void updateReturnsUpdatedUserAndAcceptsOtherRoles() throws Exception {
        when(dataSource.update(eq(user.getId()), any())).thenAnswer(invocation -> {
            User updated = invocation.getArgument(1);
            updated.setId(user.getId());
            return Optional.of(updated);
        });
        mvc.perform(put("/api/users/{id}", user.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(BODY.replace("developer", "designer")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.rol").value("designer"));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        when(dataSource.delete(user.getId())).thenReturn(true);
        mvc.perform(delete("/api/users/{id}", user.getId()))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
    }

    @Test
    void missingUserReturns404ForGetUpdateAndDelete() throws Exception {
        when(dataSource.get("missing")).thenReturn(Optional.empty());
        when(dataSource.update(eq("missing"), any())).thenReturn(Optional.empty());
        mvc.perform(get("/api/users/missing")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User not found: missing"));
        mvc.perform(put("/api/users/missing").contentType(MediaType.APPLICATION_JSON)
                .content(BODY)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/users/missing")).andExpect(status().isNotFound());
    }

    @Test
    void allFieldsMustBeNonBlankOnCreateAndUpdate() throws Exception {
        for (String value : List.of("Lucas", "Bonaffini", "lucas", "lucas@example.com", "developer")) {
            String invalid = BODY.replace("\"" + value + "\"", "\"  \"");
            mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                    .content(invalid)).andExpect(status().isBadRequest());
            mvc.perform(put("/api/users/id").contentType(MediaType.APPLICATION_JSON)
                    .content(invalid)).andExpect(status().isBadRequest());
        }
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isBadRequest());
        verifyNoInteractions(dataSource);
    }

    @Test
    void invalidEmailAndMalformedJsonAreRejected() throws Exception {
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                .content(BODY.replace("lucas@example.com", "invalid"))).andExpect(status().isBadRequest());
        mvc.perform(put("/api/users/id").contentType(MediaType.APPLICATION_JSON)
                .content(BODY.replace("lucas@example.com", "invalid"))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                .content("{invalid")).andExpect(status().isBadRequest());
        verifyNoInteractions(dataSource);
    }
}
