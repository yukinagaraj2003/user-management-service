package com.yukeshkumar.user_management_service.service;

import com.yukeshkumar.user_management_service.entity.RoleType;
import com.yukeshkumar.user_management_service.entity.UserEntity;
import com.yukeshkumar.user_management_service.exception.UserAlreadyExistsException;
import com.yukeshkumar.user_management_service.mapper.UserMapper;
import com.yukeshkumar.user_management_service.model.*;
import com.yukeshkumar.user_management_service.repository.UserRepository;
import com.yukeshkumar.user_management_service.security.JwtUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.InOrder;

@DisplayName("UserServiceImpl Enhanced Test Suite")
class UserServiceImplTest_Enhanced {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtility jwtUtility;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("CreateUser Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should successfully create a new user")
        void testCreateUser_Success() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setPassword("password123");
            request.setEmail("new@example.com");
            request.setFull_name("New User");
            request.setRole(RoleType.ROLE_USER);

            UserEntity userEntity = new UserEntity();
            UUID userId = UUID.randomUUID();
            userEntity.setId(userId);
            userEntity.setUsername("newuser");
            userEntity.setEmail("new@example.com");
            userEntity.setFull_name("New User");
            userEntity.setRole(RoleType.ROLE_USER);

            UserResponse expectedResponse = new UserResponse();
            expectedResponse.setUsername("newuser");
            expectedResponse.setEmail("new@example.com");

            when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
            when(userMapper.convertDtoToEntity(request)).thenReturn(userEntity);
            when(userRepository.save(userEntity)).thenReturn(userEntity);
            when(userMapper.convertEntityToUserResponse(userEntity)).thenReturn(expectedResponse);

            // Act
            UserResponse result = userServiceImpl.createUser(request);

            // Assert
            assertNotNull(result);
            assertEquals("newuser", result.getUsername());
            assertEquals("new@example.com", result.getEmail());
            verify(userRepository).findByUsername("newuser");
            verify(userRepository).save(userEntity);
            verify(userMapper).convertDtoToEntity(request);
            verify(userMapper).convertEntityToUserResponse(userEntity);
        }

        @Test
        @DisplayName("Should throw UserAlreadyExistsException when user exists")
        void testCreateUser_UserAlreadyExists() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("existinguser");
            request.setPassword("password");
            request.setEmail("existing@example.com");

            UserEntity existingUser = new UserEntity();
            existingUser.setUsername("existinguser");

            when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

            // Act & Assert
            assertThrows(UserAlreadyExistsException.class, () -> userServiceImpl.createUser(request));
            verify(userRepository).findByUsername("existinguser");
            verify(userRepository, never()).save(any());
            verify(userMapper, never()).convertDtoToEntity(any());
        }

        @Test
        @DisplayName("Should create user with ADMIN role")
        void testCreateUser_AdminRole() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("admin");
            request.setPassword("adminpass");
            request.setEmail("admin@example.com");
            request.setFull_name("Admin User");
            request.setRole(RoleType.ROLE_ADMIN);

            UserEntity userEntity = new UserEntity();
            UUID adminId = UUID.randomUUID();
            userEntity.setId(adminId);
            userEntity.setUsername("admin");
            userEntity.setRole(RoleType.ROLE_ADMIN);

            UserResponse expectedResponse = new UserResponse();
            expectedResponse.setUsername("admin");

            when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
            when(userMapper.convertDtoToEntity(request)).thenReturn(userEntity);
            when(userRepository.save(userEntity)).thenReturn(userEntity);
            when(userMapper.convertEntityToUserResponse(userEntity)).thenReturn(expectedResponse);

            // Act
            UserResponse result = userServiceImpl.createUser(request);

            // Assert
            assertNotNull(result);
            assertEquals("admin", result.getUsername());
            verify(userRepository).findByUsername("admin");
            verify(userRepository).save(userEntity);
        }

        @Test
        @DisplayName("Should create user with MANAGER role")
        void testCreateUser_ManagerRole() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("manager");
            request.setPassword("managerpass");
            request.setEmail("manager@example.com");
            request.setFull_name("Manager User");
            request.setRole(RoleType.ROLE_MANAGER);

            UserEntity userEntity = new UserEntity();
            UUID managerId = UUID.randomUUID();
            userEntity.setId(managerId);
            userEntity.setUsername("manager");
            userEntity.setRole(RoleType.ROLE_MANAGER);

            UserResponse expectedResponse = new UserResponse();
            expectedResponse.setUsername("manager");

            when(userRepository.findByUsername("manager")).thenReturn(Optional.empty());
            when(userMapper.convertDtoToEntity(request)).thenReturn(userEntity);
            when(userRepository.save(userEntity)).thenReturn(userEntity);
            when(userMapper.convertEntityToUserResponse(userEntity)).thenReturn(expectedResponse);

            // Act
            UserResponse result = userServiceImpl.createUser(request);

            // Assert
            assertNotNull(result);
            assertEquals("manager", result.getUsername());
            verify(userRepository).save(userEntity);
        }

        @Test
        @DisplayName("Should call mapper methods in correct order")
        void testCreateUser_MapperCallOrder() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setPassword("password");
            request.setEmail("test@example.com");

            UserEntity userEntity = new UserEntity();
            userEntity.setId(UUID.randomUUID());
            userEntity.setUsername("testuser");

            UserResponse response = new UserResponse();
            response.setUsername("testuser");

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
            when(userMapper.convertDtoToEntity(request)).thenReturn(userEntity);
            when(userRepository.save(userEntity)).thenReturn(userEntity);
            when(userMapper.convertEntityToUserResponse(userEntity)).thenReturn(response);

            // Act
            userServiceImpl.createUser(request);

            // Assert - verify call order
            InOrder inOrder = inOrder(userMapper, userRepository);
            inOrder.verify(userMapper).convertDtoToEntity(request);
            inOrder.verify(userRepository).save(userEntity);
            inOrder.verify(userMapper).convertEntityToUserResponse(userEntity);
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should successfully authenticate and return JWT token")
        void testLogin_Success() {
            // Arrange
            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword("password");

            UUID userId = UUID.randomUUID();
            Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
            CustomUserDetails userDetails = new CustomUserDetails(userId, "testuser", "password", authorities);
            
            Authentication authentication = mock(Authentication.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(jwtUtility.generateToken(userDetails)).thenReturn("jwt-token-xyz");

            // Act
            String token = userServiceImpl.login(request);

            // Assert
            assertNotNull(token);
            assertEquals("jwt-token-xyz", token);
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(authentication).getPrincipal();
            verify(jwtUtility).generateToken(userDetails);
        }

        @Test
        @DisplayName("Should throw BadCredentialsException for invalid credentials")
        void testLogin_InvalidCredentials() {
            // Arrange
            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword("wrongpassword");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Invalid username or password"));

            // Act & Assert
            assertThrows(BadCredentialsException.class, () -> userServiceImpl.login(request));
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtUtility, never()).generateToken(any());
        }

        @Test
        @DisplayName("Should generate token with ADMIN role")
        void testLogin_AdminRole() {
            // Arrange
            LoginRequest request = new LoginRequest();
            request.setUsername("admin");
            request.setPassword("adminpass");

            UUID adminId = UUID.randomUUID();
            Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
            CustomUserDetails adminDetails = new CustomUserDetails(adminId, "admin", "adminpass", authorities);
            
            Authentication authentication = mock(Authentication.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(adminDetails);
            when(jwtUtility.generateToken(adminDetails)).thenReturn("admin-jwt-token");

            // Act
            String token = userServiceImpl.login(request);

            // Assert
            assertEquals("admin-jwt-token", token);
            verify(jwtUtility).generateToken(adminDetails);
        }

        @Test
        @DisplayName("Should pass correct authentication token to manager")
        void testLogin_AuthenticationTokenCreation() {
            // Arrange
            LoginRequest request = new LoginRequest();
            request.setUsername("user");
            request.setPassword("pass123");

            CustomUserDetails userDetails = new CustomUserDetails(UUID.randomUUID(), "user", "pass123", Collections.emptyList());
            Authentication authentication = mock(Authentication.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(jwtUtility.generateToken(userDetails)).thenReturn("token");

            // Act
            userServiceImpl.login(request);

            // Assert - verify authentication manager receives correct credentials
            verify(authenticationManager).authenticate(argThat(auth -> 
                auth instanceof UsernamePasswordAuthenticationToken &&
                auth.getPrincipal().equals("user") &&
                auth.getCredentials().equals("pass123")
            ));
        }

        @Test
        @DisplayName("Should return token with correct format")
        void testLogin_TokenFormat() {
            // Arrange
            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword("password");

            CustomUserDetails userDetails = new CustomUserDetails(UUID.randomUUID(), "testuser", "password", Collections.emptyList());
            Authentication authentication = mock(Authentication.class);

            String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciJ9.signature";

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(jwtUtility.generateToken(userDetails)).thenReturn(expectedToken);

            // Act
            String token = userServiceImpl.login(request);

            // Assert
            assertNotNull(token);
            assertTrue(token.contains("."));
            assertEquals(expectedToken, token);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should verify createUser and login flow")
        void testCreateUserAndLoginFlow() {
            // Arrange - Create User
            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setUsername("newuser");
            registerRequest.setPassword("password123");
            registerRequest.setEmail("user@example.com");
            registerRequest.setFull_name("Test User");
            registerRequest.setRole(RoleType.ROLE_USER);

            UserEntity userEntity = new UserEntity();
            UUID userId = UUID.randomUUID();
            userEntity.setId(userId);
            userEntity.setUsername("newuser");

            UserResponse createResponse = new UserResponse();
            createResponse.setUsername("newuser");

            when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
            when(userMapper.convertDtoToEntity(registerRequest)).thenReturn(userEntity);
            when(userRepository.save(userEntity)).thenReturn(userEntity);
            when(userMapper.convertEntityToUserResponse(userEntity)).thenReturn(createResponse);

            // Act - Create User
            UserResponse result = userServiceImpl.createUser(registerRequest);

            // Assert
            assertNotNull(result);
            assertEquals("newuser", result.getUsername());

            // Arrange - Login
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername("newuser");
            loginRequest.setPassword("password123");

            CustomUserDetails userDetails = new CustomUserDetails(userId, "newuser", "password123", 
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
            Authentication authentication = mock(Authentication.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(jwtUtility.generateToken(userDetails)).thenReturn("jwt-token");

            // Act - Login
            String token = userServiceImpl.login(loginRequest);

            // Assert
            assertNotNull(token);
            assertEquals("jwt-token", token);
        }

        @Test
        @DisplayName("Should not allow login for non-existent user after failed creation")
        void testFailedCreateUserFollowedByFailedLogin() {
            // Arrange - Try to create user that already exists
            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setUsername("existinguser");
            registerRequest.setPassword("password");
            registerRequest.setEmail("existing@example.com");

            UserEntity existingUser = new UserEntity();
            existingUser.setUsername("existinguser");

            when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

            // Act & Assert
            assertThrows(UserAlreadyExistsException.class, () -> userServiceImpl.createUser(registerRequest));

            // Arrange - Try to login
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername("existinguser");
            loginRequest.setPassword("password");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            // Act & Assert
            assertThrows(BadCredentialsException.class, () -> userServiceImpl.login(loginRequest));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Exception Handling")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle principal as non-CustomUserDetails type")
        void testLogin_WrongPrincipalType() {
            // Arrange
            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword("password");

            Authentication authentication = mock(Authentication.class);
            when(authentication.getPrincipal()).thenReturn("not-a-userdetails-object");
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

            // Act & Assert
            assertThrows(ClassCastException.class, () -> userServiceImpl.login(request));
        }
    }
}




