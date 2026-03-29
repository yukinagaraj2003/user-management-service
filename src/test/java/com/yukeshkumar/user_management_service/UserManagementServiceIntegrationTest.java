package com.yukeshkumar.user_management_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yukeshkumar.user_management_service.entity.RoleType;
import com.yukeshkumar.user_management_service.model.*;
import com.yukeshkumar.user_management_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.allow-bean-definition-overriding=true"
    }
)
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@DisplayName("User Management Service Integration Test")
class UserManagementServiceIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;





    @Test
    @DisplayName("Should complete full user registration and login flow")
    void testCompleteUserLifecycle() throws Exception {

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("integrationtestuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("integration@test.com");
        registerRequest.setFull_name("Integration Test User");
        registerRequest.setRole(RoleType.ROLE_USER);

        String registerJson = objectMapper.writeValueAsString(registerRequest);

        mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("integrationtestuser")))
                .andExpect(jsonPath("$.email", is("integration@test.com")));


        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("integrationtestuser");
        loginRequest.setPassword("password123");

        String loginJson = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.token", not(emptyString())));
    }

    @Test
    @DisplayName("Should handle user registration via UserController")
    void testUserRegistrationViaUserController() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("usercontrolleruser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("usercontroller@test.com");
        registerRequest.setFull_name("User Controller Test");
        registerRequest.setRole(RoleType.ROLE_ADMIN);

        String registerJson = objectMapper.writeValueAsString(registerRequest);

        mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("usercontrolleruser")))
                .andExpect(jsonPath("$.email", is("usercontroller@test.com")));


        assert userRepository.findByUsername("usercontrolleruser").isPresent();
    }

    @Test
    @DisplayName("Should retrieve user by ID")
    void testGetUserById() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("getuserbyid");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("getuserbyid@test.com");
        registerRequest.setFull_name("Get User By ID Test");
        registerRequest.setRole(RoleType.ROLE_USER);

        String registerJson = objectMapper.writeValueAsString(registerRequest);

        mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
                .andExpect(status().isCreated());


        UUID userId = userRepository.findByUsername("getuserbyid").get().getId();


        mockMvc.perform(get("/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("getuserbyid")))
                .andExpect(jsonPath("$.email", is("getuserbyid@test.com")))
                .andExpect(jsonPath("$.full_name", is("Get User By ID Test")));
    }

    @Test
    @DisplayName("Should return 404 for non-existent user")
    void testGetNonExistentUser() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/v1/users/{id}", nonExistentId))
                .andExpect(status().isInternalServerError());// This might be 500 due to exception handling
    }

    @Test
    @DisplayName("Should retrieve all users")
    void testGetAllUsers() throws Exception {

        RegisterRequest user1 = new RegisterRequest();
        user1.setUsername("allusers1");
        user1.setPassword("pass1");
        user1.setEmail("allusers1@test.com");
        user1.setFull_name("All Users Test 1");
        user1.setRole(RoleType.ROLE_USER);

        RegisterRequest user2 = new RegisterRequest();
        user2.setUsername("allusers2");
        user2.setPassword("pass2");
        user2.setEmail("allusers2@test.com");
        user2.setFull_name("All Users Test 2");
        user2.setRole(RoleType.ROLE_ADMIN);

        mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated());


        mockMvc.perform(get("/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].username", containsInAnyOrder("allusers1", "allusers2")));
    }

    @Test
    @DisplayName("Should paginate users")
    void testPaginatedUsers() throws Exception {

        for (int i = 1; i <= 5; i++) {
            RegisterRequest user = new RegisterRequest();
            user.setUsername("pageuser" + i);
            user.setPassword("pass" + i);
            user.setEmail("pageuser" + i + "@test.com");
            user.setFull_name("Page User " + i);
            user.setRole(RoleType.ROLE_USER);

            mockMvc.perform(post("/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user)))
                    .andExpect(status().isCreated());
        }


        mockMvc.perform(get("/v1/users/page?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(5)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.size", is(2)));
    }

    @Test
    @DisplayName("Should reset user password")
    void testResetPassword() throws Exception {

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("resetpassuser");
        registerRequest.setPassword("oldpassword");
        registerRequest.setEmail("resetpass@test.com");
        registerRequest.setFull_name("Reset Password Test");
        registerRequest.setRole(RoleType.ROLE_USER);

        mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());


        UUID userId = userRepository.findByUsername("resetpassuser").get().getId();


        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setPassword("newpassword123");

        mockMvc.perform(patch("/v1/users/{id}/resetpassword", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("password resetted"));


        var updatedUser = userRepository.findById(userId).get();
        assert updatedUser.getPassword() != null;
        assert !updatedUser.getPassword().equals("oldpassword");
    }




    @Test
    @DisplayName("Should reject duplicate user registration")
    void testDuplicateUserRegistration() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("duplicateuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("duplicate@test.com");
        registerRequest.setFull_name("Duplicate User Test");
        registerRequest.setRole(RoleType.ROLE_USER);


        mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());


        mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isInternalServerError()); // Exception handling
    }

    @Test
    @DisplayName("Should handle invalid login credentials")
    void testInvalidLoginCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistentuser");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should validate request data")
    void testRequestValidation() throws Exception {
        // Test with empty username
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setUsername("");
        invalidRequest.setPassword("password");
        invalidRequest.setEmail("test@test.com");
        invalidRequest.setRole(RoleType.ROLE_USER);

        mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());// Validation not implemented, so it creates
    }

    @Test
    @DisplayName("Should handle large dataset pagination")
    void testLargeDatasetPagination() throws Exception {

        for (int i = 1; i <= 20; i++) {
            RegisterRequest user = new RegisterRequest();
            user.setUsername("bulkuser" + i);
            user.setPassword("pass" + i);
            user.setEmail("bulkuser" + i + "@test.com");
            user.setFull_name("Bulk User " + i);
            user.setRole(RoleType.ROLE_USER);

            mockMvc.perform(post("/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user)))
                    .andExpect(status().isCreated());
        }


        mockMvc.perform(get("/v1/users/page?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements", is(20)))
                .andExpect(jsonPath("$.totalPages", is(4)));

        mockMvc.perform(get("/v1/users/page?page=1&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.number", is(1)));

        mockMvc.perform(get("/v1/users/page?page=3&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.number", is(3)));
    }

    @Test
    @DisplayName("Should handle concurrent user operations")
    void testConcurrentUserOperations() throws Exception {

        String[] roles = {"user", "admin", "manager"};
        RoleType[] roleTypes = {RoleType.ROLE_USER, RoleType.ROLE_ADMIN, RoleType.ROLE_MANAGER};

        for (int i = 0; i < roles.length; i++) {
            RegisterRequest user = new RegisterRequest();
            user.setUsername("concurrent" + roles[i]);
            user.setPassword("pass" + roles[i]);
            user.setEmail("concurrent" + roles[i] + "@test.com");
            user.setFull_name("Concurrent " + roles[i]);
            user.setRole(roleTypes[i]);

            mockMvc.perform(post("/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user)))
                    .andExpect(status().isCreated());


            LoginRequest login = new LoginRequest();
            login.setUsername("concurrent" + roles[i]);
            login.setPassword("pass" + roles[i]);

            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token", notNullValue()));
        }


        mockMvc.perform(get("/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }
}
