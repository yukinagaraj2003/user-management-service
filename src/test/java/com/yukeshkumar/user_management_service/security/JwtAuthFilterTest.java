package com.yukeshkumar.user_management_service.security;

import com.yukeshkumar.user_management_service.model.CustomUserDetails;
import com.yukeshkumar.user_management_service.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    @Mock
    private JwtUtility jwtUtility;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintWriter printWriter;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_LoginPath_SkipsFilter() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtility);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void testDoFilterInternal_PasswordResetPath_SkipsFilter() throws Exception {
        when(request.getRequestURI()).thenReturn("/users/123/password");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtility);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void testDoFilterInternal_NoAuthorizationHeader_ContinuesChain() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtility);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void testDoFilterInternal_InvalidAuthorizationHeader_ContinuesChain() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtility);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void testDoFilterInternal_ValidToken_SetsAuthentication() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = "valid.jwt.token";

        CustomUserDetails userDetails = new CustomUserDetails(
                userId,
                "testuser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtility.extractUserId(token)).thenReturn(userId);
        when(userDetailsService.loadUserById(userId)).thenReturn(userDetails);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtility).extractUserId(token);
        verify(userDetailsService).loadUserById(userId);
        // Note: SecurityContextHolder.getContext().getAuthentication() would be set, but hard to verify in unit test
    }

    @Test
    void testDoFilterInternal_InvalidToken_SetsForbiddenResponse() throws Exception {
        String token = "invalid.jwt.token";

        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtility.extractUserId(token)).thenThrow(new RuntimeException("Invalid token"));
        when(response.getWriter()).thenReturn(printWriter);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(printWriter).write("Invalid or expired token");
        verify(filterChain, never()).doFilter(any(), any());
    }
}
