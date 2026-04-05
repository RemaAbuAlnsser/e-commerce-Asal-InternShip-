package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.AdminLoginRequest;
import com.asal.ecommerce.dto.AdminLoginResponse;
import com.asal.ecommerce.enums.Provider;
import com.asal.ecommerce.enums.Role;
import com.asal.ecommerce.mapper.AuthMapper;
import com.asal.ecommerce.model.User;
import com.asal.ecommerce.repository.UserRepository;
import com.asal.ecommerce.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testAdmin;
    private AdminLoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testAdmin = new User();
        testAdmin.setId(1L);
        testAdmin.setEmail("admin@example.com");
        testAdmin.setPassword("$2a$10$encodedPassword");
        testAdmin.setName("Admin User");
        testAdmin.setRole(Role.ADMIN);
        testAdmin.setProvider(Provider.LOCAL);
        testAdmin.setActive(true);

        loginRequest = new AdminLoginRequest();
        loginRequest.setEmail("admin@example.com");
        loginRequest.setPassword("admin123");

    }

    @Test
    void shouldReturnToken_whenLoginSuccessful() {
        // Given
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(testAdmin));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(1L, "admin@example.com", "ADMIN")).thenReturn("jwt-token");
        when(jwtUtil.getExpirationTime()).thenReturn(3600L);

        // When
        AdminLoginResponse result = userService.adminLogin(loginRequest);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Login successful", result.getMessage());
        assertEquals("jwt-token", result.getToken());
        assertEquals(3600L, result.getExpiresIn());
        assertNotNull(result.getAdmin());
        assertEquals("admin@example.com", result.getAdmin().getEmail());
    }

    @Test
    void shouldReturnError_whenWrongPassword() {
        // Given
        AdminLoginRequest wrongPasswordRequest = new AdminLoginRequest();
        wrongPasswordRequest.setEmail("admin@example.com");
        wrongPasswordRequest.setPassword("wrongpassword");
        
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(testAdmin));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        lenient().when(authMapper.toAdminLoginResponse(null, false, "Invalid email or password"))
            .thenReturn(new AdminLoginResponse(false, "Invalid email or password", null, null, null));

        // When
        AdminLoginResponse result = userService.adminLogin(wrongPasswordRequest);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Invalid email or password", result.getMessage());
        assertNull(result.getToken());
        assertNull(result.getAdmin());
    }

    @Test
    void shouldReturnError_whenEmailNotFound() {
        // Given
        AdminLoginRequest notFoundRequest = new AdminLoginRequest();
        notFoundRequest.setEmail("nonexistent@example.com");
        notFoundRequest.setPassword("admin123");
        
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        lenient().when(authMapper.toAdminLoginResponse(null, false, "Invalid email or password"))
            .thenReturn(new AdminLoginResponse(false, "Invalid email or password", null, null, null));

        // When
        AdminLoginResponse result = userService.adminLogin(notFoundRequest);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Invalid email or password", result.getMessage());
        assertNull(result.getToken());
        assertNull(result.getAdmin());
    }

    @Test
    void shouldReturnError_whenUserNotAdmin() {
        // Given
        testAdmin.setRole(Role.USER);
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(testAdmin));
        lenient().when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        lenient().when(authMapper.toAdminLoginResponse(null, false, "Access denied: Admin privileges required"))
            .thenReturn(new AdminLoginResponse(false, "Access denied: Admin privileges required", null, null, null));

        // When
        AdminLoginResponse result = userService.adminLogin(loginRequest);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Access denied: Admin privileges required", result.getMessage());
        assertNull(result.getToken());
        assertNull(result.getAdmin());
    }

    @Test
    void shouldReturnError_whenUserNotLocalProvider() {
        // Given
        testAdmin.setProvider(Provider.GOOGLE);
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(testAdmin));
        lenient().when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        lenient().when(authMapper.toAdminLoginResponse(null, false, "Invalid login method for admin account"))
            .thenReturn(new AdminLoginResponse(false, "Invalid login method for admin account", null, null, null));

        // When
        AdminLoginResponse result = userService.adminLogin(loginRequest);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Invalid login method for admin account", result.getMessage());
        assertNull(result.getToken());
        assertNull(result.getAdmin());
    }

    @Test
    void shouldReturnError_whenInactiveUser() {
        // Given
        testAdmin.setActive(false);
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(testAdmin));
        lenient().when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        lenient().when(authMapper.toAdminLoginResponse(null, false, "Account is deactivated"))
            .thenReturn(new AdminLoginResponse(false, "Account is deactivated", null, null, null));

        // When
        AdminLoginResponse result = userService.adminLogin(loginRequest);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Account is deactivated", result.getMessage());
        assertNull(result.getToken());
        assertNull(result.getAdmin());
    }

    @Test
    void shouldCreateGoogleUser_whenNewUser() {
        // Given
        User newGoogleUser = new User();
        newGoogleUser.setId(2L);
        newGoogleUser.setEmail("user@gmail.com");
        newGoogleUser.setName("Google User");
        newGoogleUser.setGoogleId("google123");
        newGoogleUser.setRole(Role.USER);
        newGoogleUser.setProvider(Provider.GOOGLE);

        when(userRepository.findByGoogleIdAndProvider("google123", Provider.GOOGLE)).thenReturn(Optional.empty());
        when(authMapper.createGoogleUser("user@gmail.com", "Google User", "google123")).thenReturn(newGoogleUser);
        when(userRepository.save(newGoogleUser)).thenReturn(newGoogleUser);

        // When
        User result = userService.createOrUpdateGoogleUser("user@gmail.com", "Google User", "google123");

        // Then
        assertNotNull(result);
        assertEquals("user@gmail.com", result.getEmail());
        assertEquals("Google User", result.getName());
        assertEquals("google123", result.getGoogleId());
        assertEquals(Role.USER, result.getRole());
        assertEquals(Provider.GOOGLE, result.getProvider());
        verify(userRepository).save(newGoogleUser);
    }

    @Test
    void shouldUpdateGoogleUser_whenExistingUser() {
        // Given
        User existingGoogleUser = new User();
        existingGoogleUser.setId(2L);
        existingGoogleUser.setEmail("old@gmail.com");
        existingGoogleUser.setName("Old Name");
        existingGoogleUser.setGoogleId("google123");
        existingGoogleUser.setRole(Role.USER);
        existingGoogleUser.setProvider(Provider.GOOGLE);

        when(userRepository.findByGoogleIdAndProvider("google123", Provider.GOOGLE)).thenReturn(Optional.of(existingGoogleUser));
        when(userRepository.save(existingGoogleUser)).thenReturn(existingGoogleUser);

        // When
        User result = userService.createOrUpdateGoogleUser("new@gmail.com", "New Name", "google123");

        // Then
        assertNotNull(result);
        verify(authMapper).updateGoogleUser(existingGoogleUser, "new@gmail.com", "New Name");
        verify(userRepository).save(existingGoogleUser);
    }

    @Test
    void shouldCreateDefaultAdmin_whenNoAdminExists() {
        // Given
        User defaultAdmin = new User();
        defaultAdmin.setId(1L);
        defaultAdmin.setEmail("admin@example.com");
        defaultAdmin.setRole(Role.ADMIN);

        when(userRepository.countByRole(Role.ADMIN)).thenReturn(0L);
        when(authMapper.createDefaultAdmin()).thenReturn(defaultAdmin);
        when(userRepository.save(defaultAdmin)).thenReturn(defaultAdmin);

        // When
        userService.createDefaultAdmin();

        // Then
        verify(userRepository).countByRole(Role.ADMIN);
        verify(authMapper).createDefaultAdmin();
        verify(userRepository).save(defaultAdmin);
    }

    @Test
    void shouldSkipCreation_whenAdminAlreadyExists() {
        // Given
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L);

        // When
        userService.createDefaultAdmin();

        // Then
        verify(userRepository).countByRole(Role.ADMIN);
        verify(authMapper, never()).createDefaultAdmin();
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldReturnTrue_whenUserIsAdmin() {
        // Given
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(testAdmin));

        // When
        boolean result = userService.isAdmin("admin@example.com");

        // Then
        assertTrue(result);
    }

    @Test
    void shouldReturnFalse_whenUserIsNotAdmin() {
        // Given
        testAdmin.setRole(Role.USER);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testAdmin));

        // When
        boolean result = userService.isAdmin("user@example.com");

        // Then
        assertFalse(result);
    }

    @Test
    void shouldReturnFalse_whenUserNotFound() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        boolean result = userService.isAdmin("nonexistent@example.com");

        // Then
        assertFalse(result);
    }

    @Test
    void shouldFixAdminPassword_whenAdminExists() {
        // Given
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(testAdmin));
        when(passwordEncoder.encode("admin123")).thenReturn("$2a$10$newEncodedPassword");
        when(userRepository.save(testAdmin)).thenReturn(testAdmin);

        // When
        userService.fixAdminPassword();

        // Then
        verify(passwordEncoder).encode("admin123");
        assertEquals("$2a$10$newEncodedPassword", testAdmin.getPassword());
        verify(userRepository).save(testAdmin);
    }

    @Test
    void shouldHandleException_whenFixAdminPasswordFails() {
        // Given
        when(userRepository.findByEmail("admin@example.com")).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertDoesNotThrow(() -> userService.fixAdminPassword());
        verify(userRepository, never()).save(any());
    }
}
