package com.patient_service.config;

import com.patient_service.models.Patient;
import com.patient_service.services.JwtService;
import com.patient_service.services.PatientService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private PatientService patientService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_NoHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    void doFilterInternal_InvalidHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic 123");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    void doFilterInternal_ValidToken() throws ServletException, IOException {
        String token = "validToken";
        String email = "test@example.com";
        Patient patient = new Patient();
        patient.setEmail(email);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn(email);
        when(patientService.loadUserByUsername(email)).thenReturn(patient);
        when(jwtService.isTokenValid(token, patient)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assert(SecurityContextHolder.getContext().getAuthentication() != null);
        assert(SecurityContextHolder.getContext().getAuthentication().getName().equals(email));
    }

    @Test
    void doFilterInternal_InvalidToken() throws ServletException, IOException {
        String token = "invalidToken";
        String email = "test@example.com";
        Patient patient = new Patient();
        patient.setEmail(email);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn(email);
        when(patientService.loadUserByUsername(email)).thenReturn(patient);
        when(jwtService.isTokenValid(token, patient)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assert(SecurityContextHolder.getContext().getAuthentication() == null);
    }
}
