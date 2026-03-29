package com.yukeshkumar.user_management_service.service;

import com.yukeshkumar.user_management_service.entity.RoleType;
import com.yukeshkumar.user_management_service.entity.UserEntity;
import com.yukeshkumar.user_management_service.mapper.UserMapper;
import com.yukeshkumar.user_management_service.model.ResetPasswordRequest;
import com.yukeshkumar.user_management_service.model.UpdateRequest;
import com.yukeshkumar.user_management_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("UserServiceClass Enhanced Test Suite")
class UserServiceClassTest_Enhanced {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceClass userServiceClass;

    private UUID userId;
    private UserEntity testUserEntity;
    private UpdateRequest testUpdateRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        
        testUserEntity = new UserEntity();
        testUserEntity.setId(userId);
        testUserEntity.setUsername("testuser");
        testUserEntity.setEmail("test@example.com");
        testUserEntity.setFull_name("Test User");
        testUserEntity.setRole(RoleType.ROLE_USER);
        testUserEntity.setPassword("encodedpassword");

        testUpdateRequest = new UpdateRequest();
        testUpdateRequest.setId(userId);
        testUpdateRequest.setUsername("testuser");
        testUpdateRequest.setEmail("test@example.com");
    }

    @Nested
    @DisplayName("GetUserById Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should successfully retrieve user by ID")
        void testGetUserById_Success() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUserEntity));
            when(userMapper.convertEntityToDto(testUserEntity)).thenReturn(testUpdateRequest);

            // Act
            UpdateRequest result = userServiceClass.getUserById(userId);

            // Assert
            assertNotNull(result);
            assertEquals(userId, result.getId());
            assertEquals("testuser", result.getUsername());
            assertEquals("test@example.com", result.getEmail());
            verify(userRepository).findById(userId);
            verify(userMapper).convertEntityToDto(testUserEntity);
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when user not found")
        void testGetUserById_UserNotFound() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, 
                    () -> userServiceClass.getUserById(nonExistentId));
            assertEquals("user not exist", exception.getMessage());
            verify(userRepository).findById(nonExistentId);
            verify(userMapper, never()).convertEntityToDto(any());
        }

        @Test
        @DisplayName("Should handle ADMIN role user retrieval")
        void testGetUserById_AdminUser() {
            // Arrange
            UUID adminId = UUID.randomUUID();
            UserEntity adminEntity = new UserEntity();
            adminEntity.setId(adminId);
            adminEntity.setUsername("admin");
            adminEntity.setRole(RoleType.ROLE_ADMIN);

            UpdateRequest adminRequest = new UpdateRequest();
            adminRequest.setId(adminId);
            adminRequest.setUsername("admin");

            when(userRepository.findById(adminId)).thenReturn(Optional.of(adminEntity));
            when(userMapper.convertEntityToDto(adminEntity)).thenReturn(adminRequest);

            // Act
            UpdateRequest result = userServiceClass.getUserById(adminId);

            // Assert
            assertNotNull(result);
            assertEquals("admin", result.getUsername());
            verify(userRepository).findById(adminId);
        }

        @Test
        @DisplayName("Should retrieve user with all details")
        void testGetUserById_AllDetails() {
            // Arrange
            testUserEntity.setFull_name("John Doe");
            testUpdateRequest.setFull_name("John Doe");

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUserEntity));
            when(userMapper.convertEntityToDto(testUserEntity)).thenReturn(testUpdateRequest);

            // Act
            UpdateRequest result = userServiceClass.getUserById(userId);

            // Assert
            assertNotNull(result);
            assertEquals("testuser", result.getUsername());
            assertEquals("test@example.com", result.getEmail());
            assertEquals("John Doe", result.getFull_name());
            verify(userRepository).findById(userId);
        }
    }

    @Nested
    @DisplayName("GetAllUser Tests")
    class GetAllUserTests {

        @Test
        @DisplayName("Should retrieve all users successfully")
        void testGetAllUser_Success() {
            // Arrange
            UUID userId2 = UUID.randomUUID();
            UserEntity user1 = new UserEntity();
            user1.setId(userId);
            user1.setUsername("user1");

            UserEntity user2 = new UserEntity();
            user2.setId(userId2);
            user2.setUsername("user2");

            List<UserEntity> userEntities = Arrays.asList(user1, user2);

            UpdateRequest updateRequest1 = new UpdateRequest();
            updateRequest1.setUsername("user1");

            UpdateRequest updateRequest2 = new UpdateRequest();
            updateRequest2.setUsername("user2");

            when(userRepository.findAll()).thenReturn(userEntities);
            when(userMapper.convertEntityToDto(user1)).thenReturn(updateRequest1);
            when(userMapper.convertEntityToDto(user2)).thenReturn(updateRequest2);

            // Act
            List<UpdateRequest> result = userServiceClass.getAllUser();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("user1", result.get(0).getUsername());
            assertEquals("user2", result.get(1).getUsername());
            verify(userRepository).findAll();
            verify(userMapper, times(2)).convertEntityToDto(any(UserEntity.class));
        }

        @Test
        @DisplayName("Should return empty list when no users exist")
        void testGetAllUser_EmptyList() {
            // Arrange
            when(userRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<UpdateRequest> result = userServiceClass.getAllUser();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            assertEquals(0, result.size());
            verify(userRepository).findAll();
            verify(userMapper, never()).convertEntityToDto(any());
        }

        @Test
        @DisplayName("Should retrieve all users with different roles")
        void testGetAllUser_MultipleRoles() {
            // Arrange
            UserEntity user1 = new UserEntity();
            user1.setId(UUID.randomUUID());
            user1.setUsername("user1");
            user1.setRole(RoleType.ROLE_USER);

            UserEntity user2 = new UserEntity();
            user2.setId(UUID.randomUUID());
            user2.setUsername("admin");
            user2.setRole(RoleType.ROLE_ADMIN);

            UserEntity user3 = new UserEntity();
            user3.setId(UUID.randomUUID());
            user3.setUsername("manager");
            user3.setRole(RoleType.ROLE_MANAGER);

            List<UserEntity> userEntities = Arrays.asList(user1, user2, user3);

            UpdateRequest request1 = new UpdateRequest();
            request1.setUsername("user1");
            UpdateRequest request2 = new UpdateRequest();
            request2.setUsername("admin");
            UpdateRequest request3 = new UpdateRequest();
            request3.setUsername("manager");

            when(userRepository.findAll()).thenReturn(userEntities);
            when(userMapper.convertEntityToDto(user1)).thenReturn(request1);
            when(userMapper.convertEntityToDto(user2)).thenReturn(request2);
            when(userMapper.convertEntityToDto(user3)).thenReturn(request3);

            // Act
            List<UpdateRequest> result = userServiceClass.getAllUser();

            // Assert
            assertEquals(3, result.size());
            verify(userRepository).findAll();
            verify(userMapper, times(3)).convertEntityToDto(any(UserEntity.class));
        }

        @Test
        @DisplayName("Should maintain order of users from repository")
        void testGetAllUser_MaintainOrder() {
            // Arrange
            List<UserEntity> userEntities = new ArrayList<>();
            UpdateRequest[] requests = new UpdateRequest[3];
            
            for (int i = 0; i < 3; i++) {
                UserEntity entity = new UserEntity();
                entity.setId(UUID.randomUUID());
                entity.setUsername("user" + i);
                userEntities.add(entity);

                UpdateRequest request = new UpdateRequest();
                request.setUsername("user" + i);
                requests[i] = request;

                when(userMapper.convertEntityToDto(entity)).thenReturn(request);
            }

            when(userRepository.findAll()).thenReturn(userEntities);

            // Act
            List<UpdateRequest> result = userServiceClass.getAllUser();

            // Assert
            assertEquals(3, result.size());
            for (int i = 0; i < 3; i++) {
                assertEquals("user" + i, result.get(i).getUsername());
            }
        }
    }

    @Nested
    @DisplayName("ResetPassword Tests")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should successfully reset password")
        void testResetPassword_Success() {
            // Arrange
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setPassword("newpassword");

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUserEntity));
            when(passwordEncoder.encode("newpassword")).thenReturn("encodedNewPassword");

            // Act
            String result = userServiceClass.resetPassword(userId, request);

            // Assert
            assertEquals("password resetted", result);
            assertEquals("encodedNewPassword", testUserEntity.getPassword());
            verify(userRepository).findById(userId);
            verify(passwordEncoder).encode("newpassword");
            verify(userRepository).save(testUserEntity);
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when user not found")
        void testResetPassword_UserNotFound() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setPassword("newpassword");

            when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, 
                    () -> userServiceClass.resetPassword(nonExistentId, request));
            assertEquals("user not found", exception.getMessage());
            verify(userRepository).findById(nonExistentId);
            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should encode password correctly")
        void testResetPassword_PasswordEncoding() {
            // Arrange
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setPassword("securePassword123!");

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUserEntity));
            when(passwordEncoder.encode("securePassword123!")).thenReturn("$2a$10$encoded.secure.hash");

            // Act
            userServiceClass.resetPassword(userId, request);

            // Assert
            verify(passwordEncoder).encode("securePassword123!");
            assertEquals("$2a$10$encoded.secure.hash", testUserEntity.getPassword());
            verify(userRepository).save(testUserEntity);
        }

        @Test
        @DisplayName("Should save updated user entity")
        void testResetPassword_SaveUser() {
            // Arrange
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setPassword("newpass");

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUserEntity));
            when(passwordEncoder.encode("newpass")).thenReturn("encoded");

            // Act
            userServiceClass.resetPassword(userId, request);

            // Assert
            ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(captor.capture());
            UserEntity savedEntity = captor.getValue();
            assertEquals("encoded", savedEntity.getPassword());
            assertEquals(userId, savedEntity.getId());
        }

        @Test
        @DisplayName("Should reset password for admin user")
        void testResetPassword_AdminUser() {
            // Arrange
            UUID adminId = UUID.randomUUID();
            UserEntity adminEntity = new UserEntity();
            adminEntity.setId(adminId);
            adminEntity.setUsername("admin");
            adminEntity.setRole(RoleType.ROLE_ADMIN);
            adminEntity.setPassword("oldAdminPass");

            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setPassword("newAdminPass");

            when(userRepository.findById(adminId)).thenReturn(Optional.of(adminEntity));
            when(passwordEncoder.encode("newAdminPass")).thenReturn("encodedAdminPass");

            // Act
            String result = userServiceClass.resetPassword(adminId, request);

            // Assert
            assertEquals("password resetted", result);
            assertEquals("encodedAdminPass", adminEntity.getPassword());
            verify(userRepository).save(adminEntity);
        }
    }

    @Nested
    @DisplayName("PageUsers Tests")
    class PageUsersTests {

        @Test
        @DisplayName("Should retrieve paginated users")
        void testPageUsers_Success() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            UserEntity userEntity = new UserEntity();
            userEntity.setId(userId);
            userEntity.setUsername("testuser");

            Page<UserEntity> userEntityPage = new PageImpl<>(Arrays.asList(userEntity), pageable, 1);

            UpdateRequest updateRequest = new UpdateRequest();
            updateRequest.setUsername("testuser");

            when(userRepository.findAll(pageable)).thenReturn(userEntityPage);
            when(userMapper.convertEntityToDto(userEntity)).thenReturn(updateRequest);

            // Act
            Page<UpdateRequest> result = userServiceClass.pageUsers(pageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals("testuser", result.getContent().get(0).getUsername());
            assertEquals(1, result.getTotalElements());
            verify(userRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should retrieve first page of users")
        void testPageUsers_FirstPage() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 5);
            UserEntity user1 = new UserEntity();
            user1.setId(UUID.randomUUID());
            user1.setUsername("user1");

            UserEntity user2 = new UserEntity();
            user2.setId(UUID.randomUUID());
            user2.setUsername("user2");

            UpdateRequest request1 = new UpdateRequest();
            request1.setUsername("user1");
            UpdateRequest request2 = new UpdateRequest();
            request2.setUsername("user2");

            Page<UserEntity> userPage = new PageImpl<>(Arrays.asList(user1, user2), pageable, 10);

            when(userRepository.findAll(pageable)).thenReturn(userPage);
            when(userMapper.convertEntityToDto(user1)).thenReturn(request1);
            when(userMapper.convertEntityToDto(user2)).thenReturn(request2);

            // Act
            Page<UpdateRequest> result = userServiceClass.pageUsers(pageable);

            // Assert
            assertEquals(2, result.getContent().size());
            assertEquals(0, result.getNumber());
            assertEquals(5, result.getSize());
            assertEquals(10, result.getTotalElements());
            verify(userRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should retrieve empty page when no users exist")
        void testPageUsers_EmptyPage() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<UserEntity> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(userRepository.findAll(pageable)).thenReturn(emptyPage);

            // Act
            Page<UpdateRequest> result = userServiceClass.pageUsers(pageable);

            // Assert
            assertNotNull(result);
            assertTrue(result.getContent().isEmpty());
            assertEquals(0, result.getTotalElements());
            assertEquals(0, result.getNumber());
            verify(userRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should retrieve second page of users")
        void testPageUsers_SecondPage() {
            // Arrange
            Pageable pageable = PageRequest.of(1, 5);
            UserEntity user6 = new UserEntity();
            user6.setId(UUID.randomUUID());
            user6.setUsername("user6");

            UpdateRequest request6 = new UpdateRequest();
            request6.setUsername("user6");

            Page<UserEntity> secondPage = new PageImpl<>(Arrays.asList(user6), pageable, 10);

            when(userRepository.findAll(pageable)).thenReturn(secondPage);
            when(userMapper.convertEntityToDto(user6)).thenReturn(request6);

            // Act
            Page<UpdateRequest> result = userServiceClass.pageUsers(pageable);

            // Assert
            assertEquals(1, result.getContent().size());
            assertEquals(1, result.getNumber());
            assertEquals(10, result.getTotalElements());
            verify(userRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should handle large page sizes")
        void testPageUsers_LargePageSize() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 100);
            List<UserEntity> users = new ArrayList<>();
            List<UpdateRequest> requests = new ArrayList<>();

            for (int i = 0; i < 50; i++) {
                UserEntity entity = new UserEntity();
                entity.setId(UUID.randomUUID());
                entity.setUsername("user" + i);
                users.add(entity);

                UpdateRequest request = new UpdateRequest();
                request.setUsername("user" + i);
                requests.add(request);

                when(userMapper.convertEntityToDto(entity)).thenReturn(request);
            }

            Page<UserEntity> userPage = new PageImpl<>(users, pageable, 50);
            when(userRepository.findAll(pageable)).thenReturn(userPage);

            // Act
            Page<UpdateRequest> result = userServiceClass.pageUsers(pageable);

            // Assert
            assertEquals(50, result.getContent().size());
            assertEquals(50, result.getTotalElements());
            assertEquals(100, result.getSize());
            verify(userRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should retrieve user, modify, and reset password")
        void testGetUserThenResetPassword() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUserEntity));
            when(userMapper.convertEntityToDto(testUserEntity)).thenReturn(testUpdateRequest);

            // Act - Get User
            UpdateRequest retrieved = userServiceClass.getUserById(userId);

            // Assert
            assertNotNull(retrieved);
            assertEquals("testuser", retrieved.getUsername());

            // Arrange - Reset Password
            ResetPasswordRequest resetRequest = new ResetPasswordRequest();
            resetRequest.setPassword("newpass");

            when(passwordEncoder.encode("newpass")).thenReturn("encodedNew");

            // Act - Reset Password
            String result = userServiceClass.resetPassword(userId, resetRequest);

            // Assert
            assertEquals("password resetted", result);
            assertEquals("encodedNew", testUserEntity.getPassword());
            verify(userRepository, times(2)).findById(userId);
        }
    }

    // Support class for ArrayList usage
    static class ArrayList<T> extends java.util.ArrayList<T> {
    }
}

