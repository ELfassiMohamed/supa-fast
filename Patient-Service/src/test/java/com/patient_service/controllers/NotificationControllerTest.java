package com.patient_service.controllers;

import com.patient_service.dto.PatientRequestResponseDTO;
import com.patient_service.enums.AccountStatus;
import com.patient_service.models.Patient;
import com.patient_service.services.JwtService;
import com.patient_service.services.PatientService;
import com.patient_service.services.RequestResponseService;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestResponseService requestResponseService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private PatientService patientService;

    private Patient activePatient;
    private Patient pendingPatient;
    private Authentication activeAuth;
    private Authentication pendingAuth;

    @BeforeEach
    void setUp() {
        activePatient = new Patient();
        activePatient.setId("patient123");
        activePatient.setAccountStatus(AccountStatus.ACTIVE);
        activeAuth = new UsernamePasswordAuthenticationToken(activePatient, null);

        pendingPatient = new Patient();
        pendingPatient.setId("pending123");
        pendingPatient.setAccountStatus(AccountStatus.PENDING);
        pendingAuth = new UsernamePasswordAuthenticationToken(pendingPatient, null);
    }

    @Test
    void getNotifications_Success() throws Exception {
        PatientRequestResponseDTO notification = new PatientRequestResponseDTO();
        notification.setRequestId("req123");
        notification.setPatientId("patient123");

        when(requestResponseService.getPatientNotifications("patient123")).thenReturn(Collections.singletonList(notification));

        mockMvc.perform(get("/api/notifications")
                .principal(activeAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].requestId").value("req123"));
    }

    @Test
    void getNotifications_Forbidden_PendingAccount() throws Exception {
        mockMvc.perform(get("/api/notifications")
                .principal(pendingAuth))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Account not activated"));
    }

    @Test
    void getNotification_ById_Success() throws Exception {
        PatientRequestResponseDTO notification = new PatientRequestResponseDTO();
        notification.setRequestId("req123");
        notification.setPatientId("patient123");

        when(requestResponseService.getResponse("req123")).thenReturn(notification);

        mockMvc.perform(get("/api/notifications/req123")
                .principal(activeAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("req123"));
    }

    @Test
    void getNotification_ById_NotFound() throws Exception {
        when(requestResponseService.getResponse("req123")).thenReturn(null);

        mockMvc.perform(get("/api/notifications/req123")
                .principal(activeAuth))
                .andExpect(status().isNotFound());
    }

    @Test
    void getNotification_ById_Forbidden_NotOwner() throws Exception {
        PatientRequestResponseDTO notification = new PatientRequestResponseDTO();
        notification.setRequestId("req123");
        notification.setPatientId("otherPatient");

        when(requestResponseService.getResponse("req123")).thenReturn(notification);

        mockMvc.perform(get("/api/notifications/req123")
                .principal(activeAuth))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Accès refusé - Cette notification ne vous appartient pas"));
    }
}
