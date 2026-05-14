package com.patient_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patient_service.dto.ProfileStatusResponse;
import com.patient_service.enums.AccountStatus;
import com.patient_service.models.Patient;
import com.patient_service.services.JwtService;
import com.patient_service.services.MedicalRecordClientService;
import com.patient_service.services.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PatientProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    @MockBean
    private MedicalRecordClientService medicalRecordClientService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private Patient patient;
    private Authentication auth;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId("patient123");
        patient.setEmail("test@example.com");
        patient.setAccountStatus(AccountStatus.ACTIVE);
        auth = new UsernamePasswordAuthenticationToken(patient, null);
    }

    @Test
    void getProfileStatus_Success() throws Exception {
        ProfileStatusResponse statusResponse = new ProfileStatusResponse("patient123", "test@example.com", AccountStatus.ACTIVE);
        when(patientService.getProfileStatus("patient123")).thenReturn(statusResponse);

        mockMvc.perform(get("/api/patient/profile-status")
                .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value("patient123"));
    }

    @Test
    void getProfile_Success() throws Exception {
        when(patientService.findById("patient123")).thenReturn(patient);

        mockMvc.perform(get("/api/patient/profile")
                .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("patient123"));
    }

    @Test
    void updateProfile_Success() throws Exception {
        Patient profileUpdates = new Patient();
        profileUpdates.setFirstName("New");
        profileUpdates.setLastName("Name");

        when(patientService.updatePatientProfile(anyString(), any(Patient.class))).thenReturn(patient);

        mockMvc.perform(put("/api/patient/complete-profile")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileUpdates)))
                .andExpect(status().isOk());
    }

    @Test
    void getMedicalHistory_Success() throws Exception {
        when(medicalRecordClientService.getPatientMedicalRecords(anyString(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/patient/medical-history")
                .principal(auth)
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }

    @Test
    void getMedicalHistory_Forbidden_PendingAccount() throws Exception {
        patient.setAccountStatus(AccountStatus.PENDING);
        
        mockMvc.perform(get("/api/patient/medical-history")
                .principal(auth))
                .andExpect(status().isForbidden());
    }
}
