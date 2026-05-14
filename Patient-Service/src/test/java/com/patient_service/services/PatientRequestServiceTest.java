package com.patient_service.services;

import com.patient_service.dto.PatientRequestDTO;
import com.patient_service.dto.PatientRequestResponseDTO;
import com.patient_service.dto.RequestMessageDTO;
import com.patient_service.enums.AccountStatus;
import com.patient_service.models.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientRequestServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PatientRequestService patientRequestService;

    @Mock
    private Authentication authentication;

    private Patient patient;
    private PatientRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId("patient123");
        patient.setAccountStatus(AccountStatus.ACTIVE);

        requestDTO = new PatientRequestDTO();
        requestDTO.setType("APPOINTMENT");
        requestDTO.setPriority("HIGH");
        requestDTO.setSubject("Need appointment");
        requestDTO.setDescription("Description");
        requestDTO.setPreferredDate(LocalDate.now().plusDays(1));
    }

    @Test
    void submitRequest_Success() {
        when(authentication.getPrincipal()).thenReturn(patient);

        PatientRequestResponseDTO response = patientRequestService.submitRequest(requestDTO, authentication);

        assertNotNull(response);
        assertEquals("QUEUED", response.getStatus());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Map.class));
    }

    @Test
    void submitRequest_InactiveAccount() {
        patient.setAccountStatus(AccountStatus.PENDING);
        when(authentication.getPrincipal()).thenReturn(patient);

        assertThrows(IllegalStateException.class, () -> patientRequestService.submitRequest(requestDTO, authentication));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Map.class));
    }

    @Test
    void addMessage_Success() {
        RequestMessageDTO messageDTO = new RequestMessageDTO();
        messageDTO.setContent("New message");

        patientRequestService.addMessage("req123", messageDTO, authentication);

        verify(rabbitTemplate).convertAndSend(eq("patient.messages.exchange"), eq("patient.messages.key"), any(Map.class));
    }
}
