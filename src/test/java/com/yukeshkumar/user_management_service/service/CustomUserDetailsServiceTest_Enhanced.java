package com.yukeshkumar.user_management_service.service;

import com.yukeshkumar.user_management_service.entity.RoleType;
import com.yukeshkumar.user_management_service.entity.UserEntity;
import com.yukeshkumar.user_management_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("CustomUserDetailsService Enhanced Test Suite")
class CustomUserDetailsServiceTest_Enhanced {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private UUID testUserId;
    private UserEntity testUserEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUserId = UUID.randomUUID();

        testUserEntity = new UserEntity();
        testUserEntity.setId(testUserId);
        testUserEntity.setUsername("testuser");
        testUserEntity.setPassword("password");
        testUserEntity.setEmail("test@example.com");
        testUserEntity.setFull_name("Test User");
        testUserEntity.setRole(RoleType.ROLE_USER);
    }

    @Nested
    @DisplayName("LoadUserByUsername Tests")
    class LoadUserByUsernameTests {

        @Test
        @DisplayName("Should successfully load user by username")
        void testLoadUserByUsername_Success() {
            // Arrange
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUserEntity));

            // Act
            UserDetails result = customUserDetailsService.loadUserByUsername("testuser");

            // Assert
            assertNotNull(result);
            assertEquals("testuser", result.getUsername());
            assertEquals("password", result.getPassword());
            assertTrue(result.isEnabled());
            assertTrue(result.isAccountNonExpired());
            assertTrue(result.isAccountNonLocked());
            assertTrue(result.isCredentialsNonExpired());
            verify(userRepository).findByUsername("testuser");
        }

        @Test
        @DisplayName("Should load user with ROLE_USER authority")
        void testLoadUserByUsername_UserRole() {
            // Arrange
            testUserEntity.setRole(RoleType.ROLE_USER);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUserEntity));

            // Act
            UserDetails result = customUserDetailsService.loadUserByUsername("testuser");

            // Assert
            assertNotNull(result);
            assertTrue(result.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
            assertEquals(1, result.getAuthorities().size());
            verify(userRepository).findByUsername("testuser");
        }

        @Test
        @DisplayName("Should load user with ROLE_ADMIN authority")
        void testLoadUserByUsername_AdminRole() {
            // Arrange
            testUserEntity.setRole(RoleType.ROLE_ADMIN);
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUserEntity));

            // Act
            UserDetails result = customUserDetailsService.loadUserByUsername("admin");

            // Assert
            assertNotNull(result);
            assertTrue(result.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
            verify(userRepository).findByUsername("admin");
        }

        @Test
        @DisplayName("Should load user with ROLE_MANAGER authority")
        void testLoadUserByUsername_ManagerRole() {
            // Arrange
            testUserEntity.setRole(RoleType.ROLE_MANAGER);
            when(userRepository.findByUsername("manager")).thenReturn(Optional.of(testUserEntity));

            // Act
            UserDetails result = customUserDetailsService.loadUserByUsername("manager");

            // Assert
            assertNotNull(result);
            assertTrue(result.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_MANAGER")));
            verify(userRepository).findByUsername("manager");
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when user not found")
        void testLoadUserByUsername_UserNotFound() {
            // Arrange
            when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

            // Act & Assert
            UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                    () -> customUserDetailsService.loadUserByUsername("nonexistent"));
            assertEquals("not found", exception.getMessage());
            verify(userRepository).findByUsername("nonexistent");
        }

        @Test
        @DisplayName("Should load user with complete user details")
        void testLoadUserByUsername_CompleteDetails() {
            // Arrange
            testUserEntity.setEmail("user@example.com");
            testUserEntity.setFull_name("John Doe");
            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUserEntity));

            // Act
            UserDetails result = customUserDetailsService.loadUserByUsername("johndoe");

            // Assert
            assertNotNull(result);
            assertEquals("testuser", result.getUsername());
            assertEquals("password", result.getPassword());
            verify(userRepository).findByUsername("johndoe");
        }

        @Test
        @DisplayName("Should handle username with special characters")
        void testLoadUserByUsername_SpecialCharacters() {
            // Arrange
            testUserEntity.setUsername("user@example.com");
            when(userRepository.findByUsername("user@example.com")).thenReturn(Optional.of(testUserEntity));

            // Act
            UserDetails result = customUserDetailsService.loadUserByUsername("user@example.com");

            // Assert
            assertNotNull(result);
            assertEquals("user@example.com", result.getUsername());
            verify(userRepository).findByUsername("user@example.com");
        }

        @Test
        @DisplayName("Should handle case-sensitive usernames")
        void testLoadUserByUsername_CaseSensitive() {
            // Arrange
            testUserEntity.setUsername("TestUser");
            when(userRepository.findByUsername("TestUser")).thenReturn(Optional.of(testUserEntity));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

            // Act & Assert
            UserDetails result = customUserDetailsService.loadUserByUsername("TestUser");
            assertNotNull(result);
            assertEquals("TestUser", result.getUsername());

            UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                    () -> customUserDetailsService.loadUserByUsername("testuser"));
            assertNotNull(exception);
        }

        @Test
        @DisplayName("Should preserve authority order")
        void testLoadUserByUsername_AuthorityContent() {
            // Arrange
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUserEntity));

            // Act
            UserDetails result = customUserDetailsService.loadUserByUsername("testuser");

            // Assert
            Collection<? extends GrantedAuthority> authorities = result.getAuthorities();
            assertTrue(authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList()
                    .contains("ROLE_USER"));
            assertEquals(1, authorities.size());
        }
    }

    @Nested
    @DisplayName("LoadUserById Tests")
    class LoadUserByIdTests {

        @Test
        @DisplayName("Should successfully load user by ID")
        void testLoadUserById_Success() {
            // Arrange
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUserEntity));

            // Act
            UserDetails result = customUserDetailsService.loadUserById(testUserId);

            // Assert
            assertNotNull(result);
            assertEquals("testuser", result.getUsername());
            assertEquals("password", result.getPassword());
            verify(userRepository).findById(testUserId);
        }

        @Test
        @DisplayName("Should load user by ID with ROLE_ADMIN")
        void testLoadUserById_AdminRole() {
            // Arrange
            testUserEntity.setRole(RoleType.ROLE_ADMIN);
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUserEntity));

            // Act
            UserDetails result = customUserDetailsService.loadUserById(testUserId);

            // Assert
            assertNotNull(result);
            assertTrue(result.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
            verify(userRepository).findById(testUserId);
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when user ID not found")
        void testLoadUserById_UserNotFound() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                    () -> customUserDetailsService.loadUserById(nonExistentId));
            assertEquals("User not found", exception.getMessage());
            verify(userRepository).findById(nonExistentId);
        }

        @Test
        @DisplayName("Should load different users by their IDs")
        void testLoadUserById_DifferentIds() {
            // Arrange
            UUID userId2 = UUID.randomUUID();
            UserEntity user2 = new UserEntity();
            user2.setId(userId2);
            user2.setUsername("user2");
            user2.setPassword("password2");
            user2.setRole(RoleType.ROLE_USER);

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUserEntity));
            when(userRepository.findById(userId2)).thenReturn(Optional.of(user2));

            // Act
            UserDetails result1 = customUserDetailsService.loadUserById(testUserId);
            UserDetails result2 = customUserDetailsService.loadUserById(userId2);

            // Assert
            assertNotNull(result1);
            assertNotNull(result2);
            assertEquals("testuser", result1.getUsername());
            assertEquals("user2", result2.getUsername());
            verify(userRepository).findById(testUserId);
            verify(userRepository).findById(userId2);
        }

        @Test
        @DisplayName("Should load user by ID with ROLE_MANAGER")
        void testLoadUserById_ManagerRole() {
            // Arrange
            testUserEntity.setRole(RoleType.ROLE_MANAGER);
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUserEntity));

            // Act
            UserDetails result = customUserDetailsService.loadUserById(testUserId);

            // Assert
            assertTrue(result.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_MANAGER")));
        }

        @Test
        @DisplayName("Should preserve all user data when loading by ID")
        void testLoadUserById_PreserveData() {
            // Arrange
            testUserEntity.setEmail("test@example.com");
            testUserEntity.setFull_name("Test User Full");
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUserEntity));

            // Act
            UserDetails result = customUserDetailsService.loadUserById(testUserId);

            // Assert
            assertNotNull(result);
            assertEquals("testuser", result.getUsername());
            assertEquals("password", result.getPassword());
            assertEquals(1, result.getAuthorities().size());
        }
    }

    @Nested
    @DisplayName("Authority Handling Tests")
    class AuthorityHandlingTests {

        @Test
        @DisplayName("Should correctly format authority with ROLE_ prefix")
        void testLoadUserByUsername_RolePrefixHandling() {
            // Arrange
            testUserEntity.setRole(RoleType.ROLE_USER);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUserEntity));

            // Act
            UserDetails result = customUserDetailsService.loadUserByUsername("testuser");

            // Assert
            assertTrue(result.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
            verify(userRepository).findByUsername("testuser");
        }

        @Test
        @DisplayName("Should handle role without ROLE_ prefix")
        void testLoadUserByUsername_RoleWithoutPrefix() {
            // Arrange
            testUserEntity.setRole(RoleType.ROLE_USER);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUserEntity));

            // Act
            UserDetails result = customUserDetailsService.loadUserByUsername("testuser");

            // Assert
            String authority = result.getAuthorities().iterator().next().getAuthority();
            assertTrue(authority.startsWith("ROLE_"));
            verify(userRepository).findByUsername("testuser");
        }

        @Test
        @DisplayName("Should have exactly one authority per user")
        void testLoadUserByUsername_SingleAuthority() {
            // Arrange
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUserEntity));

            // Act
            UserDetails result = customUserDetailsService.loadUserByUsername("testuser");

            // Assert
            assertEquals(1, result.getAuthorities().size());
            verify(userRepository).findByUsername("testuser");
        }

        @Test
        @DisplayName("Should create SimpleGrantedAuthority objects")
        void testLoadUserByUsername_AuthorityType() {
            // Arrange
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUserEntity));

            // Act
            UserDetails result = customUserDetailsService.loadUserByUsername("testuser");

            // Assert
            assertTrue(result.getAuthorities().iterator().next() instanceof SimpleGrantedAuthority);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null username")
        void testLoadUserByUsername_NullUsername() {
            // Arrange
            when(userRepository.findByUsername(null)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(UsernameNotFoundException.class,
                    () -> customUserDetailsService.loadUserByUsername(null));
        }

        @Test
        @DisplayName("Should handle empty string username")
        void testLoadUserByUsername_EmptyUsername() {
            // Arrange
            when(userRepository.findByUsername("")).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(UsernameNotFoundException.class,
                    () -> customUserDetailsService.loadUserByUsername(""));
        }

        @Test
        @DisplayName("Should handle very long username")
        void testLoadUserByUsername_LongUsername() {
            // Arrange
            String longUsername = "a".repeat(255);
            testUserEntity.setUsername(longUsername);
            when(userRepository.findByUsername(longUsername)).thenReturn(Optional.of(testUserEntity));

            // Act
            UserDetails result = customUserDetailsService.loadUserByUsername(longUsername);

            // Assert
            assertNotNull(result);
            assertEquals(longUsername, result.getUsername());
        }

        @Test
        @DisplayName("Should handle whitespace in username")
        void testLoadUserByUsername_WhitespaceUsername() {
            // Arrange
            String usernameWithSpace = "user name";
            testUserEntity.setUsername(usernameWithSpace);
            when(userRepository.findByUsername(usernameWithSpace)).thenReturn(Optional.of(testUserEntity));

            // Act
            UserDetails result = customUserDetailsService.loadUserByUsername(usernameWithSpace);

            // Assert
            assertNotNull(result);
            assertEquals(usernameWithSpace, result.getUsername());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should load user by username and then by ID")
        void testLoadByUsernameAndId() {
            // Arrange
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUserEntity));
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUserEntity));

            // Act - Load by username
            UserDetails resultByUsername = customUserDetailsService.loadUserByUsername("testuser");
            
            // Act - Load by ID
            UserDetails resultById = customUserDetailsService.loadUserById(testUserId);

            // Assert
            assertNotNull(resultByUsername);
            assertNotNull(resultById);
            assertEquals(resultByUsername.getUsername(), resultById.getUsername());
            assertEquals(resultByUsername.getPassword(), resultById.getPassword());
            verify(userRepository).findByUsername("testuser");
            verify(userRepository).findById(testUserId);
        }

        @Test
        @DisplayName("Should handle multiple role types in sequence")
        void testLoadMultipleRolesSequence() {
            // Arrange
            testUserEntity.setRole(RoleType.ROLE_USER);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUserEntity));

            // Act & Assert - USER
            UserDetails userResult = customUserDetailsService.loadUserByUsername("testuser");
            assertTrue(userResult.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));

            // Arrange - ADMIN
            testUserEntity.setRole(RoleType.ROLE_ADMIN);
            UserDetails adminResult = customUserDetailsService.loadUserByUsername("testuser");
            assertTrue(adminResult.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));

            // Arrange - MANAGER
            testUserEntity.setRole(RoleType.ROLE_MANAGER);
            UserDetails managerResult = customUserDetailsService.loadUserByUsername("testuser");
            assertTrue(managerResult.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_MANAGER")));
        }
    }
}

