package controller;

import com.example.todo.controller.AuthController;
import com.example.todo.model.dto.AuthRequest;
import com.example.todo.model.dto.AuthResponse;
import com.example.todo.model.dto.RegisterRequest;
import com.example.todo.security.JwtUtil;
import com.example.todo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest registerRequest;
    private AuthRequest authRequest;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");

        authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("password123");

        userDetails = new User(
                "testuser",
                "encodedPassword",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    // ========== register TESTS ==========

    @Test
    void register_WithValidRequest_ShouldReturnOk() {
        doNothing().when(userService).register(registerRequest.getUsername(), registerRequest.getPassword());

        ResponseEntity<?> response = authController.register(registerRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User created", response.getBody());
        verify(userService, times(1)).register(registerRequest.getUsername(), registerRequest.getPassword());
    }

    @Test
    void register_WithExistingUser_ShouldPropagateException() {
        String errorMessage = "User already exists";
        doThrow(new RuntimeException(errorMessage))
                .when(userService).register(registerRequest.getUsername(), registerRequest.getPassword());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authController.register(registerRequest);
        });

        assertEquals(errorMessage, exception.getMessage());
        verify(userService, times(1)).register(registerRequest.getUsername(), registerRequest.getPassword());
    }

    @Test
    void register_WithNullRequest_ShouldThrowException() {
        assertThrows(Exception.class, () -> {
            authController.register(null);
        });
    }

    @Test
    void register_WithNullUsername_ShouldPassNullToService() {
        RegisterRequest requestWithNullUsername = new RegisterRequest();
        requestWithNullUsername.setUsername(null);
        requestWithNullUsername.setPassword("password123");

        doNothing().when(userService).register(null, "password123");

        ResponseEntity<?> response = authController.register(requestWithNullUsername);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).register(null, "password123");
    }

    // ========== login TESTS ==========

    @Test
    void login_WithValidCredentials_ShouldReturnToken() {
        String expectedToken = "jwt.token.here";

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn(expectedToken);

        ResponseEntity<AuthResponse> response = authController.login(authRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedToken, response.getBody().getToken());

        verify(authManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(authentication, times(1)).getPrincipal();
        verify(jwtUtil, times(1)).generateToken(userDetails);
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowBadCredentialsException() {
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authController.login(authRequest);
        });

        assertEquals("Bad credentials", exception.getMessage());
        verify(authManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(authentication, never()).getPrincipal();
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void login_WithNullRequest_ShouldThrowException() {
        assertThrows(Exception.class, () -> {
            authController.login(null);
        });
    }

    @Test
    void login_WithEmptyPassword_ShouldAttemptAuthentication() {
        AuthRequest requestWithEmptyPassword = new AuthRequest();
        requestWithEmptyPassword.setUsername("testuser");
        requestWithEmptyPassword.setPassword("");

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("token");

        ResponseEntity<AuthResponse> response = authController.login(requestWithEmptyPassword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authManager, times(1)).authenticate(argThat(token ->
                token.getPrincipal().equals("testuser") &&
                        token.getCredentials().equals("")
        ));
    }

    // ========== Other TESTS ==========

    @Test
    void login_ShouldUseUserDetailsFromAuthentication() {
        UserDetails differentUserDetails = new User(
                "differentuser",
                "differentPassword",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(differentUserDetails);
        when(jwtUtil.generateToken(differentUserDetails)).thenReturn("adminToken");

        ResponseEntity<AuthResponse> response = authController.login(authRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(jwtUtil, times(1)).generateToken(differentUserDetails);
    }

    @Test
    void login_WhenAuthenticationManagerThrowsOtherException_ShouldPropagate() {
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Authentication service unavailable"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authController.login(authRequest);
        });

        assertEquals("Authentication service unavailable", exception.getMessage());
    }


}
