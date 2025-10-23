package service;

import com.example.todo.model.entity.Role;
import com.example.todo.model.entity.User;
import com.example.todo.repository.UserRepository;
import com.example.todo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setRoles(Set.of(Role.ROLE_USER));
    }

    @Test
    void register_WithNewUser_ShouldSaveUserSuccessfully() {
        String username = "newuser";
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            userToSave.setId(1L);
            return userToSave;
        });

        userService.register(username, rawPassword);

        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, times(1)).encode(rawPassword);
        verify(userRepository, times(1)).save(any(User.class));

        verify(userRepository).save(argThat(user ->
                user.getUsername().equals(username) &&
                        user.getPassword().equals(encodedPassword) &&
                        user.getRoles().equals(Set.of(Role.ROLE_USER))
        ));
    }

    @Test
    void register_WithExistingUser_ShouldThrowRuntimeException() {
        String username = "existinguser";
        String rawPassword = "password123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.register(username, rawPassword);
        });

        assertEquals("User already exists", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_WithEmptyPassword_ShouldEncodeEmptyPassword() {
        String username = "newuser";
        String rawPassword = "";
        String encodedPassword = "encodedEmptyPassword";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.register(username, rawPassword);

        verify(passwordEncoder, times(1)).encode(rawPassword);
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ========== findByUsername TESTS ==========

    @Test
    void findByUsername_WithExistingUser_ShouldReturnUser() {
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findByUsername(username);

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void findByUsername_WithNonExistingUser_ShouldReturnEmpty() {
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsername(username);

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void findByUsername_WithNullUsername_ShouldReturnEmpty() {
        String username = null;
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsername(username);

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByUsername(username);
    }

    // ========== loadUserByUsername TESTS ==========

    @Test
    void loadUserByUsername_WithExistingUser_ShouldReturnUserDetails() {
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertEquals(1, userDetails.getAuthorities().size());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void loadUserByUsername_WithUserHavingMultipleRoles_ShouldReturnAllAuthorities() {
        User adminUser = new User();
        adminUser.setUsername("adminuser");
        adminUser.setPassword("adminPassword");
        adminUser.setRoles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN));

        String username = "adminuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(adminUser));

        UserDetails userDetails = userService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals("adminuser", userDetails.getUsername());
        assertEquals(2, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_WithUserHavingNoRoles_ShouldReturnEmptyAuthorities() {
        User userWithNoRoles = new User();
        userWithNoRoles.setUsername("norolesuser");
        userWithNoRoles.setPassword("password");
        userWithNoRoles.setRoles(Set.of());

        String username = "norolesuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userWithNoRoles));

        UserDetails userDetails = userService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals("norolesuser", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().isEmpty());
    }

}
