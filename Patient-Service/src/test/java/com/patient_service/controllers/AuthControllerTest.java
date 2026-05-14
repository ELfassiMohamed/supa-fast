package com.patient_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patient_service.dto.AuthRequest;
import com.patient_service.dto.RegisterRequest;
import com.patient_service.enums.AccountStatus;
import com.patient_service.enums.Role;
import com.patient_service.models.Patient;
import com.patient_service.services.JwtService;
import com.patient_service.services.PatientPublisherService;
import com.patient_service.services.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for unit test
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private PatientPublisherService patientPublisherService;

    @Autowired
    private ObjectMapper objectMapper;

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId("patient123");
        patient.setEmail("test@example.com");
        patient.setAccountStatus(AccountStatus.PENDING);
        patient.setRole(Role.PATIENT);
    }

    @Test
    void register_Success() throws Exception {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");
        
        when(patientService.registerPatient(eq("test@example.com"), eq("password123"), any(RegisterRequest.class)))
                .thenReturn(patient);
        when(jwtService.generateToken(any())).thenReturn("mockToken");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mockToken"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void login_Success() throws Exception {
        AuthRequest request = new AuthRequest("test@example.com", "password123");
        Authentication authentication = new UsernamePasswordAuthenticationToken(patient, null);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken(any())).thenReturn("mockToken");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mockToken"));
    }

    @Test
    void login_Failure() throws Exception {
        AuthRequest request = new AuthRequest("test@example.com", "wrong");
        when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }
}
