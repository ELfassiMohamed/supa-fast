package com.patient_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patient_service.dto.PatientRequestDTO;
import com.patient_service.dto.PatientRequestResponseDTO;
import com.patient_service.dto.RequestMessageDTO;
import com.patient_service.enums.AccountStatus;
import com.patient_service.models.Patient;
import com.patient_service.services.JwtService;
import com.patient_service.services.PatientRequestService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientRequestService requestService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private PatientService patientService;

    @Autowired
    private ObjectMapper objectMapper;

    private Patient patient;
    private Authentication auth;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId("patient123");
        patient.setAccountStatus(AccountStatus.ACTIVE);
        auth = new UsernamePasswordAuthenticationToken(patient, null);
    }

    @Test
    void submitRequest_Success() throws Exception {
        PatientRequestDTO requestDTO = new PatientRequestDTO();
        requestDTO.setType("APPOINTMENT");
        requestDTO.setPriority("NORMAL");
        requestDTO.setSubject("Subject");

        PatientRequestResponseDTO response = new PatientRequestResponseDTO("req123", "QUEUED", "Message");
        when(requestService.submitRequest(any(PatientRequestDTO.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/requests")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.requestId").value("req123"));
    }

    @Test
    void submitRequest_Forbidden_PendingAccount() throws Exception {
        patient.setAccountStatus(AccountStatus.PENDING);
        PatientRequestDTO requestDTO = new PatientRequestDTO();

        mockMvc.perform(post("/api/requests")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void addMessage_Success() throws Exception {
        RequestMessageDTO messageDTO = new RequestMessageDTO();
        messageDTO.setContent("New message");

        mockMvc.perform(post("/api/requests/req123/message")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void addMessage_Forbidden_PendingAccount() throws Exception {
        patient.setAccountStatus(AccountStatus.PENDING);
        RequestMessageDTO messageDTO = new RequestMessageDTO();

        mockMvc.perform(post("/api/requests/req123/message")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageDTO)))
                .andExpect(status().isForbidden());
    }
}
